package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._



class ExcuteHandler extends Module {
  val io = IO(new Bundle {
    val Uop_io = new Uop_IO
    val ScoreboardVisit_io = new ScoreboardVisit_IO

    val IssueMMAU_Excute_io     = Flipped(new IssueMMAU_Excute_IO)
    val IssueMLU_Excute_io      = Flipped(new IssueMLU_Excute_IO)
    val IssueMSU_Excute_io      = Flipped(new IssueMSU_Excute_IO)
    val IssueMrelease_Excute_io = Flipped(new IssueMrelease_Excute_IO)
  })

  // ----------- 别名简化 -----------
  val uop = io.Uop_io
  val sb = io.ScoreboardVisit_io

  val ms1 = uop.Operands_io.ms1
  val ms2 = uop.Operands_io.ms2
  val md  = uop.Operands_io.md
  val rs1 = uop.Operands_io.rs1
  val rs2 = uop.Operands_io.rs2

  val MLU_Bit  = 0
  val MSU_Bit  = 1
  val MMAU_Bit = 2
  val MISC_Bit = 3
  val EWU_Bit  = 4
  val MRELEASE_Bit = 5

  // ----------- 默认值 -----------
  sb.writeMaskAlloc_RF   := 0.U
  sb.writeMaskAlloc_Unit := 0.U
  sb.writeMaskFree_RF    := 0.U
  sb.writeMaskFree_Unit  := 0.U


  // MMAU赋默认值
  io.IssueMMAU_Excute_io.sigStart := false.B
  io.IssueMMAU_Excute_io.in_ms1   := 0.U
  io.IssueMMAU_Excute_io.in_ms2   := 0.U
  io.IssueMMAU_Excute_io.in_md    := 0.U
  io.IssueMMAU_Excute_io.mtilem   := 0.U
  io.IssueMMAU_Excute_io.mtilen   := 0.U
  io.IssueMMAU_Excute_io.mtilek   := 0.U

  // MLU赋默认值
  io.IssueMLU_Excute_io.sigStart := false.B
  io.IssueMLU_Excute_io.rs1      := 0.U
  io.IssueMLU_Excute_io.rs2      := 0.U
  io.IssueMLU_Excute_io.in_md    := 0.U
  io.IssueMLU_Excute_io.mtilem   := 0.U
  io.IssueMLU_Excute_io.mtilen   := 0.U
  io.IssueMLU_Excute_io.mtilek   := 0.U

  io.IssueMLU_Excute_io.is_mlbe8 := false.B
  io.IssueMLU_Excute_io.is_mlae8 := false.B
  io.IssueMLU_Excute_io.is_mlce32 := false.B
                                                          //新的指令加在空行中,由上往下

  val is_MLU_ins = Wire(Bool()) //指令为Load指令
  is_MLU_ins := uop.InsType_io.is_mlbe8 || uop.InsType_io.is_mlae8 || uop.InsType_io.is_mlce32

  // MSU赋默认值
  io.IssueMSU_Excute_io.sigStart := false.B
  io.IssueMSU_Excute_io.rs1      := 0.U
  io.IssueMSU_Excute_io.rs2      := 0.U
  io.IssueMSU_Excute_io.in_md    := 0.U
  io.IssueMSU_Excute_io.mtilem   := 0.U
  io.IssueMSU_Excute_io.mtilen   := 0.U
  io.IssueMSU_Excute_io.mtilek   := 0.U

  io.IssueMSU_Excute_io.is_msce32 := false.B
                                                          //新的指令加在空行中,由上往下

  val is_MSU_ins = Wire(Bool()) //指令为Load指令
  is_MSU_ins := uop.InsType_io.is_msce32

  // Mrelease's default value
  io.IssueMrelease_Excute_io.sigStart := false.B
  io.IssueMrelease_Excute_io.tokenRd := 0.U

  // Check mrelease inst type
  val is_MRELEASE_ins = Wire(Bool())
  is_MRELEASE_ins := uop.InsType_io.is_mrelease

  // ----------- ready 信号仲裁 -----------
  val mma_ready = uop.InsType_io.is_mmacc && {
    val regFree = (sb.read_RF(ms1) | sb.read_RF(ms2) | sb.read_RF(md)) === 0.U
    val unitFree = sb.read_Unit(MMAU_Bit) === 0.U
    regFree && unitFree
  }

  val mlu_ready = is_MLU_ins && {
    val regFree = sb.read_RF(md) === 0.U
    val unitFree = sb.read_Unit(MLU_Bit) === 0.U
    regFree && unitFree
  }

  val msu_ready = is_MSU_ins && {
    val regFree = sb.read_RF(md) === 0.U
    val unitFree = sb.read_Unit(MSU_Bit) === 0.U
    regFree && unitFree
  }

  // 添加mrelease指令的ready信号判断
  val mrelease_ready = is_MRELEASE_ins && {
    val unitFree = sb.read_Unit(MSU_Bit) | sb.read_Unit(MRELEASE_Bit) === 0.U
    unitFree
  }

  val is_ready = mma_ready || mlu_ready || msu_ready || mrelease_ready
  val is_shaked = is_ready && uop.ShakeHands_io.valid
  uop.ShakeHands_io.ready := is_ready

  // ----------- Done 信号仲裁 -----------  
  val mma_FreeRF = WireDefault(0.U(Consts.RF_LEN.W))
  val mma_FreeUnit = WireDefault(0.U(Consts.Unit_LEN.W))

  val mlu_FreeRF = WireDefault(0.U(Consts.RF_LEN.W))
  val mlu_FreeUnit = WireDefault(0.U(Consts.Unit_LEN.W))

  val msu_FreeRF = WireDefault(0.U(Consts.RF_LEN.W))
  val msu_FreeUnit = WireDefault(0.U(Consts.Unit_LEN.W))

  val mrelease_FreeUnit = WireDefault(0.U(Consts.Unit_LEN.W))

  sb.writeMaskFree_RF   := mma_FreeRF | mlu_FreeRF | msu_FreeRF
  sb.writeMaskFree_Unit := mma_FreeUnit | mlu_FreeUnit | msu_FreeUnit | mrelease_FreeUnit



  // ----------- MMAU逻辑 -----------
  when (uop.InsType_io.is_mmacc) {
    io.IssueMMAU_Excute_io.sigStart := is_shaked
    io.IssueMMAU_Excute_io.in_ms1   := ms1
    io.IssueMMAU_Excute_io.in_ms2   := ms2
    io.IssueMMAU_Excute_io.in_md    := md
    io.IssueMMAU_Excute_io.mtilem   := uop.mtileConfig_io.mtilem
    io.IssueMMAU_Excute_io.mtilen   := uop.mtileConfig_io.mtilen
    io.IssueMMAU_Excute_io.mtilek   := uop.mtileConfig_io.mtilek

    when (is_shaked) { //分配资源
      sb.writeMaskAlloc_RF   := (1.U << ms1) | (1.U << ms2) | (1.U << md)
      sb.writeMaskAlloc_Unit := (1.U << MMAU_Bit)
    }

    
  }

  when (io.IssueMMAU_Excute_io.sigDone) { //释放资源
      // sb.writeMaskFree_RF   := (1.U << io.IssueMMAU_Excute_io.out_ms1) | (1.U << io.IssueMMAU_Excute_io.out_ms2) | (1.U << io.IssueMMAU_Excute_io.out_md)
      // sb.writeMaskFree_Unit := (1.U << MMAU_Bit)
      mma_FreeRF   := (1.U << io.IssueMMAU_Excute_io.out_ms1) | (1.U << io.IssueMMAU_Excute_io.out_ms2) | (1.U << io.IssueMMAU_Excute_io.out_md)
      mma_FreeUnit := (1.U << MMAU_Bit)
    }

  // ----------- MLU逻辑 -----------
  

  when (is_MLU_ins) {
    io.IssueMLU_Excute_io.sigStart := is_shaked
    io.IssueMLU_Excute_io.rs1      := rs1
    io.IssueMLU_Excute_io.rs2      := rs2
    io.IssueMLU_Excute_io.in_md    := md
    io.IssueMLU_Excute_io.mtilem   := uop.mtileConfig_io.mtilem
    io.IssueMLU_Excute_io.mtilen   := uop.mtileConfig_io.mtilen
    io.IssueMLU_Excute_io.mtilek   := uop.mtileConfig_io.mtilek

    io.IssueMLU_Excute_io.is_mlbe8 := uop.InsType_io.is_mlbe8
    io.IssueMLU_Excute_io.is_mlae8 := uop.InsType_io.is_mlae8
    io.IssueMLU_Excute_io.is_mlce32 := uop.InsType_io.is_mlce32


    when (is_shaked) { //分配资源
      sb.writeMaskAlloc_RF   := (1.U << md)
      sb.writeMaskAlloc_Unit := (1.U << MLU_Bit)
    }

    
  }

  when (io.IssueMLU_Excute_io.sigDone) { //释放资源
      // sb.writeMaskFree_RF   := (1.U << io.IssueMLU_Excute_io.out_md)
      // sb.writeMaskFree_Unit := (1.U << MLU_Bit)
      mlu_FreeRF   := (1.U << io.IssueMLU_Excute_io.out_md)
      mlu_FreeUnit := (1.U << MLU_Bit)
    }

  // ----------- MSU逻辑 -----------
  

  when (is_MSU_ins) {
    io.IssueMSU_Excute_io.sigStart := is_shaked
    io.IssueMSU_Excute_io.rs1      := rs1
    io.IssueMSU_Excute_io.rs2      := rs2
    io.IssueMSU_Excute_io.in_md    := md
    io.IssueMSU_Excute_io.mtilem   := uop.mtileConfig_io.mtilem
    io.IssueMSU_Excute_io.mtilen   := uop.mtileConfig_io.mtilen
    io.IssueMSU_Excute_io.mtilek   := uop.mtileConfig_io.mtilek

    io.IssueMSU_Excute_io.is_msce32 := uop.InsType_io.is_msce32


    when (is_shaked) {
      sb.writeMaskAlloc_RF   := (1.U << md)
      sb.writeMaskAlloc_Unit := (1.U << MSU_Bit)
    }

    
  }

  when (io.IssueMSU_Excute_io.sigDone) { //释放资源
      // sb.writeMaskFree_RF   := (1.U << io.IssueMSU_Excute_io.out_md)
      // sb.writeMaskFree_Unit := (1.U << MSU_Bit)
      msu_FreeRF   := (1.U << io.IssueMSU_Excute_io.out_md)
      msu_FreeUnit := (1.U << MSU_Bit)
    }

  // ----------- Mrelease逻辑 -----------
  when (is_MRELEASE_ins) {
    io.IssueMrelease_Excute_io.sigStart := is_shaked
    io.IssueMrelease_Excute_io.tokenRd := rs1

    when (is_shaked) {
      sb.writeMaskAlloc_Unit := (1.U << MRELEASE_Bit)
    }
  }

  when (io.IssueMrelease_Excute_io.sigDone) {
    mrelease_FreeUnit := (1.U << MRELEASE_Bit)
  }
}





// //
// class ExcuteHandler extends Module {
//   val io = IO(new Bundle {
//     val Uop_io = new Uop_IO
//     val ScoreboardVisit_io = new ScoreboardVisit_IO

//     val IssueMMAU_Excute_io = Flipped(new IssueMMAU_Excute_IO)
//     val IssueMLU_Excute_io  = Flipped(new IssueMLU_Excute_IO)
//   })

//   // ----------- 别名简化 -----------
//   val uop = io.Uop_io
//   val sb = io.ScoreboardVisit_io

//   val ms1 = uop.Operands_io.ms1
//   val ms2 = uop.Operands_io.ms2
//   val md  = uop.Operands_io.md
//   val rs1 = uop.Operands_io.rs1
//   val rs2 = uop.Operands_io.rs2

//   val MLU_Bit  = 0
//   val MSU_Bit  = 1
//   val MMAU_Bit = 2
//   val MISC_Bit = 3
//   val EWU_Bit  = 4

//   // ----------- 默认值 -----------
//   sb.writeMaskAlloc_RF   := 0.U
//   sb.writeMaskAlloc_Unit := 0.U
//   sb.writeMaskFree_RF    := 0.U
//   sb.writeMaskFree_Unit  := 0.U


//   // MMAU赋默认值
//   io.IssueMMAU_Excute_io.sigStart := false.B
//   io.IssueMMAU_Excute_io.in_ms1   := 0.U
//   io.IssueMMAU_Excute_io.in_ms2   := 0.U
//   io.IssueMMAU_Excute_io.in_md    := 0.U
//   io.IssueMMAU_Excute_io.mtilem   := 0.U
//   io.IssueMMAU_Excute_io.mtilen   := 0.U
//   io.IssueMMAU_Excute_io.mtilek   := 0.U

//   // MLU赋默认值
//   io.IssueMLU_Excute_io.sigStart := false.B
//   io.IssueMLU_Excute_io.rs1      := 0.U
//   io.IssueMLU_Excute_io.rs2      := 0.U
//   io.IssueMLU_Excute_io.in_md    := 0.U
//   io.IssueMLU_Excute_io.is_mlbe8 := false.B
//   io.IssueMLU_Excute_io.mtilem   := 0.U
//   io.IssueMLU_Excute_io.mtilen   := 0.U
//   io.IssueMLU_Excute_io.mtilek   := 0.U

//   // ----------- ready 信号仲裁 -----------
//   val mma_ready = uop.InsType_io.is_mmacc && {
//     val regFree = (sb.read_RF(ms1) | sb.read_RF(ms2) | sb.read_RF(md)) === 0.U
//     val unitFree = sb.read_Unit(MMAU_Bit) === 0.U
//     regFree && unitFree
//   }

//   val mlu_ready = uop.InsType_io.is_mlbe8 && {
//     val regFree = sb.read_RF(md) === 0.U
//     val unitFree = sb.read_Unit(MLU_Bit) === 0.U
//     regFree && unitFree
//   }

//   val is_ready = mma_ready || mlu_ready
//   val is_shaked = is_ready && uop.ShakeHands_io.valid
//   uop.ShakeHands_io.ready := is_ready

//   // ----------- MMAU逻辑 -----------
//   when (uop.InsType_io.is_mmacc) {
//     io.IssueMMAU_Excute_io.sigStart := is_shaked
//     io.IssueMMAU_Excute_io.in_ms1   := ms1
//     io.IssueMMAU_Excute_io.in_ms2   := ms2
//     io.IssueMMAU_Excute_io.in_md    := md
//     io.IssueMMAU_Excute_io.mtilem   := uop.mtileConfig_io.mtilem
//     io.IssueMMAU_Excute_io.mtilen   := uop.mtileConfig_io.mtilen
//     io.IssueMMAU_Excute_io.mtilek   := uop.mtileConfig_io.mtilek

//     when (is_shaked) {
//       sb.writeMaskAlloc_RF   := (1.U << ms1) | (1.U << ms2) | (1.U << md)
//       sb.writeMaskAlloc_Unit := (1.U << MMAU_Bit)
//     }

//     when (io.IssueMMAU_Excute_io.sigDone) {
//       sb.writeMaskFree_RF   := (1.U << io.IssueMMAU_Excute_io.out_ms1) | (1.U << io.IssueMMAU_Excute_io.out_ms2) | (1.U << io.IssueMMAU_Excute_io.out_md)
//       sb.writeMaskFree_Unit := (1.U << MMAU_Bit)
//     }
//   }

//   // ----------- MLU逻辑 -----------
//   when (uop.InsType_io.is_mlbe8) {
//     io.IssueMLU_Excute_io.sigStart := is_shaked
//     io.IssueMLU_Excute_io.rs1      := rs1
//     io.IssueMLU_Excute_io.rs2      := rs2
//     io.IssueMLU_Excute_io.in_md    := md
//     io.IssueMLU_Excute_io.is_mlbe8 := true.B
//     io.IssueMLU_Excute_io.mtilem   := uop.mtileConfig_io.mtilem
//     io.IssueMLU_Excute_io.mtilen   := uop.mtileConfig_io.mtilen
//     io.IssueMLU_Excute_io.mtilek   := uop.mtileConfig_io.mtilek

//     when (is_shaked) {
//       sb.writeMaskAlloc_RF   := (1.U << md)
//       sb.writeMaskAlloc_Unit := (1.U << MLU_Bit)
//     }

//     when (io.IssueMLU_Excute_io.sigDone) {
//       sb.writeMaskFree_RF   := (1.U << io.IssueMLU_Excute_io.out_md)
//       sb.writeMaskFree_Unit := (1.U << MLU_Bit)
//     }
//   }
// }










// 旧版，有bug
// class ExcuteHandler extends Module{
//   val io = IO(new Bundle {
    
//     val Uop_io = new Uop_IO
//     val ScoreboardVisit_io = new ScoreboardVisit_IO //访问积分板

//     val IssueMMAU_Excute_io = Flipped(new IssueMMAU_Excute_IO)    //连接IssueMMAU
//     val IssueMLU_Excute_io = Flipped(new IssueMLU_Excute_IO)  //连接IssueMLU
//   })

//   val ms1 = io.Uop_io.Operands_io.ms1
//   val ms2 = io.Uop_io.Operands_io.ms2
//   val md = io.Uop_io.Operands_io.md
//   val rs1 = io.Uop_io.Operands_io.rs1
//   val rs2 = io.Uop_io.Operands_io.rs2

//   val MLU_Bit = 0
//   val MSU_Bit = 1
//   val MMAU_Bit = 2
//   val MISC_Bit = 3
//   val EWU_Bit = 4

//   //default
//   io.ScoreboardVisit_io.writeMaskAlloc_RF := 0.U
//   io.ScoreboardVisit_io.writeMaskAlloc_Unit := 0.U
//   io.ScoreboardVisit_io.writeMaskFree_RF := 0.U
//   io.ScoreboardVisit_io.writeMaskFree_Unit := 0.U

//   //ready
//   val is_ready = Wire(Bool())
//   is_ready := false.B
//   io.Uop_io.ShakeHands_io.ready := is_ready

//   //握手是否成功
//   val is_shaked = is_ready && io.Uop_io.ShakeHands_io.valid

//   /*    MMAU    */
//   val mmaReg_is_free = ( io.ScoreboardVisit_io.read_RF(ms1) | io.ScoreboardVisit_io.read_RF(ms2) |io.ScoreboardVisit_io.read_RF(md) ) === 0.U
//   val mmaUnit_is_free = io.ScoreboardVisit_io.read_Unit(MMAU_Bit) === 0.U

//   when(io.Uop_io.InsType_io.is_mmacc){
//     is_ready := mmaReg_is_free && mmaUnit_is_free
//   }

//   io.IssueMMAU_Excute_io.sigStart := is_shaked && io.Uop_io.InsType_io.is_mmacc

//   when(io.Uop_io.InsType_io.is_mmacc && is_shaked){//分配资源
//     io.ScoreboardVisit_io.writeMaskAlloc_RF := (1.U << ms1) | (1.U << ms2) | (1.U << md)
//     io.ScoreboardVisit_io.writeMaskAlloc_Unit := (1.U << MMAU_Bit)
//   }

//   when(io.IssueMMAU_Excute_io.sigDone){//释放资源
//     io.ScoreboardVisit_io.writeMaskFree_RF := (1.U << io.IssueMMAU_Excute_io.out_ms1) | (1.U << io.IssueMMAU_Excute_io.out_ms2) | (1.U << io.IssueMMAU_Excute_io.out_md)
//     io.ScoreboardVisit_io.writeMaskFree_Unit := (1.U << MMAU_Bit)
//   }

//   io.IssueMMAU_Excute_io.in_ms1 := io.Uop_io.Operands_io.ms1  //原封不动传递
//   io.IssueMMAU_Excute_io.in_ms2 := io.Uop_io.Operands_io.ms2
//   io.IssueMMAU_Excute_io.in_md := io.Uop_io.Operands_io.md
//   io.IssueMMAU_Excute_io.mtilem := io.Uop_io.mtileConfig_io.mtilem
//   io.IssueMMAU_Excute_io.mtilen := io.Uop_io.mtileConfig_io.mtilen
//   io.IssueMMAU_Excute_io.mtilek := io.Uop_io.mtileConfig_io.mtilek

//   // /*    MLU   */
//   // val mluReg_is_free = io.ScoreboardVisit_io.read_RF(md) === 0.U
//   // val mluUnit_is_free = io.ScoreboardVisit_io.read_Unit(MLU_Bit) === 0.U

//   // when(io.Uop_io.InsType_io.is_mlbe8){
//   //   is_ready := mluReg_is_free && mluUnit_is_free
//   // }

//   io.IssueMLU_Excute_io.sigStart := is_shaked && io.Uop_io.InsType_io.is_mlbe8

//   // when(is_shaked && io.Uop_io.InsType_io.is_mlbe8){//分配资源
//   //   io.ScoreboardVisit_io.writeMaskAlloc_RF := (1.U << md)
//   //   io.ScoreboardVisit_io.writeMaskAlloc_Unit := (1.U << MLU_Bit)
//   // }

//   // when(io.IssueMLU_Excute_io.sigDone){//释放资源
//   //   io.ScoreboardVisit_io.writeMaskFree_RF :=  (1.U << io.IssueMLU_Excute_io.out_md)
//   //   io.ScoreboardVisit_io.writeMaskFree_Unit := (1.U << MLU_Bit)
//   // }

//   io.IssueMLU_Excute_io.is_mlbe8 := io.Uop_io.InsType_io.is_mlbe8  //原封不动传递
//   io.IssueMLU_Excute_io.rs1 := io.Uop_io.Operands_io.rs1  
//   io.IssueMLU_Excute_io.rs2 := io.Uop_io.Operands_io.rs2
//   io.IssueMLU_Excute_io.in_md := io.Uop_io.Operands_io.md 
//   io.IssueMLU_Excute_io.mtilem := io.Uop_io.mtileConfig_io.mtilem
//   io.IssueMLU_Excute_io.mtilen := io.Uop_io.mtileConfig_io.mtilen
//   io.IssueMLU_Excute_io.mtilek := io.Uop_io.mtileConfig_io.mtilek


// }
