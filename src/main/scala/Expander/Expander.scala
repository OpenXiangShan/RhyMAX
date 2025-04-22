package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._



class Expander extends Module{
    val io = IO(new Bundle {
      val ShakeHands_io = new ShakeHands_IO
      val Operands_io = new Operands_IO
      val InsType_io = new InsType_IO
      val mtileConfig_io = new mtileConfig_IO
      val ScoreboardVisit_io = new ScoreboardVisit_IO
      val FSM_io = Flipped(new FSM_IO)

      val sigDone = Output(Bool())    //for debug
    })

    

    val subExcuteHandler = Module(new ExcuteHandler)
    val subIssueMMAU = Module(new IssueMMAU)

    /*  for debug   */
    io.sigDone := subIssueMMAU.io.IssueMMAU_Excute_io.sigDone

    /*  between Top and ExcuteHandler */
    io.ShakeHands_io <> subExcuteHandler.io.ShakeHands_io
    io.InsType_io <> subExcuteHandler.io.InsType_io
    io.Operands_io <> subExcuteHandler.io.Operands_io
    io.ScoreboardVisit_io <> subExcuteHandler.io.ScoreboardVisit_io

    /*  between Top and IssueMMAU */
    io.Operands_io <> subIssueMMAU.io.Operands_io
    io.mtileConfig_io <> subIssueMMAU.io.mtileConfig_io
    io.FSM_io <> subIssueMMAU.io.FSM_io
    subIssueMMAU.io.is_mmacc := io.InsType_io.is_mmacc

    /*  between ExcuteHandler and IssueMMAU */
    subExcuteHandler.io.IssueMMAU_Excute_io <> subIssueMMAU.io.IssueMMAU_Excute_io

}