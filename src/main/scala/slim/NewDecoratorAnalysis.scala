package slim

import ch.uzh.ifi.seal.lisa.core._
import ch.uzh.ifi.seal.lisa.module.analysis.ChildCountHelper

object NewDecoratorAnalysis extends Analysis with ChildCountHelper {

  final val decoratorNames = List("classmethod", "staticmethod", "property", "total_ordering")

  case class DecoratorCounters(persist: Boolean, counters: Map[String,Int]) extends Data {
    def this() = this(false, Map[String,Int]())
  }

  override def start = { implicit domain => state =>
    // start on any leaf vertex
    if (leaf(state)) {
      if (state is 'decorator) {
        state[Literal].map.get("str") match {
          case Some(n) => if (decoratorNames.contains(n)) {
            state ! new DecoratorCountersPacket(DecoratorCounters(false, Map(n -> 1)))
          } else state ! new DecoratorCountersPacket(state[DecoratorCounters])
          case _ => state ! new DecoratorCountersPacket(state[DecoratorCounters])
        }
      } else state ! new DecoratorCountersPacket(state[DecoratorCounters])
    }
    else state
  }

  class DecoratorCountersPacket(val d: DecoratorCounters) extends AnalysisPacket {

    private def mergeMaps(a: Map[String,Int], b: Map[String,Int]): Map[String,Int] = {
      (a ++ b).map { case (k, v) =>
        if (a.contains(k)) {
          (k -> (a(k) + b.getOrElse(k, 0)))
        } else (k -> v)
      }
    }

    override def collect = { implicit domain => state =>
      val old = state[DecoratorCounters]
      val update = old.copy(
        counters = mergeMaps(old.counters, d.counters))

      allChildren[DecoratorCounters](state)(
        incomplete = state + update,
        complete = {
          val persist = ((state is ('class, 'method, 'file)) && update.counters.size > 0)
          val finalized = (
            if (state is 'decorator) {
              state[Literal].map.get("str") match {
                case Some(n) => if (decoratorNames.contains(n)) {
                  update.copy(counters = update.counters + (n -> (update.counters.getOrElse(n, 0) + 1)))
                } else update
                case _ => update
              }
            } else update
          ).copy(persist = persist)
          state + finalized ! new DecoratorCountersPacket(finalized)
        }
      )

    }
  }
}

