package adder

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers


class AdderTest extends AnyFreeSpec with Matchers {
  "Adder should pass" in {
    simulate(new Adder) { dut =>
      dut.io.a.poke(1.U)
      dut.io.b.poke(2.U)
      dut.clock.step()
      dut.io.o.expect(3.U)
    }
  }
}



class AdderTest1 extends AnyFreeSpec with Matchers {
  "Adder should pass" in {
    simulate(new Adder) { dut =>
      dut.io.a.poke(1.U)
      dut.io.b.poke(2.U)
      dut.clock.step()
      dut.io.o.expect(1.U)
    }
  }
}
