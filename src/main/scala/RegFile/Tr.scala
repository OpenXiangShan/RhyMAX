package RegFile

import chisel3._
import chisel3.util._

import common._
import utility.sram._




class Tr extends RegFileFormat {
  val io = IO(new Bundle {
    val r = Vec(numTrBank, Flipped(new SRAMReadBus(genTr, setTr, wayTr)))
    val w = Vec(numTrBank, Flipped(new SRAMWriteBus(genTr, setTr, wayTr, useBitmaskTr)))
  })

  // 实例化 numTrBank 个 SRAM Bank
  private val banks = Seq.fill(numTrBank) {
    Module(new SplittedSRAMTemplate(
      gen = genTr,
      set = setTr,
      way = wayTr,
      setSplit = setSplitTr,
      waySplit = waySplitTr,
      dataSplit = dataSplitTr,
      bypassWrite = true,
      suffix = Some("sram"),
    ))
  }

  // 连接每组 IO 和对应的 Bank
  for ((bank, idx) <- banks.zipWithIndex) {
    bank.io.r <> io.r(idx)
    bank.io.w <> io.w(idx)
  }
}