package RegFile

import chisel3._
import chisel3.util._

import common._
import utility.sram._



class RegFile extends RegFileFormat {
  val io = IO(new Bundle {
    val r = Flipped(new SRAMReadBus(genTr, setTr, wayTr))
    val w = Flipped(new SRAMWriteBus(genTr, setTr, wayTr, useBitmaskTr))
  })

  // 实例化SRAM（直接使用继承的参数）
  private val Bank = Module(new SplittedSRAMTemplate(
    gen = genTr,
    set = setTr,
    way = wayTr,
    setSplit = setSplitTr,
    waySplit = waySplitTr,
    dataSplit = dataSplitTr
  ))

  // 连接接口
  io.r <> Bank.io.r
  io.w <> Bank.io.w
}


// class RegFile extends RegFileFormat {
//   val io = IO(new Bundle {
//     val r = Flipped(new SRAMReadBus(genTr, setTr, wayTr))
//     val w = Flipped(new SRAMWriteBus(genTr, setTr, wayTr, useBitmaskTr))
//   })

//   // 实例化SRAM（直接使用继承的参数）
//   private val Bank = Module(new SRAMTemplate(
//     gen = genTr,
//     set = setTr,
//     way = wayTr
//   ))

//   // 连接接口
//   io.r <> Bank.io.r
//   io.w <> Bank.io.w
// }


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
      dataSplit = dataSplitTr
    ))
  }

  // 连接每组 IO 和对应的 Bank
  for ((bank, idx) <- banks.zipWithIndex) {
    bank.io.r <> io.r(idx)
    bank.io.w <> io.w(idx)
  }
}


//GPT
// // 假设 4 个 Tr 实例化
// val trs = Seq.fill(4)(Module(new Tr))

// // 定义 4 个 read port IO
// val readPorts = IO(Vec(4, new Bundle {
//   val addr = Input(UInt(2.W)) // 0~3，选择具体 Tr
//   val index = Input(Vec(numTrBank, UInt(log2Ceil(setTr).W))) // 每 bank 的 index
//   val data = Output(Vec(numTrBank, UInt(8.W))) // 每 bank 的返回数据
// }))

// // 默认所有 bank 的 read valid 置零，避免悬空信号
// for (tr <- trs; bank <- 0 until numTrBank) {
//   tr.io.r(bank).req.valid := false.B
//   tr.io.r(bank).req.bits.setIdx := DontCare
// }

// // 处理每个读端口
// for (portIdx <- 0 until 4) {
//   val port = readPorts(portIdx)

//   // 选择对应 Tr
//   val selectedTr = trs(port.addr)

//   // 向 selectedTr 的每个 bank 发起读请求
//   for (bankIdx <- 0 until numTrBank) {
//     selectedTr.io.r(bankIdx).req.valid := true.B
//     selectedTr.io.r(bankIdx).req.bits.setIdx := port.index(bankIdx)

//     // 返回数据接上
//     port.data(bankIdx) := selectedTr.io.r(bankIdx).resp.data.head
//   }
// }
