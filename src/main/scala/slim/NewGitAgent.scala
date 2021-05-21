package slim

import java.nio.{ByteBuffer, CharBuffer}
import ch.uzh.ifi.seal.lisa.core.source.AsyncAgent

import java.nio.file._
import scala.collection.JavaConverters._
import scala.collection.immutable.SortedMap
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.language.postfixOps
import com.signalcollect.Graph
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.FileMode
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.OrTreeFilter
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import org.eclipse.jgit.treewalk.filter.TreeFilter
import ch.uzh.ifi.seal.lisa.core.computation._
import ch.uzh.ifi.seal.lisa.core.misc._
import ch.uzh.ifi.seal.lisa.core.public.Parser
import ch.uzh.ifi.seal.lisa.core.public.Deleted
import ch.uzh.ifi.seal.lisa.core.public.Created
import ch.uzh.ifi.seal.lisa.core.public.Modified
import ch.uzh.ifi.seal.lisa.core.public.Preexisting

import java.nio.charset.StandardCharsets

/** Extends FileRevision by also storing the Git ObjectId for each blob */
case class GitFileRevision (fileId: String, uri: String, revId: String,
                            changeType: ChangeType, objectId: ObjectId)
                            extends FileRevision

/** SourceAgent implementation for Git repositories; supports async parsing
  *
  * @param parsers a list of selected parsers
  * @param url the Git url
  * @param start start revision hash as a string
  * @param end end revision hash as a string
  * @see SourceAgent
  * @see AsyncAgent
  */
class GitAgentWithCommit(parsers: List[Parser], url: String, localDirPath: String,
               start: Option[String] = None, end: Option[String] = None,
               limit: Option[Int] = None, commit: String)
              (implicit override val uid: String)
               extends SourceAgent(parsers)(uid) with AsyncAgent with LazyLogging with RuntimeStats {

  var revisions: Option[SortedMap[Int, (Revision, Set[GitFileRevision])]] = None
  val localDir = Paths.get(localDirPath)
  val gitDir = localDir.resolve(".git")
  val builder = new FileRepositoryBuilder
  val repository = builder.setGitDir(gitDir.toFile)
    .readEnvironment
    .findGitDir
    .build

  // increase jgit cache sizes
  import org.eclipse.jgit.storage.file.WindowCacheConfig
  val cfg = new WindowCacheConfig()
  val a = cfg.getPackedGitLimit()
  val b = cfg.getPackedGitWindowSize()
  cfg.setPackedGitLimit(4096L*1024*1024)
  cfg.setDeltaBaseCacheLimit(1024*1024*1024)
  cfg.setStreamFileThreshold(32*1024*1024)
  cfg.setPackedGitWindowSize(64*1024*1024)
  cfg.setPackedGitOpenFiles(512)
  cfg.install()

    /** clones the repository. Directory at localDirPath will be overwritten! */
  override def fetch() = {
    if (Files.exists(localDir)) {
      FileTools.removeRecursively(localDir)
    }
    logger.info(s"cloning repository ${url}...")
    stats += ("git_url" -> url)
    if (Files.exists(localDir)) {
    }
    val timestamp = System.nanoTime()

    // Use C git to clone because Jgit is about 7 times slower by comparison.
    // But here's how to do it in jgit if ever necessary:
    // Git.cloneRepository
    //    .setURI(url)
    //    .setDirectory(localDir.toFile)
    //    .call
    import scala.sys.process.Process
    import scala.sys.process.ProcessIO
    import scala.io.Source
    val proc = Process(Seq("git", "clone", "-n", url, localDirPath))
    val proc2 = Process(Seq("cd", localDirPath))
    val proc3 = Process(Seq("git", "checkout", commit))
    var result = ""
    var err = ""
    val io = new ProcessIO(
      stdin  => { },
      stdout => result = Source.fromInputStream(stdout).mkString,
      stderr => err = Source.fromInputStream(stderr).mkString
    )
    val run = proc.run(io)
    if (run.exitValue != 0) {
      logger.error(s"git clone exited with ${run.exitValue}")
    }
    val run2 = proc2.run(io)
    if (run.exitValue != 0) {
      logger.error(s"dir cd exited with ${run.exitValue}")
    }    
    val run3 = proc3.run(io)
    if (run.exitValue != 0) {
      logger.error(s"git checkout exited with ${run.exitValue}")
    }
    val duration = (System.nanoTime - timestamp)/1000000000.0
    logger.info(f"clone duration: ${duration}%3.2fs")
    logger.debug("cloning finished")
    stats += ("git_clone_seconds" -> duration)
  }

  /** supports async parsing, so simply calls the generic parseAsync method */
  override def parse(graph: Graph[Any,Any]) = {
    if (!Files.exists(localDir)) {
      logger.error("ERROR Git.scala: checkoutInitial - code dir does not exist")
    }
    else {
      parseAsync(graph, this)
    }

    import scala.sys.process.Process
    import scala.sys.process.ProcessIO
    import scala.io.Source    
    val proc = Process(Seq("cd", ".."))
    var result = ""
    var err = ""
    val io = new ProcessIO(
      stdin  => { },
      stdout => result = Source.fromInputStream(stdout).mkString,
      stderr => err = Source.fromInputStream(stderr).mkString
    )
    val run = proc.run(io)

  }

  /** Removes the local clone directory */
  override def cleanup() = {
    if (Files.exists(localDir)) {
      FileTools.removeRecursively(localDir)
    }
  }

  override def getRevisionFiles(): Option[SortedMap[Int,(Revision, Set[_ <: FileRevision])]] = {
    revisions
  }

  override def readFileRevision(f: FileRevision): Option[CharBuffer] = {
    f match {
      case gf: GitFileRevision =>
        try {
          val fileLoader = repository.open(gf.objectId)
          val fileBytes = ByteBuffer.wrap(fileLoader.getCachedBytes)
          val fileChars = StandardCharsets.UTF_8.decode(fileBytes)
          Some(fileChars)
        } catch {
          case e: Throwable => None
        }
      case _ => None
    }
  }

  override def load() = {
    logger.info("reading Git metadata")
    val timestamp = System.nanoTime()

    // open the bare git repository
    val git = new Git(repository)

    // Create a RevWalk which includes all commits
    val revWalk = new RevWalk(repository)

    val endCommitId = repository.resolve(Constants.HEAD)
    val endCommit = revWalk.parseCommit(endCommitId)
    // this is called markStart because we're walking backwards in time, so
    // the end commit chosen by management is the starting point of the walk
    revWalk.markStart(endCommit)

    val c = cfg.getPackedGitLimit()
    val d = cfg.getPackedGitWindowSize()

    // start walking revisions forward in time
    revWalk.sort(RevSort.REVERSE)
    val iterator = revWalk.iterator()
    var previousRevision: Option[Revision] = None
    var fileCount: Long = 0

    import concurrent._
    import ExecutionContext.Implicits.global

    var relevant = start.isEmpty
    var relevantRevisions: ListBuffer[RevCommit] = new ListBuffer
    iterator.asScala.foreach { case commit =>
      val commitId = commit.getId.getName
      if (start.nonEmpty && commitId.startsWith(start.get)) {
        relevant = true
      }
      if (relevant) {
        relevantRevisions += commit
      }
      if (end.nonEmpty && commitId.startsWith(end.get)) {
        relevant = false
      }
    }
    relevantRevisions = limit match {
      case Some(n) => relevantRevisions.takeRight(n)
      case _ => relevantRevisions
    }
    // determine tree walk ending point (oldest, relevant commit)
    val startCommit: RevCommit = revWalk.parseCommit(relevantRevisions.head)

    val revisionFutures = Future.traverse(relevantRevisions.zipWithIndex){ case (commit, n) => Future {
      var files: ListBuffer[GitFileRevision] = new ListBuffer
      val reader = repository.newObjectReader
      val commitId = commit.getId.getName

      // create a tree walk to traverse the file tree
      val walk = new TreeWalk(reader)
      walk.setRecursive(true)

      // get the file tree contained in the current commit
      val tree = commit.getTree
      walk.addTree(tree)

      // and also get the tree of the parent commit
      val gotParent = (commit.getParentCount > 0)
      if (gotParent) {
        val parentCommit = commit.getParent(0)
        val parentTree = parentCommit.getTree
        walk.addTree(parentTree)
      }

      val suffixes = parsers.map(_.suffixes).flatten
      val filter = suffixes.size match {
        case 1 => PathSuffixFilter.create(suffixes.head)
        case _ => OrTreeFilter.create(Array[TreeFilter](
          suffixes.map(PathSuffixFilter.create(_)):_*))
      }
      walk.setFilter(filter)

      // walk the commit tree
      while (walk.next()) {
        val objectId = walk.getObjectId(0)
        val fileMode = walk.getFileMode(0)
        if (gotParent) {
          val parentFileId = walk.getObjectId(1)
          val parentFileMode = walk.getFileMode(1)

          // if the two commits aren't refering identical files...
          if (objectId != parentFileId) {
            // and if there exists a parent commit, figure out if the file...
            val changeType = if (gotParent) {
              // is missing in this commit, because it got deleted...
              if (fileMode == FileMode.MISSING) { Deleted }
              // or if it is missing from the parent commit, because it got created.
              else if (parentFileMode == FileMode.MISSING) { Created }
              // otherwise it was modified
              else { Modified }
            }
            else {
              // if there's no parent commit, it must have been created
              Created
            }
            val uri = walk.getPathString.replace(' ','_')
            files += GitFileRevision("/" + uri, uri, commitId, changeType, objectId)
            if (changeType != Deleted) { fileCount += 1 }
          }
          else {
            // if the oldest included commit is not the initial commit, we
            // also need to load all files which exist in that commit
              if (startCommit.equals(commit)) {
                val changeType = Preexisting
                val uri = walk.getPathString.replace(' ','_')
                files += GitFileRevision("/" + uri, uri, commitId, changeType, objectId)
                fileCount += 1
              }
          }
        }
        else {
          // otherwise it is the actual initial commit where all files are new
          val changeType = Created
          val uri = walk.getPathString.replace(' ','_')
          files += GitFileRevision("/" + uri, uri, commitId, changeType, objectId)
          fileCount += 1
        }
        if (walk.isSubtree()) {
          walk.enterSubtree()
        }
      }

      // extract metadata
      val authorIdent = commit.getAuthorIdent
      val authorName = authorIdent.getName
      val authorEmail = authorIdent.getEmailAddress
      val authorDate = authorIdent.getWhen
      val authorTz = authorIdent.getTimeZone
      val committerIdent = commit.getCommitterIdent
      val committerName = committerIdent.getName
      val committerEmail = committerIdent.getEmailAddress
      val committerDate = committerIdent.getWhen
      val committerTz = committerIdent.getTimeZone

      // store metadata as a new Revision
      val revision = Revision(n, commitId,
                              authorDate, authorTz, authorName, authorEmail,
                              committerDate, committerTz, committerName, committerEmail,
                              previousRevision, None)

      (n, revision, files.toSet)
    }}
    val revisionResults = Await.result(revisionFutures, 1 hour).foldLeft(SortedMap[Int, (Revision, Set[GitFileRevision])]()) { case (acc, (n, revision, files)) =>
      // update previous revision to point to current new revision as next
      previousRevision match {
        case Some(r) =>
          r.next = Some(revision)
          revision.prev = Some(r)
        case None =>
      }
      previousRevision = Some(revision)
      (acc + (n -> (revision, files)))
    }
    revisions = Some(revisionResults)

    val duration = (System.nanoTime - timestamp)/1000000000.0
    logger.info(f"git metadata extraction duration: ${duration}%3.2fs")
    stats += ("source_metadata_seconds" -> duration)
    stats += ("source_revision_count" -> revisions.get.size)
    stats += ("source_file_count" -> fileCount)
  }

}


