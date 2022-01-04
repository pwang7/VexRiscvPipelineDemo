package pipeline.demo

import spinal.core._
import spinal.lib._

import vexriscv._
import vexriscv.plugin._

class PipelineDemo extends Component with Pipeline {
  type Tp = PipelineDemo

  val io = new Bundle {
    val flush = in(Bool())
    val flushNext = in(Bool())
    val halt = in(Bool())
    val remove = in(Bool())
    val input = slave(Stream(Vec(UInt(32 bits), size = 10)))
    val output = master(Stream(UInt(32 bits)))
    val delayOut = master(Flow(UInt(32 bits)))
  }

  def newStage(): Stage = { val s = new Stage; stages += s; s }
  val stage1 = newStage()
  val stage2 = newStage()
  val stage3 = newStage()
  val stage4 = newStage()

  object INPUT extends Stageable(Vec(UInt(32 bits), size = 10))

  plugins += new DebugPlugin
  //stages.foreach(s => s.arbitration.flushIt.setWhen(io.halt))
}

class DebugPlugin extends Plugin[PipelineDemo] {
  object SUM0 extends Stageable(UInt(32 bits))
  object SUM1 extends Stageable(UInt(32 bits))
  object SUM2 extends Stageable(UInt(32 bits))
  object SUM3 extends Stageable(UInt(32 bits))
  object SUM4 extends Stageable(UInt(32 bits))
  object SUM5 extends Stageable(UInt(32 bits))
  object SUM6 extends Stageable(UInt(32 bits))
  object SUM7 extends Stageable(UInt(32 bits))
  object SUM8 extends Stageable(UInt(32 bits))

  override def build(pd: PipelineDemo): Unit = {
    import pd._

//    pd plug new Area {
//      import pd._
//    }

    stage1 plug new Area {

      import stage1._

      arbitration.isValid := io.input.valid
      io.input.ready := arbitration.isFiring
      insert(INPUT) := io.input.payload
      insert(SUM0) := input(INPUT)(0) + input(INPUT)(1)
      insert(SUM1) := input(INPUT)(2) + input(INPUT)(3)
      insert(SUM2) := input(INPUT)(4) + input(INPUT)(5)
      insert(SUM3) := input(INPUT)(6) + input(INPUT)(7)
      insert(SUM4) := input(INPUT)(8) + input(INPUT)(9)
//      insert(SUM0) := io.input.payload(0) + io.input.payload(1)
//      insert(SUM1) := io.input.payload(2) + io.input.payload(3)
//      insert(SUM2) := io.input.payload(4) + io.input.payload(5)
//      insert(SUM3) := io.input.payload(6) + io.input.payload(7)
//      insert(SUM4) := io.input.payload(8) + io.input.payload(9)
    }

    stage2 plug new Area {

      import stage2._

//      assert(
//        assertion = input(EN) === arbitration.isValid,
//        message =
//          L"EN@stage2=${input(EN)} =/= stage2.isValid=${arbitration.isValid}",
//        severity = FAILURE
//      )
//      arbitration.flushIt.setWhen(io.flush)
//      arbitration.haltItself.setWhen(io.halt)
//      when(io.halt) {
//        output(EN) := False
//      }
      arbitration.removeIt.setWhen(io.remove)
      // arbitration.haltItself.setWhen(io.halt)
      insert(SUM5) := input(SUM0) + input(SUM1)
      insert(SUM6) := input(SUM2) + input(SUM3)
    }

    stage3 plug new Area {

      import stage3._

      //      assert(
      //        assertion = input(EN) === arbitration.isValid,
      //        message =
      //          L"EN@stage2=${input(EN)} =/= stage2.isValid=${arbitration.isValid}",
      //        severity = FAILURE
      //      )
      arbitration.flushIt.setWhen(io.flush)
      arbitration.flushNext.setWhen(io.flushNext)
      arbitration.haltItself.setWhen(io.halt)
      //      when(io.halt) {
      //        output(EN) := False
      //      }
      // arbitration.flushIt.setWhen(io.halt)
      // arbitration.haltItself.setWhen(io.halt)
      insert(SUM7) := (input(SUM5) + input(SUM6)) >> U(0, 1 bit)
    }

    stage4 plug new Area {

      import stage4._

//      assert(
//        assertion = input(EN) === arbitration.isValid,
//        message =
//          L"EN@stage3=${input(EN)} =/= stage3.isValid=${arbitration.isValid}",
//        severity = FAILURE
//      )
      // arbitration.haltItself.setWhen(io.halt)
      insert(SUM8) := input(SUM4) + input(SUM7)

      io.output.valid := arbitration.isValid
      io.output.payload := output(SUM8)
      arbitration.haltItself.setWhen(!io.output.ready)

      val previousValid = RegInit(False)
      val previousOut = Reg(UInt(32 bits)) init (0)
      when(io.output.fire) {
        previousValid := io.output.valid
        previousOut := io.output.payload
      }
      io.delayOut.valid := previousValid
      io.delayOut.payload := previousOut + io.output.payload
    }
  }
}
