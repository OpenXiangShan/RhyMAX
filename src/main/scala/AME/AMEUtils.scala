package AME

import chisel3._
import chisel3.util._

import utility.sram._
import common._
import RegFile._
import MMAU._



object connectPort{ //用于将MMAU的有关信号连接到RegFile的读写端口
  
  def toTrReadPort(
    regFilePort: RegFileTrReadIO,
    addr: UInt,
    mmauAddr: Vec[UInt],
    mmauData: Vec[UInt]
  ): Unit = {
    regFilePort.addr := addr
    regFilePort.act := true.B
    for (i <- 0 until mmauAddr.length) {
      regFilePort.r(i).req.bits.setIdx := mmauAddr(i)
      regFilePort.r(i).req.valid := true.B
      mmauData(i) := regFilePort.r(i).resp.data.head
    }
  }

  def toAccReadPort(
    regFilePort: RegFileAccReadIO,
    addr: UInt,
    mmauAddr: Vec[UInt],
    mmauData: Vec[UInt]
  ): Unit = {
    regFilePort.addr := addr
    regFilePort.act := true.B
    for (i <- 0 until mmauAddr.length) {
      regFilePort.r(i).req.bits.setIdx := mmauAddr(i)
      regFilePort.r(i).req.valid := true.B
      mmauData(i) := regFilePort.r(i).resp.data.head
    }
  }

  
  def toAccWritePort(
    regFilePort: RegFileAccWriteIO,
    addr: UInt,
    mmauAddr: Vec[UInt],
    mmauData: Vec[UInt],
    enable: Vec[Bool]
  ): Unit = {
    regFilePort.addr := addr
    regFilePort.act := true.B
    for (i <- 0 until mmauAddr.length) {
      regFilePort.w(i).req.bits.setIdx := mmauAddr(i)
      regFilePort.w(i).req.valid := enable(i)
      regFilePort.w(i).req.bits.data.head := mmauData(i)
    }
  }
}