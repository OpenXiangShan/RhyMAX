package RegFile

import chisel3._
import chisel3.util._

import common._
import utility.sram._



class RegFileIO extends Bundle {
  //Tr
  val readTr = Vec(Consts.numTrReadPort, new Bundle {
    val addr = Input(UInt(3.W)) // 选择具体的 Tr（0~3）
    val r = Vec(Consts.numTrBank, Flipped(new SRAMReadBus(Consts.genTr, Consts.setTr, Consts.wayTr)))
    val act = Input(Bool())
  })

  val writeTr = Vec(Consts.numTrWritePort, new Bundle {
    val addr = Input(UInt(3.W)) // 选择具体的 Tr（0~3）
    val w = Vec(Consts.numTrBank, Flipped(new SRAMWriteBus(Consts.genTr, Consts.setTr, Consts.wayTr, Consts.useBitmaskTr)))
    val act = Input(Bool())
  })

  //Acc
  val readAcc = Vec(Consts.numAccReadPort, new Bundle {
    val addr = Input(UInt(3.W)) // 选择具体的 Acc（0~3）
    val r = Vec(Consts.numAccBank, Flipped(new SRAMReadBus(Consts.genAcc, Consts.setAcc, Consts.wayAcc)))
    val act = Input(Bool())
  })

  val writeAcc = Vec(Consts.numAccWritePort, new Bundle {
    val addr = Input(UInt(3.W)) // 选择具体的 Acc（0~3）
    val w = Vec(Consts.numAccBank, Flipped(new SRAMWriteBus(Consts.genAcc, Consts.setAcc, Consts.wayAcc, Consts.useBitmaskAcc)))
    val act = Input(Bool())
  })
}




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







// class RegFile extends RegFileFormat {
//   val io = IO(new Bundle {
//     val r = Flipped(new SRAMReadBus(genTr, setTr, wayTr))
//     val w = Flipped(new SRAMWriteBus(genTr, setTr, wayTr, useBitmaskTr))
//   })

//   // 实例化SRAM（直接使用继承的参数）
//   private val Bank = Module(new SplittedSRAMTemplate(
//     gen = genTr,
//     set = setTr,
//     way = wayTr,
//     setSplit = setSplitTr,
//     waySplit = waySplitTr,
//     dataSplit = dataSplitTr
//   ))

//   // 连接接口
//   io.r <> Bank.io.r
//   io.w <> Bank.io.w
// }
