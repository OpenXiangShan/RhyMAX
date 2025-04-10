package AME

import chisel3._
import chisel3.util._

import utility.sram._
import common._
import RegFile._
import MMAU._



class AME extends Module {
  val io = IO(new Bundle {
    val sigStart = Input(Bool())    // 启动信号
    val writeTr = new RegFileTrWriteIO
    val writeAcc = new RegFileAccWriteIO
    val readTr = new RegFileTrReadIO
    val readAcc = new RegFileAccReadIO
    val sigDone = Output(Bool())    // 结束信号
  })

  val subMMAU = Module(new MMAU)
  val subRegFile = Module(new RegFile)
  subRegFile.io := DontCare

  // 连接顶层信号
  subMMAU.io.sigStart := io.sigStart
  io.sigDone := subMMAU.io.sigDone

  // 连接顶层寄存器文件接口
  io.writeTr <> subRegFile.io.writeTr(2)
  io.writeAcc <> subRegFile.io.writeAcc(2)
  io.readTr <> subRegFile.io.readTr(2)
  io.readAcc <> subRegFile.io.readAcc(2)

  
  //read A(Tr0),using subRegFile.io.readTr(0)
  connectPort.toTrReadPort(
    subRegFile.io.readTr(0),
    0.U,
    subMMAU.io.addrReadA,
    subMMAU.io.vecA
  )

  //read B(Tr1),using subRegFile.io.readTr(1)
  connectPort.toTrReadPort(
    subRegFile.io.readTr(1),
    1.U,
    subMMAU.io.addrReadB,
    subMMAU.io.vecB
  )

  //read Cin(Acc0),using subRegFile.io.readAcc(0)
  connectPort.toAccReadPort(
    subRegFile.io.readAcc(0),
    0.U,
    subMMAU.io.addrReadC,
    subMMAU.io.vecCin
  )

  //write Cout(Acc0),using subRegFile.io.writeAcc(0)
  connectPort.toAccWritePort(
    subRegFile.io.writeAcc(0),
    0.U,
    subMMAU.io.addrWriteC,
    subMMAU.io.vecCout,
    subMMAU.io.sigEnWriteC
  )

  // 结束后注销端口
  when(subMMAU.io.sigDone) {
    subRegFile.io.readTr(0).act := false.B
    subRegFile.io.readTr(1).act := false.B
    subRegFile.io.readAcc(0).act := false.B
    subRegFile.io.writeAcc(0).act := false.B
  }
}



