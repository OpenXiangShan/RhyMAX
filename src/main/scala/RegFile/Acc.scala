package RegFile

import chisel3._
import chisel3.util._

import common._
import utility.sram._



class Acc extends RegFileFormat {
  val io = IO(new Bundle {
    val r = Vec(numAccBank, Flipped(new SRAMReadBus(genAcc, setAcc, wayAcc)))
    val w = Vec(numAccBank, Flipped(new SRAMWriteBus(genAcc, setAcc, wayAcc, useBitmaskAcc)))
  })

  // 实例化 numAccBank 个 SRAM Bank
  private val banks = Seq.fill(numAccBank) {
    Module(new SplittedSRAMTemplate(
      gen = genAcc,
      set = setAcc,
      way = wayAcc,
      setSplit = setSplitAcc,
      waySplit = waySplitAcc,
      dataSplit = dataSplitAcc,
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