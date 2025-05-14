package RegFile

import chisel3._
import chisel3.util._

import common._
import utility.sram._







// Tr ReadPort
class RegFileTrRead_IO extends Bundle {
  val addr = Input(UInt(Consts.Tr_ADDR_LEN.W)) // 选择具体的 Tr（0~3）
  val r = Vec(Consts.numTrBank, Flipped(new SRAMReadBus(Consts.genTr, Consts.setTr, Consts.wayTr)))
  val act = Input(Bool())
}

// Tr WritePort
class RegFileTrWrite_IO extends Bundle {
  val addr = Input(UInt(Consts.Tr_ADDR_LEN.W)) // 选择具体的 Tr（0~3）
  val w = Vec(Consts.numTrBank, Flipped(new SRAMWriteBus(Consts.genTr, Consts.setTr, Consts.wayTr, Consts.useBitmaskTr)))
  val act = Input(Bool())
}

// Acc ReadPort
class RegFileAccRead_IO extends Bundle {
  val addr = Input(UInt(Consts.Acc_ADDR_LEN.W)) // 选择具体的 Acc（0~3）
  val r = Vec(Consts.numAccBank, Flipped(new SRAMReadBus(Consts.genAcc, Consts.setAcc, Consts.wayAcc)))
  val act = Input(Bool())
}

// Acc WritePort
class RegFileAccWrite_IO extends Bundle {
  val addr = Input(UInt(Consts.Acc_ADDR_LEN.W)) // 选择具体的 Acc（0~3）
  val w = Vec(Consts.numAccBank, Flipped(new SRAMWriteBus(Consts.genAcc, Consts.setAcc, Consts.wayAcc, Consts.useBitmaskAcc)))
  val act = Input(Bool())
}

// All ReadPort
class RegFileAllRead_IO extends Bundle {
  val addr = Input(UInt(Consts.All_ADDR_LEN.W)) // 选择具体的 Reg（0~7）
  val r = Vec(Consts.numAllBank, Flipped(new SRAMReadBus(Consts.genAll, Consts.setAll, Consts.wayAll)))
  val act = Input(Bool())
}

// All WritePort
class RegFileAllWrite_IO extends Bundle {
  val addr = Input(UInt(Consts.All_ADDR_LEN.W)) // 选择具体的 Reg（0~7）
  val w = Vec(Consts.numAllBank, Flipped(new SRAMWriteBus(Consts.genAll, Consts.setAll, Consts.wayAll, Consts.useBitmaskAll)))
  val act = Input(Bool())
}




// === 顶层 RegFileIO ===

class RegFileIO extends Bundle {
  // Tr
  val readTr = Vec(Consts.numTrReadPort, new RegFileTrRead_IO)
  val writeTr = Vec(Consts.numTrWritePort, new RegFileTrWrite_IO)

  // Acc
  val readAcc = Vec(Consts.numAccReadPort, new RegFileAccRead_IO)
  val writeAcc = Vec(Consts.numAccWritePort, new RegFileAccWrite_IO)

  // All
  val readAll = Vec(Consts.numAllReadPort, new RegFileAllRead_IO)
  val writeAll = Vec(Consts.numAllWritePort, new RegFileAllWrite_IO)
}




/*
//read port
req.bits.setIdx
resp.data.head
req.valid

//write port
req.bits.setIdx
req.bits.data.head
req.valid
*/