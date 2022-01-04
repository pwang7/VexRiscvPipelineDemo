package pipeline.demo

import spinal.core.sim._

object PipelineDemoSim extends App {
  SimConfig.allOptimisation.withWave.compile(new PipelineDemo).doSim { dut =>
    dut.clockDomain.forkStimulus(10)

    dut.io.flush #= false
    dut.io.flushNext #= false
    dut.io.remove #= false
    dut.io.halt #= false
    dut.io.input.valid #= false
    dut.io.output.ready #= true
    for (idx <- 0 until 6) {
      dut.io.input.payload(idx) #= 0
    }

    fork {
      dut.clockDomain.waitSampling()
      //  dut.io.input.valid.randomize()
      for (iter <- 0 until 100) {
        dut.io.input.valid #= ((iter % 2) == 0)
        sleep(0)
        dut.io.input.payload(0) #= iter + 1
        for (idx <- 1 until dut.io.input.payload.size) {
          dut.io.input.payload(idx) #= 0 // idx + iter
        }
        if (dut.io.input.valid.toBoolean) {
          dut.clockDomain.waitSamplingWhere(
            dut.io.input.valid.toBoolean && dut.io.input.ready.toBoolean
          )
        } else {
          dut.clockDomain.waitSampling()
        }
      }
    }

    dut.clockDomain.waitSampling(10)
    dut.io.output.ready #= false
    dut.clockDomain.waitSampling(2)
    dut.io.output.ready #= true
    dut.clockDomain.waitSampling(3)
    dut.io.halt #= true
    dut.clockDomain.waitSampling(4)
    dut.io.halt #= false
    dut.clockDomain.waitSampling(5)
    dut.io.flush #= true
    dut.clockDomain.waitSampling(6)
    dut.io.flush #= false
    dut.clockDomain.waitSampling(7)
    dut.io.remove #= true
    dut.clockDomain.waitSampling(8)
    dut.io.remove #= false
    dut.clockDomain.waitSampling(9)
    dut.io.flushNext #= true
    dut.clockDomain.waitSampling(10)
    dut.io.flushNext #= false

    dut.clockDomain.waitSampling(10)
  // simSuccess()
  }
}
