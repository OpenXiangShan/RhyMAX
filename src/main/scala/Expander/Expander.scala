package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._
import utility.GTimer



class Expander extends Module{
    val io = IO(new Bundle {
      val Uop_io = new Uop_IO
      val ScoreboardVisit_io = new ScoreboardVisit_IO


      val FSM_MMAU_io = Flipped(new FSM_MMAU_IO)
      val FSM_MLU_io = new FSM_MLU_IO
      val FSM_MSU_io = new FSM_MSU_IO

      val sigDone = Output(Bool())    //for debug
    })

    

    val subExcuteHandler = Module(new ExcuteHandler)
    val subIssueMMAU = Module(new IssueMMAU)
    val subIssueMLU = Module(new IssueMLU)
    val subIssueMSU = Module(new IssueMSU)

    /*  for debug   */
    // io.sigDone := subIssueMMAU.io.IssueMMAU_Excute_io.sigDone
    // io.sigDone := subIssueMLU.io.IssueMLU_Excute_io.sigDone
    io.sigDone := subIssueMSU.io.IssueMSU_Excute_io.sigDone

//debug
//printf(p"[ExcuteHandler] sigDone = ${io.sigDone}\n") 

    /*  between Top and ExcuteHandler */
    io.ScoreboardVisit_io <> subExcuteHandler.io.ScoreboardVisit_io
    io.Uop_io <> subExcuteHandler.io.Uop_io


    /*  between Top and IssueMMAU */
    io.FSM_MMAU_io <> subIssueMMAU.io.FSM_MMAU_io


    /*  between Top and IssueMLU */
    io.FSM_MLU_io <> subIssueMLU.io.FSM_MLU_io

    /*  between Top and IssueMSU */
    io.FSM_MSU_io <> subIssueMSU.io.FSM_MSU_io
    

    /*  between ExcuteHandler and IssueMMAU */
    subExcuteHandler.io.IssueMMAU_Excute_io <> subIssueMMAU.io.IssueMMAU_Excute_io

    /*  between ExcuteHandler and IssueMLU */
    subExcuteHandler.io.IssueMLU_Excute_io <> subIssueMLU.io.IssueMLU_Excute_io

    /*  between ExcuteHandler and IssueMSU */
    subExcuteHandler.io.IssueMSU_Excute_io <> subIssueMSU.io.IssueMSU_Excute_io


    /*  between IssueMMAU and IssueMLU  */
    //nothing

    /*  between IssueMMAU and IssueMSU  */
    //nothing

    /*  between IssueMLU and IssueMSU  */
    //nothing





    /**
      * Logging
      */

    val timer = GTimer()

    when (subIssueMMAU.io.IssueMMAU_Excute_io.sigStart) {
      val in_ms1 = subIssueMMAU.io.IssueMMAU_Excute_io.in_ms1
      val in_ms2 = subIssueMMAU.io.IssueMMAU_Excute_io.in_ms2
      val in_md  = subIssueMMAU.io.IssueMMAU_Excute_io.in_md
      val mtilem = subIssueMMAU.io.IssueMMAU_Excute_io.mtilem
      val mtilen = subIssueMMAU.io.IssueMMAU_Excute_io.mtilen
      val mtilek = subIssueMMAU.io.IssueMMAU_Excute_io.mtilek

      printf(p"[cycle=${timer}][MMAU] start: in_ms1=${in_ms1}, in_ms2=${in_ms2}, in_md=${in_md}, " +
             p"mtilem=${mtilem}, mtilen=${mtilen}, mtilek=${mtilek}\n")
    }

    when (subIssueMLU.io.IssueMLU_Excute_io.sigStart) {
      val is_mlbe8 = subIssueMLU.io.IssueMLU_Excute_io.is_mlbe8
      val is_mlae8 = subIssueMLU.io.IssueMLU_Excute_io.is_mlae8
      val is_mlce32 = subIssueMLU.io.IssueMLU_Excute_io.is_mlce32
      val rs1 = subIssueMLU.io.IssueMLU_Excute_io.rs1
      val rs2 = subIssueMLU.io.IssueMLU_Excute_io.rs2
      val md = subIssueMLU.io.IssueMLU_Excute_io.in_md
      val mtilem = subIssueMLU.io.IssueMLU_Excute_io.mtilem
      val mtilen = subIssueMLU.io.IssueMLU_Excute_io.mtilen
      val mtilek = subIssueMLU.io.IssueMLU_Excute_io.mtilek

      printf(p"[cycle=${timer}][MLU] start: is_mlbe8=${is_mlbe8}, is_mlae8=${is_mlae8}, is_mlce32=${is_mlce32}, " +
             p"rs1=${rs1}, rs2=${rs2}, md=${md}, mtilem=${mtilem}, mtilen=${mtilen}, mtilek=${mtilek}\n")
    }

    when (subIssueMSU.io.IssueMSU_Excute_io.sigStart) {
      val is_msce32 = subIssueMSU.io.IssueMSU_Excute_io.is_msce32
      val rs1 = subIssueMSU.io.IssueMSU_Excute_io.rs1
      val rs2 = subIssueMSU.io.IssueMSU_Excute_io.rs2
      val md = subIssueMSU.io.IssueMSU_Excute_io.in_md
      val mtilem = subIssueMSU.io.IssueMSU_Excute_io.mtilem
      val mtilen = subIssueMSU.io.IssueMSU_Excute_io.mtilen
      val mtilek = subIssueMSU.io.IssueMSU_Excute_io.mtilek

      printf(p"[cycle=${timer}][MSU] start: is_msce32=${is_msce32}, rs1=${rs1}, rs2=${rs2}, md=${md}, " +
             p"mtilem=${mtilem}, mtilen=${mtilen}, mtilek=${mtilek}\n")
    }

    when (subIssueMMAU.io.IssueMMAU_Excute_io.sigDone) {
      printf(p"[cycle=${timer}][MMAU] done"
             + p" out_ms1=${subIssueMMAU.io.IssueMMAU_Excute_io.out_ms1},"
             + p" out_ms2=${subIssueMMAU.io.IssueMMAU_Excute_io.out_ms2},"
             + p" out_md=${subIssueMMAU.io.IssueMMAU_Excute_io.out_md}\n")
    }

    when (subIssueMLU.io.IssueMLU_Excute_io.sigDone) {
      printf(p"[cycle=${timer}][MLU] done out_md=${subIssueMLU.io.IssueMLU_Excute_io.out_md}\n")
    }

    when (subIssueMSU.io.IssueMSU_Excute_io.sigDone) {
      printf(p"[cycle=${timer}][MSU] done out_md=${subIssueMSU.io.IssueMSU_Excute_io.out_md}\n")
    }
}