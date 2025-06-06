package IssueQueen

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers




class IssueQueen_Test extends AnyFreeSpec with Matchers {

  "IssueQueen should PASS" in {
    simulate(new IssueQueen) { dut =>
      val uopSeq = Seq(
        (0, 1, 3, 1, 2, true,  false, 3, 4, 5),
        (4, 2, 5, 6, 7, false, true,  8, 9, 10),
        (2, 1, 3, 1, 2, true,  false, 3, 4, 5),
        (3, 2, 5, 6, 7, false, true,  8, 9, 10),
        (6, 1, 3, 1, 2, true,  false, 3, 4, 5),
        (8, 2, 5, 6, 7, false, true,  8, 9, 10),
      )

      var pushIdx = 0

      // ---------- PUSH ----------
      dut.io.Uop_Out_io.ShakeHands_io.ready.poke(false.B)
      dut.clock.step(1) //使DUT进入工作状态，否则初始状态enq_ready为false

      while (dut.io.Uop_In_io.ShakeHands_io.ready.peek().litToBoolean) {
        val (ms1, ms2, md, rs1, rs2, is_mmacc, is_mlbe8, m, n, k) = uopSeq(pushIdx)

        dut.io.Uop_In_io.Operands_io.ms1.poke(ms1.U)
        dut.io.Uop_In_io.Operands_io.ms2.poke(ms2.U)
        dut.io.Uop_In_io.Operands_io.md.poke(md.U)
        dut.io.Uop_In_io.Operands_io.rs1.poke(rs1.U)
        dut.io.Uop_In_io.Operands_io.rs2.poke(rs2.U)

        dut.io.Uop_In_io.InsType_io.is_mmacc.poke(is_mmacc.B)
        dut.io.Uop_In_io.InsType_io.is_mlbe8.poke(is_mlbe8.B)

        dut.io.Uop_In_io.mtileConfig_io.mtilem.poke(m.U)
        dut.io.Uop_In_io.mtileConfig_io.mtilen.poke(n.U)
        dut.io.Uop_In_io.mtileConfig_io.mtilek.poke(k.U)

        dut.io.Uop_In_io.ShakeHands_io.valid.poke(true.B)

        println(s"push uop $pushIdx")
        pushIdx += 1
        dut.clock.step(1)
      }

      dut.io.Uop_In_io.ShakeHands_io.valid.poke(false.B)

      // ---------- POP ----------
      dut.io.Uop_Out_io.ShakeHands_io.ready.poke(true.B)

      var popCount = 0
      while (dut.io.Uop_Out_io.ShakeHands_io.valid.peek().litToBoolean) {
        val out = dut.io.Uop_Out_io

        val ms1 = out.Operands_io.ms1.peek().litValue
        val ms2 = out.Operands_io.ms2.peek().litValue
        val md = out.Operands_io.md.peek().litValue
        val rs1 = out.Operands_io.rs1.peek().litValue
        val rs2 = out.Operands_io.rs2.peek().litValue

        val is_mmacc = out.InsType_io.is_mmacc.peek().litToBoolean
        val is_mlbe8 = out.InsType_io.is_mlbe8.peek().litToBoolean

        val m = out.mtileConfig_io.mtilem.peek().litValue
        val n = out.mtileConfig_io.mtilen.peek().litValue
        val k = out.mtileConfig_io.mtilek.peek().litValue

        println(f"$ms1, $ms2, $md, $rs1, $rs2, $is_mmacc, $is_mlbe8, $m, $n, $k")

        popCount += 1
        dut.clock.step(1)
      }

      // ---------- DONE ----------
      dut.io.Uop_Out_io.ShakeHands_io.valid.expect(false.B)




    }
  }
}
