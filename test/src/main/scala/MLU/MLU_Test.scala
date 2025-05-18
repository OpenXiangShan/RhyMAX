package MLU

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import chisel3.experimental.BundleLiterals._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MLU_Test extends AnyFreeSpec with Matchers {
  "MLU should PASS" in {
    simulate(new MLU) { dut =>
      dut.clock.step(1)
    }
  }
}
