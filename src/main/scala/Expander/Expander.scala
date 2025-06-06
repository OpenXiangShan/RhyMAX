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
      val FSM_MLU_io = new FSM_MLU_IO

      val sigDone = Output(Bool())    //for debug
    })

    

    val subExcuteHandler = Module(new ExcuteHandler)
    val subIssueMMAU = Module(new IssueMMAU)
    val subIssueMLU = Module(new IssueMLU)

    /*  for debug   */
    io.sigDone := subIssueMMAU.io.IssueMMAU_Excute_io.sigDone
    // io.sigDone := subIssueMLU.io.IssueMLU_Excute_io.sigDone

//debug
printf(p"[ExcuteHandler] sigDone = ${io.sigDone}\n") 

    /*  between Top and ExcuteHandler */
    io.ScoreboardVisit_io <> subExcuteHandler.io.ScoreboardVisit_io
    io.Uop_io <> subExcuteHandler.io.Uop_io


    /*  between Top and IssueMMAU */
    io.FSM_MMAU_io <> subIssueMMAU.io.FSM_MMAU_io


    /*  between Top and IssueMLU */
    io.FSM_MLU_io <> subIssueMLU.io.FSM_MLU_io
    

    /*  between ExcuteHandler and IssueMMAU */
    subExcuteHandler.io.IssueMMAU_Excute_io <> subIssueMMAU.io.IssueMMAU_Excute_io

    /*  between ExcuteHandler and IssueMLU */
    subExcuteHandler.io.IssueMLU_Excute_io <> subIssueMLU.io.IssueMLU_Excute_io


    /*  between IssueMLU and IssueMMAU */
    //nothing





}