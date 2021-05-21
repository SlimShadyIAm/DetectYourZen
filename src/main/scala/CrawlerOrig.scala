import ch.uzh.ifi.seal.lisa.core._
import ch.uzh.ifi.seal.lisa.core.public._
import ch.uzh.ifi.seal.lisa.module.parser.PythonNativeParser
import ch.uzh.ifi.seal.lisa.module.persistence.CSVPerRevisionParallelizedPersistence
import ch.uzh.ifi.seal.lisa.module.persistence.CSVStats
import ch.uzh.ifi.seal.lisa.module.analysis.PythonicAnalysisSuite
import java.nio.file._
import java.io.File
import scala.io.Source
import java.io.PrintWriter

object CrawlerOrig extends App with CSVStats {

  val datadir = args(0)

  val projects = Source.fromFile("./python_original.txt").getLines

  val gitLocalDir = "/tmp/lisa/"

  val parsers = List[Parser](PythonNativeParser)

  val analyses = PythonicAnalysisSuite

  def analyze(url: String, resultsDir: String, uid: String) = {
    if (Files.exists(Paths.get(resultsDir))) {
      println(s"$resultsDir already exists, skipping")
    }
    else {
      implicit val iuid = uid
      var persistence = new CSVPerRevisionParallelizedPersistence(resultsDir)
      var sources = new GitAgent(parsers, url, s"$gitLocalDir/$uid", None, None, Some(1))
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
  projects.zipWithIndex.foreach { case (repo, i) =>
    val uid = urlToUid(repo)
    val resultsDir = s"$datadir/$uid"
    println(s"analyzing ${i}: ${repo}, storing results in ${resultsDir}")
    analyze(repo, resultsDir, uid)
  }

}

