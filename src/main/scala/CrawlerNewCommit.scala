import ch.uzh.ifi.seal.lisa.core._
import ch.uzh.ifi.seal.lisa.core.public._
import ch.uzh.ifi.seal.lisa.module.parser.PythonNativeParser
import ch.uzh.ifi.seal.lisa.module.persistence.{CSVPerRevisionParallelizedPersistence, CSVStats}
import slim._
import java.io.{File, PrintWriter}
import java.nio.file._
import scala.io.Source

object CrawlerNewCommit extends App with CSVStats {

  val datadir = args(0)

  val projects = Source.fromFile("./python-commit.txt").getLines

  val gitLocalDir = "/tmp/lisa/"

  val parsers = List[Parser](PythonNativeParser)

  val analyses = NewPythonicAnalysisSuite

  def analyze(url: String, resultsDir: String, uid: String, commit: String) = {
    if (Files.exists(Paths.get(resultsDir))) {
      println(s"$resultsDir already exists, skipping")
    }
    else {
      implicit val iuid = uid
      var persistence = new CSVPerRevisionParallelizedPersistence(resultsDir)
      var sources = new GitAgentWithCommit(parsers, url, s"$gitLocalDir/$uid", None, None, Some(1), commit)
      var c = new LisaComputation(sources, analyses, persistence)
      try {
        c.execute
      }
      catch {
        case e: OutOfMemoryError =>
          (new File(resultsDir)).mkdirs
          storeStats(s"${resultsDir}/stats")
          new PrintWriter(s"$resultsDir/error") { write("OOM"); close }
      }
      finally {
        // this is actually needed to clear the memory from any left-over data.
        // without it, the crawler crashes with an OOM after a few computations
        c = null
        sources = null
        persistence = null
        System.gc()
      }
    }
  }

  def urlToUid(url: String): String = {
     url.dropWhile(_ != '.').dropWhile(_ != '/').drop(1).replaceAll("/", "_").replaceAll(".git", "")
  }

  (new File(datadir)).mkdirs
  projects.zipWithIndex.foreach { case (repository, i) =>
    val repo = repository.split(" ")(0)
    val commit = repository.split(" ")(1)
   
    val uid = urlToUid(repo)
    val resultsDir = s"$datadir/$uid"
    println(s"analyzing ${i}: ${repo}, commit ${commit} storing results in ${resultsDir}")
    analyze(repo, resultsDir, uid, commit)
  }

}

