package slim

import ch.uzh.ifi.seal.lisa.core._
import ch.uzh.ifi.seal.lisa.module.analysis._

/* Python native analyses only sure to be compatible if run on Python 3.6.0 */
object NewPythonicAnalysisSuite extends AnalysisSuite {
  val phases: List[List[Analysis]] = List(
    List(
      ChildCountAnalysis,
      PythonNativeNameAnalysis
    ),
    List(
      ReprStrsAnalysis,
      NewMagicMethodsAnalysis,
      NodeTypeCountersAnalysis,
      NewCallNameCountersAnalysis,
      NestedFunctionAnalysis,
      MethodCountAnalysis,
      ClassCountAnalysis,
      DecoratorAnalysis
    ),
    List(
      NestedFunctionCountAnalysis
    )
  )
}

