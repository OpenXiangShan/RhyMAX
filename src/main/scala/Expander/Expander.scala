package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._



class Expander extends Module{
    val io = IO(new Bundle {
      val Uop_io = new Uop_IO
      val ScoreboardVisit_io = new ScoreboardVisit_IO


      val FSM_MMAU_io = Flipped(new FSM_MMAU_IO)

      val sigDone = Output(Bool())    //for debug
    })

    

    val subExcuteHandler = Module(new ExcuteHandler)
    val subIssueMMAU = Module(new IssueMMAU)

    /*  for debug   */
    io.sigDone := subIssueMMAU.io.IssueMMAU_Excute_io.sigDone

    /*  between Top and ExcuteHandler */
    
    io.ScoreboardVisit_io <> subExcuteHandler.io.ScoreboardVisit_io
    io.Uop_io <> subExcuteHandler.io.Uop_io

    /*  between Top and IssueMMAU */
    
    io.FSM_MMAU_io <> subIssueMMAU.io.FSM_MMAU_io
    

    /*  between ExcuteHandler and IssueMMAU */
    subExcuteHandler.io.IssueMMAU_Excute_io <> subIssueMMAU.io.IssueMMAU_Excute_io

}