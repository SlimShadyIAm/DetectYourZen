package slim

import ch.uzh.ifi.seal.lisa.core._
import ch.uzh.ifi.seal.lisa.module.analysis.ChildCountHelper

object NewCallNameCountersAnalysis extends Analysis with ChildCountHelper {

  final val callNames = List("namedtuple", "defaultdict", "OrderedDict", "zip",
    "deque", "Counter", "enumerate", "filter", "map",
    "izip_longest", "zip_longest", "starmap", "tee",
    "groupby")

  case class CallNameCounters(persist: Boolean, counters: Map[String,Int]) extends Data {
    def this() = this(false, Map[String,Int]())
  }

  override def start = { implicit domain => state =>
    // start on any leaf vertex
    if (leaf(state)) {
      if (state is 'name) {
        state[Literal].map.get("str") match {
          case Some(n) => if (callNames.contains(n)) {
            state ! new CallNameCountersPacket(CallNameCounters(false, Map(n -> 1)))
          } else state ! new CallNameCountersPacket(state[CallNameCounters])
          case _ => state ! new CallNameCountersPacket(state[CallNameCounters])
        }
      } else state ! new CallNameCountersPacket(state[CallNameCounters])
    }
    else state
  }

  class CallNameCountersPacket(val d: CallNameCounters) extends AnalysisPacket {

    private def mergeMaps(a: Map[String,Int], b: Map[String,Int]): Map[String,Int] = {
      (a ++ b).map { case (k, v) =>
        if (a.contains(k)) {
          (k -> (a(k) + b.getOrElse(k, 0)))
        } else (k -> v)
      }
    }

    override def collect = { implicit domain => state =>
      val old = state[CallNameCounters]
      val update = old.copy(
        counters = mergeMaps(old.counters, d.counters))

      allChildren[CallNameCounters](state)(
        incomplete = state + update,
        complete = {
          val persist = ((state is ('class, 'method, 'file)) && update.counters.size > 0)
          val finalized = (
            if (state is 'name) {
              state[Literal].map.get("str") match {
                case Some(n) => if (callNames.contains(n)) {
                  update.copy(counters = update.counters + (n -> (update.counters.getOrElse(n, 0) + 1)))
                } else update
                case _ => update
              }
            } else update
            ).copy(persist = persist)
          state + finalized ! new CallNameCountersPacket(finalized)
        }
      )

    }
  }
}

