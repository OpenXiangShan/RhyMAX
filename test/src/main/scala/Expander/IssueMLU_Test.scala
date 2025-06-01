package Expander

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers


import common._




class IssueMLU_Test extends AnyFreeSpec with Matchers {

  "IssueMLU should PASS" in {
    simulate(new IssueMLU) { dut =>
    
      dut.io.IssueMLU_Excute_io.sigStart.poke(true.B)
      dut.io.IssueMLU_Excute_io.is_mlbe8.poke(true.B)
      dut.io.IssueMLU_Excute_io.rs1.poke(0.U)   //baseaddr
      dut.io.IssueMLU_Excute_io.rs2.poke(100.U) //stride
      dut.io.IssueMLU_Excute_io.in_md.poke(2.U)
      dut.io.IssueMLU_Excute_io.mtilem.poke(64.U)
      dut.io.IssueMLU_Excute_io.mtilen.poke(56.U)   //row=7
      dut.io.IssueMLU_Excute_io.mtilek.poke(192.U)  //col=3

      dut.clock.step(1)

      dut.io.IssueMLU_Excute_io.sigStart.poke(false.B)

    //   dut.clock.step(5)

      while(!dut.io.IssueMLU_Excute_io.sigDone.peek().litToBoolean){
        dut.clock.step(1)
        // for(i <- 0 until 8){
        //     println(s"cacheline $i : addr = ${dut.io.FSM_MLU_io.Cacheline_Read_io(i).addr.peek().litValue} , id = ${dut.io.FSM_MLU_io.Cacheline_Read_io(i).id.peek().litValue}")
        // }

        // println()
      }
    }
  }
}

