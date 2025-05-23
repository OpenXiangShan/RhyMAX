package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._



class ExcuteHandler extends Module{
  val io = IO(new Bundle {
    
    val Uop_io = new Uop_IO
    val ScoreboardVisit_io = new ScoreboardVisit_IO //访问积分板

    val IssueMMAU_Excute_io = Flipped(new IssueMMAU_Excute_IO)    //连接IssueMMAU
    val IssueMLU_Excute_io = Flipped(new IssueMLU_Excute_IO)  //连接IssueMLU
  })

  val ms1 = io.Uop_io.Operands_io.ms1
  val ms2 = io.Uop_io.Operands_io.ms2
  val md = io.Uop_io.Operands_io.md
  val rs1 = io.Uop_io.Operands_io.rs1
  val rs2 = io.Uop_io.Operands_io.rs2

  val MLU_Bit = 0
  val MSU_Bit = 1
  val MMAU_Bit = 2
  val MISC_Bit = 3
  val EWU_Bit = 4

  //default
  io.ScoreboardVisit_io.writeMaskAlloc_RF := 0.U
  io.ScoreboardVisit_io.writeMaskAlloc_Unit := 0.U
  io.ScoreboardVisit_io.writeMaskFree_RF := 0.U
  io.ScoreboardVisit_io.writeMaskFree_Unit := 0.U

  //ready
  val is_ready = Wire(Bool())
  is_ready := false.B
  io.Uop_io.ShakeHands_io.ready := is_ready

  //握手是否成功
  val is_shaked = is_ready && io.Uop_io.ShakeHands_io.valid

  /*    MMAU    */
  val mmaReg_is_free = ( io.ScoreboardVisit_io.read_RF(ms1) | io.ScoreboardVisit_io.read_RF(ms2) |io.ScoreboardVisit_io.read_RF(md) ) === 0.U
  val mmaUnit_is_free = io.ScoreboardVisit_io.read_Unit(MMAU_Bit) === 0.U

  when(io.Uop_io.InsType_io.is_mmacc){
    is_ready := mmaReg_is_free && mmaUnit_is_free
  }

  io.IssueMMAU_Excute_io.sigStart := is_shaked && io.Uop_io.InsType_io.is_mmacc

  when(io.Uop_io.InsType_io.is_mmacc && is_shaked){//分配资源
    io.ScoreboardVisit_io.writeMaskAlloc_RF := (1.U << ms1) | (1.U << ms2) | (1.U << md)
    io.ScoreboardVisit_io.writeMaskAlloc_Unit := (1.U << MMAU_Bit)
  }

  when(io.IssueMMAU_Excute_io.sigDone){//释放资源
    io.ScoreboardVisit_io.writeMaskFree_RF := (1.U << io.IssueMMAU_Excute_io.out_ms1) | (1.U << io.IssueMMAU_Excute_io.out_ms2) | (1.U << io.IssueMMAU_Excute_io.out_md)
    io.ScoreboardVisit_io.writeMaskFree_Unit := (1.U << MMAU_Bit)
  }

  io.IssueMMAU_Excute_io.in_ms1 := io.Uop_io.Operands_io.ms1  //原封不动传递
  io.IssueMMAU_Excute_io.in_ms2 := io.Uop_io.Operands_io.ms2
  io.IssueMMAU_Excute_io.in_md := io.Uop_io.Operands_io.md
  io.IssueMMAU_Excute_io.mtilem := io.Uop_io.mtileConfig_io.mtilem
  io.IssueMMAU_Excute_io.mtilen := io.Uop_io.mtileConfig_io.mtilen
  io.IssueMMAU_Excute_io.mtilek := io.Uop_io.mtileConfig_io.mtilek

  /*    MLU   */
  val mluReg_is_free = io.ScoreboardVisit_io.read_RF(md) === 0.U
  val mluUnit_is_free = io.ScoreboardVisit_io.read_Unit(MLU_Bit) === 0.U

  when(io.Uop_io.InsType_io.is_mlbe8){
    is_ready := mluReg_is_free && mluUnit_is_free
  }

  io.IssueMLU_Excute_io.sigStart := is_shaked && io.Uop_io.InsType_io.is_mlbe8

  when(is_shaked && io.Uop_io.InsType_io.is_mlbe8){//分配资源
    io.ScoreboardVisit_io.writeMaskAlloc_RF := (1.U << md)
    io.ScoreboardVisit_io.writeMaskAlloc_Unit := (1.U << MLU_Bit)
  }

  when(io.IssueMLU_Excute_io.sigDone){//释放资源
    io.ScoreboardVisit_io.writeMaskFree_RF :=  (1.U << io.IssueMLU_Excute_io.out_md)
    io.ScoreboardVisit_io.writeMaskFree_Unit := (1.U << MLU_Bit)
  }

  io.IssueMLU_Excute_io.is_mlbe8 := io.Uop_io.InsType_io.is_mlbe8  //原封不动传递
  io.IssueMLU_Excute_io.rs1 := io.Uop_io.Operands_io.rs1  
  io.IssueMLU_Excute_io.rs2 := io.Uop_io.Operands_io.rs2
  io.IssueMLU_Excute_io.in_md := io.Uop_io.Operands_io.md 
  io.IssueMLU_Excute_io.mtilem := io.Uop_io.mtileConfig_io.mtilem
  io.IssueMLU_Excute_io.mtilen := io.Uop_io.mtileConfig_io.mtilen
  io.IssueMLU_Excute_io.mtilek := io.Uop_io.mtileConfig_io.mtilek


}