package RegFile

import chisel3._
import chisel3.util._

import common._
import utility.sram._





class RegFile extends RegFileFormat {
  val io = IO(new RegFileIO)
  io := DontCare

  // 实例化 numTr 个 Tr
  val trs = Seq.fill(numTr)(Module(new Tr))
  val trsVec = VecInit(trs.map(_.io)) 
  trsVec.foreach(_ := DontCare) //避免报错

  //Tr
  for (portIdx <- 0 until numTrReadPort) {
  val port = io.readTr(portIdx)
  
    for ((tr, trIdx) <- trs.zipWithIndex) {
      val hit = port.addr === trIdx.U

      when(hit && port.act){
        for (bankIdx <- 0 until numTrBank) {
          port.r(bankIdx) <> tr.io.r(bankIdx)
        }
      }
    }
  }

  for (portIdx <- 0 until numTrWritePort) {
    val port = io.writeTr(portIdx)
    
    for ((tr, trIdx) <- trs.zipWithIndex) {
      val hit = port.addr === trIdx.U

      when(hit && port.act){
        for (bankIdx <- 0 until numTrBank) {
          port.w(bankIdx) <> tr.io.w(bankIdx)
        }
      }
    }
  }

  // 实例化 numAcc 个 Acc
  val accs = Seq.fill(numAcc)(Module(new Acc))
  val accsVec = VecInit(accs.map(_.io)) 
  accsVec.foreach(_ := DontCare) //避免报错

  //Acc 
  for (portIdx <- 0 until numAccReadPort) {
  val port = io.readAcc(portIdx)
  
    for ((acc, accIdx) <- accs.zipWithIndex) {
      val hit = port.addr === accIdx.U

      when(hit && port.act){
        for (bankIdx <- 0 until numAccBank) {
          port.r(bankIdx) <> acc.io.r(bankIdx)
        }
      }
    }
  }

  for (portIdx <- 0 until numAccWritePort) {
    val port = io.writeAcc(portIdx)
    
    for ((acc, accIdx) <- accs.zipWithIndex) {
      val hit = port.addr === accIdx.U

      when(hit && port.act){
        for (bankIdx <- 0 until numAccBank) {
          port.w(bankIdx) <> acc.io.w(bankIdx)
        }
      }
    }
  }

}




