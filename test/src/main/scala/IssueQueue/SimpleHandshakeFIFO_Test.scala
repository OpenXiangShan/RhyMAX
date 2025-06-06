package IssueQueen

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class SimpleHandshakeFIFO_Test extends AnyFreeSpec with Matchers {

  "SimpleHandshakeFIFO should PASS" in {
    simulate(new SimpleHandshakeFIFO(depth = 4, width = 16)) { dut =>
      val inputData = Seq(0x1111, 0x2222, 0x3333, 0x4444 , 0x5555)
      var pushIdx = 0

      // ------- Push Phase -------
      dut.io.deq_ready.poke(false.B) // 禁止读取

      dut.clock.step(1) //使DUT进入工作状态，否则初始状态enq_ready为false

      while (dut.io.enq_ready.peek().litToBoolean) {
        dut.io.enq_valid.poke(true.B)
        dut.io.enq_bits.poke(inputData(pushIdx).U)
        pushIdx += 1

        dut.clock.step(1)

println(s"push one")
      }

      dut.io.enq_valid.poke(false.B)

      // ------- Pop Phase -------
      dut.io.deq_ready.poke(true.B)

      var popCount = 0
      
      while (dut.io.deq_valid.peek().litToBoolean) {
        
        val value = dut.io.deq_bits.peek()
        // val value = "h9999".U
        println(f"out = 0x${value.litValue}%04X")

        // value mustBe inputData(popCount)
        popCount += 1
        
        dut.clock.step(1)
      }

      // 验证 FIFO 已空
      dut.io.deq_valid.expect(false.B) 
    }
  }
}


