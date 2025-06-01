package MLU

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import chisel3.experimental.BundleLiterals._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MLU_Test extends AnyFreeSpec with Matchers {
  "MLU should PASS" in {
    simulate(new MLU) { dut =>
      dut.io.FSM_MLU_io.md.poke(1.U)

      dut.io.MLU_L2_io.Cacheline_ReadBack_io(0).data.poke("h123456789abcdef1_7384628392738261".U)
      dut.io.MLU_L2_io.Cacheline_ReadBack_io(0).id.poke(0.U)
      dut.io.MLU_L2_io.Cacheline_ReadBack_io(0).valid.poke(true.B)

      dut.io.MLU_L2_io.Cacheline_ReadBack_io(1).data.poke("h8368273648363221_8472638372672822".U)
      dut.io.MLU_L2_io.Cacheline_ReadBack_io(1).id.poke(0.U)
      dut.io.MLU_L2_io.Cacheline_ReadBack_io(1).valid.poke(true.B)

      dut.clock.step(1)

      dut.io.MLU_L2_io.Cacheline_ReadBack_io(0).valid.poke(false.B)
      dut.io.MLU_L2_io.Cacheline_ReadBack_io(1).valid.poke(false.B)


      for(i <- 0 until 8){//分8个cycle输出
        

        val addr = dut.io.RegFileAllWrite_io.addr.peek()
        println(s"addr = $addr")

        for(y <- 0 until 32){
          val setIdx = dut.io.RegFileAllWrite_io.w(y).req.bits.setIdx.peek()
          val data = dut.io.RegFileAllWrite_io.w(y).req.bits.data.head.asUInt.peek()
          val valid = dut.io.RegFileAllWrite_io.w(y).req.valid.peek().litToBoolean

          println(f"bank $y : setIdx = $setIdx , data = 0x${data.litValue}%016X , valid = $valid")
        }

        val act = dut.io.RegFileAllWrite_io.act.peek().litToBoolean
        println(s"act = $act")

        dut.clock.step(1)

      }
    }
  }
}
