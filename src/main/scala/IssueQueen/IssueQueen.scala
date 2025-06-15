package IssueQueen

import chisel3._
import chisel3.util._

import common._
import MMAU._
import Expander._


class IssueQueen extends Module {
  val io = IO(new Bundle {
    val Uop_In_io  = new Uop_IO
    val Uop_Out_io = Flipped(new Uop_IO)
  })

  // ======== 拼接字段 ========
  val enq_data = Cat(
    io.Uop_In_io.mtileConfig_io.mtilek,
    io.Uop_In_io.mtileConfig_io.mtilen,
    io.Uop_In_io.mtileConfig_io.mtilem,

                                            //新的指令加在空行中,由下往上
    io.Uop_In_io.InsType_io.is_mlae8,
    io.Uop_In_io.InsType_io.is_mlbe8,
    io.Uop_In_io.InsType_io.is_mmacc,
    io.Uop_In_io.Operands_io.rs2,
    io.Uop_In_io.Operands_io.rs1,
    io.Uop_In_io.Operands_io.md,
    io.Uop_In_io.Operands_io.ms2,
    io.Uop_In_io.Operands_io.ms1
  )

  // ======== 位宽定义 ========
  val totalWidth = log2Ceil(Consts.tileK + 1) +
                   log2Ceil(Consts.tileN + 1) +
                   log2Ceil(Consts.tileM + 1) +
                   1 + 1 + 1 +                    //新指令添加则"+1"
                   Consts.rs2_LEN +
                   Consts.rs1_LEN +
                   Consts.All_ADDR_LEN * 3

  val fifo = Module(new SimpleHandshakeFIFO(depth = 20, width = totalWidth))

  // ======== 输入端握手 ========
  fifo.io.enq_valid := io.Uop_In_io.ShakeHands_io.valid
  fifo.io.enq_bits  := enq_data
  io.Uop_In_io.ShakeHands_io.ready := fifo.io.enq_ready

  // ======== 输出端拆解 ========
  val deq_data = fifo.io.deq_bits

  // 定义各字段的位偏移量
  val ms1Offset = 0
  val ms2Offset = ms1Offset + Consts.All_ADDR_LEN
  val mdOffset = ms2Offset + Consts.All_ADDR_LEN
  val rs1Offset = mdOffset + Consts.All_ADDR_LEN
  val rs2Offset = rs1Offset + Consts.rs1_LEN
  val is_mmaccOffset = rs2Offset + Consts.rs2_LEN 
  val is_mlbe8Offset = is_mmaccOffset + 1
  val is_mlae8Offset = is_mlbe8Offset + 1
                                                          //新的指令加在空行中,由上往下
  val mtilemOffset = is_mlae8Offset + 1
  val mtilenOffset = mtilemOffset + log2Ceil(Consts.tileM + 1)
  val mtilekOffset = mtilenOffset + log2Ceil(Consts.tileN + 1)

  io.Uop_Out_io.Operands_io.ms1      := deq_data(ms1Offset + Consts.All_ADDR_LEN - 1, ms1Offset)
  io.Uop_Out_io.Operands_io.ms2      := deq_data(ms2Offset + Consts.All_ADDR_LEN - 1, ms2Offset)
  io.Uop_Out_io.Operands_io.md       := deq_data(mdOffset + Consts.All_ADDR_LEN - 1, mdOffset)
  io.Uop_Out_io.Operands_io.rs1      := deq_data(rs1Offset + Consts.rs1_LEN - 1, rs1Offset)
  io.Uop_Out_io.Operands_io.rs2      := deq_data(rs2Offset + Consts.rs2_LEN - 1, rs2Offset)
  io.Uop_Out_io.InsType_io.is_mmacc  := deq_data(is_mmaccOffset).asBool
  io.Uop_Out_io.InsType_io.is_mlbe8  := deq_data(is_mlbe8Offset).asBool
  io.Uop_Out_io.InsType_io.is_mlae8  := deq_data(is_mlae8Offset).asBool
  io.Uop_Out_io.mtileConfig_io.mtilem := deq_data(mtilemOffset + log2Ceil(Consts.tileM + 1) - 1, mtilemOffset)
  io.Uop_Out_io.mtileConfig_io.mtilen := deq_data(mtilenOffset + log2Ceil(Consts.tileN + 1) - 1, mtilenOffset)
  io.Uop_Out_io.mtileConfig_io.mtilek := deq_data(mtilekOffset + log2Ceil(Consts.tileK + 1) - 1, mtilekOffset)

  // ======== 输出端握手 ========
  io.Uop_Out_io.ShakeHands_io.valid := fifo.io.deq_valid
  fifo.io.deq_ready       := io.Uop_Out_io.ShakeHands_io.ready
}