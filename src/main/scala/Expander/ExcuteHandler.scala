package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._



class ExcuteHandler extends Module{
  val io = IO(new Bundle {
    val ShakeHands_io = new ShakeHands_IO   //握手
    val InsType_io = new InsType_IO         //指令类型
    val Operands_io = new Operands_IO       //操作数
    val ScoreboardVisit_io = new ScoreboardVisit_IO //访问积分板

    val IssueMMAU_Excute_io = Flipped(new IssueMMAU_Excute_IO)    //连接IssueMMAU
  })

  val ms1 = io.Operands_io.ms1
  val ms2 = io.Operands_io.ms2
  val md = io.Operands_io.md

  //default
  io.ScoreboardVisit_io.writeMaskAlloc_RF := 0.U
  io.ScoreboardVisit_io.writeMaskAlloc_Unit := 0.U
  io.ScoreboardVisit_io.writeMaskFree_RF := 0.U
  io.ScoreboardVisit_io.writeMaskFree_Unit := 0.U

  //ready
  val is_ready = Wire(Bool())
  is_ready := false.B
  io.ShakeHands_io.ready := is_ready

  //握手是否成功
  val is_shaked = is_ready && io.ShakeHands_io.valid

  /*    mma    */
  val mmaReg_is_free = ( io.ScoreboardVisit_io.read_RF(ms1) | io.ScoreboardVisit_io.read_RF(ms2) |io.ScoreboardVisit_io.read_RF(md) ) === 0.U
  val mmaUnit_is_free = io.ScoreboardVisit_io.read_Unit(1) === 0.U

  when(io.InsType_io.is_mmacc){
    is_ready := mmaReg_is_free && mmaUnit_is_free
  }

  io.IssueMMAU_Excute_io.sigStart := is_shaked && io.InsType_io.is_mmacc

  when(io.InsType_io.is_mmacc && is_shaked){//分配资源
    io.ScoreboardVisit_io.writeMaskAlloc_RF := (1.U << ms1) | (1.U << ms2) | (1.U << md)
    io.ScoreboardVisit_io.writeMaskAlloc_Unit := (1.U << 1)
  }

  when(io.IssueMMAU_Excute_io.sigDone){//释放资源
    io.ScoreboardVisit_io.writeMaskFree_RF := (1.U << io.IssueMMAU_Excute_io.ms1) | (1.U << io.IssueMMAU_Excute_io.ms2) | (1.U << io.IssueMMAU_Excute_io.md)
    io.ScoreboardVisit_io.writeMaskFree_Unit := (1.U << 1)
  }

  io.IssueMMAU_Excute_io.is_shaked := is_shaked


}