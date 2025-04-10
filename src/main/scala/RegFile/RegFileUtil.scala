package RegFile

import chisel3._
import chisel3.util._

import common._
import utility.sram._







// Tr Read
class RegFileTrReadIO extends Bundle {
  val addr = Input(UInt(3.W)) // 选择具体的 Tr（0~3）
  val r = Vec(Consts.numTrBank, Flipped(new SRAMReadBus(Consts.genTr, Consts.setTr, Consts.wayTr)))
  val act = Input(Bool())
}

// Tr Write
class RegFileTrWriteIO extends Bundle {
  val addr = Input(UInt(3.W)) // 选择具体的 Tr（0~3）
  val w = Vec(Consts.numTrBank, Flipped(new SRAMWriteBus(Consts.genTr, Consts.setTr, Consts.wayTr, Consts.useBitmaskTr)))
  val act = Input(Bool())
}

// Acc Read
class RegFileAccReadIO extends Bundle {
  val addr = Input(UInt(3.W)) // 选择具体的 Acc（0~3）
  val r = Vec(Consts.numAccBank, Flipped(new SRAMReadBus(Consts.genAcc, Consts.setAcc, Consts.wayAcc)))
  val act = Input(Bool())
}

// Acc Write
class RegFileAccWriteIO extends Bundle {
  val addr = Input(UInt(3.W)) // 选择具体的 Acc（0~3）
  val w = Vec(Consts.numAccBank, Flipped(new SRAMWriteBus(Consts.genAcc, Consts.setAcc, Consts.wayAcc, Consts.useBitmaskAcc)))
  val act = Input(Bool())
}






// === 顶层 RegFileIO ===

class RegFileIO extends Bundle {
  // Tr
  val readTr = Vec(Consts.numTrReadPort, new RegFileTrReadIO)
  val writeTr = Vec(Consts.numTrWritePort, new RegFileTrWriteIO)

  // Acc
  val readAcc = Vec(Consts.numAccReadPort, new RegFileAccReadIO)
  val writeAcc = Vec(Consts.numAccWritePort, new RegFileAccWriteIO)
}

