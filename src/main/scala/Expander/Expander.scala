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
    val loggerMMAU = new UopLogger("MMAU", subIssueMMAU.io.IssueMMAU_Excute_io)
    val loggerMLA = new UopLogger("MLA", subIssueMLU.io.IssueMLU_Excute_io)
    val loggerMLB = new UopLogger("MLB", subIssueMLU.io.IssueMLU_Excute_io)
    val loggerMLC = new UopLogger("MLC", subIssueMLU.io.IssueMLU_Excute_io)
    val loggerMSU = new UopLogger("MSU", subIssueMSU.io.IssueMSU_Excute_io)

    loggerMMAU.logStart(_.sigStart)
    loggerMLA.logStart(b => b.sigStart && b.is_mlae8)
    loggerMLB.logStart(b => b.sigStart && b.is_mlbe8)
    loggerMLC.logStart(b => b.sigStart && b.is_mlce32)
    loggerMSU.logStart(_.sigStart)

    loggerMMAU.logDone(_.sigDone)
    loggerMLA.logDone(b => b.sigDone && b.out_is_mlae8)
    loggerMLB.logDone(b => b.sigDone && b.out_is_mlbe8)
    loggerMLC.logDone(b => b.sigDone && b.out_is_mlce32)
    loggerMSU.logDone(_.sigDone)
}


class UopLogger[T <: Bundle](name: String, info: T) {
  val startCount = RegInit(0.U(32.W))
  val doneCount = RegInit(0.U(32.W))
  val timer = GTimer()

  val start_prefix = cf"[cycle=${timer}][${name}][$startCount] start: "
  val done_prefix = cf"[cycle=${timer}][${name}][$doneCount] done: "

  val info_start_cf = info match {
    case info: IssueMMAU_Excute_IO =>
      cf"in_ms1=${info.in_ms1}, in_ms2=${info.in_ms2}, in_md=${info.in_md}, " +
      cf"mtilem=${info.mtilem}, mtilen=${info.mtilen}, mtilek=${info.mtilek}\n"
    case info: IssueMLU_Excute_IO =>
      cf"rs1=${info.rs1}, rs2=${info.rs2}, md=${info.in_md}, " +
      cf"mtilem=${info.mtilem}, mtilen=${info.mtilen}, mtilek=${info.mtilek}\n"
    case info: IssueMSU_Excute_IO =>
      cf"rs1=${info.rs1}, rs2=${info.rs2}, md=${info.in_md}, " +
      cf"mtilem=${info.mtilem}, mtilen=${info.mtilen}, mtilek=${info.mtilek}\n"
    case _ =>
      require(false, "UopLogger: unknown info type")
      cf""
  }

  val info_done_cf = info match {
    case info: IssueMMAU_Excute_IO =>
      cf"out_ms1=${info.out_ms1}, out_ms2=${info.out_ms2}, out_md=${info.out_md}\n"
    case info: IssueMLU_Excute_IO =>
      cf"out_md=${info.out_md}\n"
    case info: IssueMSU_Excute_IO =>
      cf"\n"
    case _ =>
      require(false, "UopLogger: unknown info type")
      cf""
  }

  def logStart(cond: (T => Bool)): Unit = {
    if (!Consts.LOGGING) {
      return;
    }

    when (cond(info)) {
      startCount := startCount + 1.U
      printf(start_prefix + info_start_cf)
    }
  }

  def logDone(cond: (T => Bool)): Unit = {
    if (!Consts.LOGGING) {
      return;
    }

    when (cond(info)) {
      doneCount := doneCount + 1.U
      printf(done_prefix + info_done_cf)
    }
  }
}