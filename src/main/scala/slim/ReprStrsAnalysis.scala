package slim

import ch.uzh.ifi.seal.lisa.core._
import ch.uzh.ifi.seal.lisa.module.analysis.ChildCountHelper

object ReprStrsAnalysis extends Analysis with ChildCountHelper {
  // note that __init__ is not counted as a magic method
  final def level1Funs = Set("__repr__", "__str__")

  private def reprStrLevel(state: AnalysisState)(implicit domain: Domain): Int = {
    if (!(state is 'method)) { return 0 }
    state[Literal].map.get("str") match {
      case Some(v) if level1Funs.contains(v) => 1
      case _ => 0
    }
  }


  case class ReprStrs(persist: Boolean, level1: Int) extends Data {
    def this() = this(false, 0)
  }

  override def start = { implicit domain => state =>
    // start on any leaf vertex
    if (leaf(state)) {
      val old = state[ReprStrs]
      val (update, gotLevel) = (reprStrLevel(state) match {
        case 1 => (old.copy(level1 = old.level1 + 1), true)
        case _ => (old, false)
      })
      state + update.copy(persist = (state is 'method) && gotLevel) ! new ReprStrsPacket(update)
    } else state
  }

  class ReprStrsPacket(val d: ReprStrs) extends AnalysisPacket {
    override def collect = { implicit domain => state =>

      val old = state[ReprStrs]
      val update = old.copy(
        level1 = old.level1 + d.level1)

      allChildren[ReprStrs](state)(
        incomplete = state + update,
        complete = {
          val persist = ((state is ('class, 'method, 'file)) && (
            update.level1 != 0))
          val finalized = (reprStrLevel(state) match {
            case 1 => update.copy(level1 = update.level1 + 1)
            case _ => update
          }).copy(persist = persist)
          state + finalized ! new ReprStrsPacket(finalized)
        }
      )

    }
  }
}

