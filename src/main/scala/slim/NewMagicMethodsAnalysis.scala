package slim

import ch.uzh.ifi.seal.lisa.core._
import ch.uzh.ifi.seal.lisa.module.analysis.ChildCountHelper

object NewMagicMethodsAnalysis extends Analysis with ChildCountHelper {
  // note that __init__ is not counted as a magic method
  final def level1Funs = Set(
    "__new__", "__del__", "__cmp__", "__eq__", "__ne__", "__lt__", "__gt__",
    "__le__", "__ge__", "__add__", "__sub__", "__mul__", "__div__", "__and__",
    "__or__", "__int__", "__long__", "__float__", "__str__", "__repr__",
    "__bool__", "__call__", "__enter__", "__exit__", "__get__", "__set__",
    "__delete__", "__len__", "__getitem__", "__setitem__", "__delitem__",
    "__getattr__", "__setattr__", "__delattr__", "__iter__", "__contains__")
  final def level2Funs = Set(
    "__floordiv__", "__truediv__", "__mod__", "__xor__", "__pow__",
    "__radd__", "__rsub__", "__rdiv__", "__iadd__", "__isub__", "__idiv__",
    "__rand__", "__ror__", "__iand__", "__ior__", "__hex__", "__complex__",
    "__oct__", "__unicode__", "__format__", "__hash__", "__dir__", "__copy__",
    "__deepcopy__", "__getattribute__", "__reversed__", "__getstate__",
    "__setstate__", "__reduce__", "__pos__", "__neg__", "__abs__",
    "__invert__", "__round__")
  final def level3Funs = Set(
    "__divmod__", "__lshift__", "__rshift__", "__rfloordiv__", "__rtruediv__",
    "__rmod__", "__rdivmod__", "__rpow__", "__rlshift__", "__rrshift__",
    "__rxor__", "__ifloordiv__", "__itruediv__", "__imod__", "__idivmod__",
    "__ipow__", "__ilshift__", "__irshift__", "__ixor__", "__index__",
    "__trunc__", "__coerce__", "__sizeof__", "__bytes__", "__nonzero__",
    "__instancecheck__", "__subclasscheck__", "__missing__",
    "__getinitargs__", "__getnewargs__", "__reduce__", "__ex__", "__floor__",
    "__ceil__")

  private def magicLevel(state: AnalysisState)(implicit domain: Domain): Int = {
    if (!(state is 'method)) { return 0 }
    state[Literal].map.get("str") match {
      case Some(v) if level1Funs.contains(v) => 1
      case Some(v) if level2Funs.contains(v) => 2
      case Some(v) if level3Funs.contains(v) => 3
      case _ => 0
    }
  }


  case class MagicMethods(persist: Boolean, level1: Int, level2: Int, level3: Int) extends Data {
    def this() = this(false, 0, 0, 0)
  }

  override def start = { implicit domain => state =>
    // start on any leaf vertex
    if (leaf(state)) {
      val old = state[MagicMethods]
      val (update, gotLevel) = (magicLevel(state) match {
        case 1 => (old.copy(level1 = old.level1 + 1), true)
        case 2 => (old.copy(level2 = old.level2 + 1), true)
        case 3 => (old.copy(level3 = old.level3 + 1), true)
        case _ => (old, false)
      })
      state + update.copy(persist = (state is 'method) && gotLevel) ! new MagicMethodsPacket(update)
    } else state
  }

  class MagicMethodsPacket(val d: MagicMethods) extends AnalysisPacket {
    override def collect = { implicit domain => state =>

      val old = state[MagicMethods]
      val update = old.copy(
        level1 = old.level1 + d.level1,
        level2 = old.level2 + d.level2,
        level3 = old.level3 + d.level3)

      allChildren[MagicMethods](state)(
        incomplete = state + update,
        complete = {
          val persist = ((state is ('class, 'method, 'file)) && (
            update.level1 != 0 || update.level2 != 0 || update.level3 != 0))
          val finalized = (magicLevel(state) match {
            case 1 => update.copy(level1 = update.level1 + 1)
            case 2 => update.copy(level2 = update.level2 + 1)
            case 3 => update.copy(level3 = update.level3 + 1)
            case _ => update
          }).copy(persist = persist)
          state + finalized ! new MagicMethodsPacket(finalized)
        }
      )

    }
  }
}

