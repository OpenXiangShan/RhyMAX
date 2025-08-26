package Expander

import chisel3._
import chisel3.util._

import common._

class IssueMrelease extends Module {
  val io = IO(new Bundle {
    // To ExcuteHandler
    val IssueMrelease_Excute_io = new IssueMrelease_Excute_IO

    // To XSCore
    val toXSCore_io = Decoupled(new AmuRelease_IO)
  })


  // store mrelease inst info
  val reg_sigStart = RegInit(false.B)
  val reg_tokenRd = RegInit(0.U(Consts.TOKENREG_LEN.W))

  when (io.IssueMrelease_Excute_io.sigStart) {
    reg_sigStart := true.B
    reg_tokenRd := io.IssueMrelease_Excute_io.tokenRd
  }.otherwise {
    reg_sigStart := false.B
  }

  io.toXSCore_io.valid := reg_sigStart
  io.toXSCore_io.bits.tokenRd := reg_tokenRd

  io.IssueMrelease_Excute_io.sigDone := io.toXSCore_io.fire
}