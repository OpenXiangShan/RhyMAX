package AME

import chisel3._
import chisel3.util._

import utility.sram._
import common._
import RegFile._
import MMAU._



class AME extends Module{
  val io = IO(new Bundle{
    val sigStart = Input(Bool())    //启动信号
    val writeTr = new RegFileTrWriteIO
    val writeAcc = new RegFileAccWriteIO
    val readTr = new RegFileTrReadIO
    val readAcc = new RegFileAccReadIO

    val sigDone = Output(Bool())    //结束信号
  })

  val subMMAU = Module(new MMAU)
  val subRegFile = Module(new RegFile)
  subRegFile.io := DontCare

    /*  between top and subMMAU*/
  subMMAU.io.sigStart := io.sigStart
  io.sigDone := subMMAU.io.sigDone

    /*  between top and subRegFile*/
  io.writeTr <> subRegFile.io.writeTr(2)
  io.writeAcc <> subRegFile.io.writeAcc(2)
  io.readTr <> subRegFile.io.readTr(2)
  io.readAcc <> subRegFile.io.readAcc(2)

    /*  between subRegFile and subMMAU*/
  //read A(Tr0),using subRegFile.io.readTr(0)
  subRegFile.io.readTr(0).addr := 0.U
  subRegFile.io.readTr(0).act := true.B //始终激活端口
  for(i <- 0 until Consts.numTrBank){
    subRegFile.io.readTr(0).r(i).req.bits.setIdx := subMMAU.io.addrReadA(i)
    subRegFile.io.readTr(0).r(i).req.valid := true.B    //始终读使能
    subMMAU.io.vecA(i) := subRegFile.io.readTr(0).r(i).resp.data.head
  } 

  //read B(Tr1),using subRegFile.io.readTr(1)
  subRegFile.io.readTr(1).addr := 1.U
  subRegFile.io.readTr(1).act := true.B //始终激活端口
  for(i <- 0 until Consts.numTrBank){
    subRegFile.io.readTr(1).r(i).req.bits.setIdx := subMMAU.io.addrReadB(i)
    subRegFile.io.readTr(1).r(i).req.valid := true.B    //始终读使能
    subMMAU.io.vecB(i) := subRegFile.io.readTr(1).r(i).resp.data.head
  } 

  //read Cin(Acc0),using subRegFile.io.readAcc(0)
  subRegFile.io.readAcc(0).addr := 0.U
  subRegFile.io.readAcc(0).act := true.B //始终激活端口
  for(i <- 0 until Consts.numAccBank){
    subRegFile.io.readAcc(0).r(i).req.bits.setIdx := subMMAU.io.addrReadC(i)
    subRegFile.io.readAcc(0).r(i).req.valid := true.B    //始终读使能
    subMMAU.io.vecCin(i) := subRegFile.io.readAcc(0).r(i).resp.data.head
  } 

  //write Cout(Acc0),using subRegFile.io.writeAcc(0)
  subRegFile.io.writeAcc(0).addr := 0.U
  subRegFile.io.writeAcc(0).act := true.B //始终激活端口
  for(i <- 0 until Consts.numAccBank){
    subRegFile.io.writeAcc(0).w(i).req.bits.setIdx := subMMAU.io.addrWriteC(i)
    subRegFile.io.writeAcc(0).w(i).req.valid := subMMAU.io.sigEnWriteC(i)   
    subRegFile.io.writeAcc(0).w(i).req.bits.data.head := subMMAU.io.vecCout(i)
  } 

  when(subMMAU.io.sigDone === true.B){  //结束后注销端口
    subRegFile.io.readTr(0).act := false.B
    subRegFile.io.readTr(1).act := false.B
    subRegFile.io.readAcc(0).act := false.B
    subRegFile.io.writeAcc(0).act := false.B
  }
}


