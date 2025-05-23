package AME

import chisel3._
import chisel3.util._

import utility.sram._
import common._
import RegFile._
import MMAU._
import Expander._
import ScoreBoard._
import MLU._



//完整Expander
class AME extends Module {
  val io = IO(new Bundle {
    // val sigStart = Input(Bool())    // 启动信号

    // val mtileConfig_io = new mtileConfig_IO //配置矩阵形状
    // val Operands_io = new Operands_IO //译码信号

    // val ShakeHands_io = new ShakeHands_IO
    // val Operands_io = new Operands_IO
    // val InsType_io = new InsType_IO
    // val mtileConfig_io = new mtileConfig_IO
    val Uop_io = new Uop_IO //译码后信号

    val writeAll = new RegFileAllWrite_IO  //通用读端口
    val readAll = new RegFileAllRead_IO  //通用写端口

    val MLU_L2_io = new MLU_L2_IO   //访问L2


    val sigDone = Output(Bool())    // for debug
  })

  val subMMAU = Module(new MMAU)
  val subRegFile = Module(new RegFile)
  val subExpander = Module(new Expander)
  val subScoreBoard = Module(new ScoreBoard)
  val subMLU = Module(new MLU)

  /*  for debug   */
  io.sigDone := subExpander.io.sigDone

  /*  between Top and MMAU  */
  // nothing

  /*  between Top and MLU  */
  io.MLU_L2_io <> subMLU.io.MLU_L2_io

  /*  between Top and RegFile  */
  subRegFile.io := DontCare

  io.writeAll <> subRegFile.io.writeAll(1)
  io.readAll <> subRegFile.io.readAll(1)

  /*  between Top and Expander  */
  io.Uop_io <> subExpander.io.Uop_io

  /*  between Top and ScoreBoard  */
  // nothing

  /*  between MMAU and RegFile  */
    //read A(Tr0),using subRegFile.io.readTr(0)
  connectPort.toTrReadPort(
    subRegFile.io.readTr(0),
    subMMAU.io.Ops_io.ms1,
    subMMAU.io.actPortReadA,
    subMMAU.io.addrReadA,
    subMMAU.io.vecA
  )

    //read B(Tr1),using subRegFile.io.readTr(1)
  connectPort.toTrReadPort(
    subRegFile.io.readTr(1),
    subMMAU.io.Ops_io.ms2,
    subMMAU.io.actPortReadB,
    subMMAU.io.addrReadB,
    subMMAU.io.vecB
  )

    //read Cin(Acc0),using subRegFile.io.readAcc(0)
  connectPort.toAccReadPort(
    subRegFile.io.readAcc(0),
    subMMAU.io.Ops_io.md,
    subMMAU.io.actPortReadC,
    subMMAU.io.addrReadC,
    subMMAU.io.vecCin
  )

    //write Cout(Acc0),using subRegFile.io.writeAcc(0)
  connectPort.toAccWritePort(
    subRegFile.io.writeAcc(0),
    subMMAU.io.Ops_io.md,
    subMMAU.io.actPortWriteC,
    subMMAU.io.addrWriteC,
    subMMAU.io.vecCout,
    subMMAU.io.sigEnWriteC
  )

  /*  between MMAU and Expander  */
  subMMAU.io.FSM_MMAU_io <> subExpander.io.FSM_MMAU_io

  /*  between MMAU and ScoreBoard  */
  // nothing

  /*  between MMAU and MLU  */
  // nothing

  /*  between RegFile and MLU  */
  subRegFile.io.writeAll(0) <> subMLU.io.RegFileAllWrite_io


  /*  between RegFile and Expander  */
  // nothing

  /*  between RegFile and ScoreBoard  */
  // nothing

  /*  between Expander and ScoreBoard  */
  subExpander.io.ScoreboardVisit_io <> subScoreBoard.io.ScoreboardVisit_io

  /*  between Expander and MLU  */
  subMLU.io.FSM_MLU_io <> subExpander.io.FSM_MLU_io

  /*  between ScoreBoard and MLU  */
  //nothing

}












// //还没有完整Expander
// class AME extends Module {
//   val io = IO(new Bundle {
//     val sigStart = Input(Bool())    // 启动信号

//     val mtileConfig_io = new mtileConfig_IO //配置矩阵形状
//     val Operands_io = new Operands_IO //译码信号
//     val writeAll = new RegFileAllWrite_IO  //通用读端口
//     val readAll = new RegFileAllRead_IO  //通用写端口


//     val sigDone = Output(Bool())    // 结束信号
//   })

//   val subMMAU = Module(new MMAU)
//   val subRegFile = Module(new RegFile)
//   val subFSM = Module(new FSM)
//   val subTileHandler = Module(new TileHandler)


//   /*  between TileHandler and AME*/
//   subTileHandler.io.mtileConfig_io <> io.mtileConfig_io

//   /*  between TileHandler and FSM */
//   subTileHandler.io.TileHandler_MMAU_io <> subFSM.io.TileHandler_MMAU_io


  

//   /* between FSM and AME */
//   subFSM.io.sigStart := io.sigStart
//   io.sigDone := subFSM.io.sigDone

//   subFSM.io.Ops_io.ms1 := io.Operands_io.ms1(1,0)
//   subFSM.io.Ops_io.ms2 := io.Operands_io.ms2(1,0)
//   subFSM.io.Ops_io.md := io.Operands_io.md(1,0)



//   /* between RF and AME */
//   subRegFile.io := DontCare

//   io.writeAll <> subRegFile.io.writeAll(0)
//   io.readAll <> subRegFile.io.readAll(0)

//   /* between RF and MMAU*/
//     //read A(Tr0),using subRegFile.io.readTr(0)
//   connectPort.toTrReadPort(
//     subRegFile.io.readTr(0),
//     subMMAU.io.Ops_io.ms1,
//     subMMAU.io.actPortReadA,
//     subMMAU.io.addrReadA,
//     subMMAU.io.vecA
//   )

//     //read B(Tr1),using subRegFile.io.readTr(1)
//   connectPort.toTrReadPort(
//     subRegFile.io.readTr(1),
//     subMMAU.io.Ops_io.ms2,
//     subMMAU.io.actPortReadB,
//     subMMAU.io.addrReadB,
//     subMMAU.io.vecB
//   )

//     //read Cin(Acc0),using subRegFile.io.readAcc(0)
//   connectPort.toAccReadPort(
//     subRegFile.io.readAcc(0),
//     subMMAU.io.Ops_io.md,
//     subMMAU.io.actPortReadC,
//     subMMAU.io.addrReadC,
//     subMMAU.io.vecCin
//   )

//     //write Cout(Acc0),using subRegFile.io.writeAcc(0)
//   connectPort.toAccWritePort(
//     subRegFile.io.writeAcc(0),
//     subMMAU.io.Ops_io.md,
//     subMMAU.io.actPortWriteC,
//     subMMAU.io.addrWriteC,
//     subMMAU.io.vecCout,
//     subMMAU.io.sigEnWriteC
//   )



//   /* between FSM and MMAU*/
//   subMMAU.io.FSM_MMAU_io <> subFSM.io.FSM_MMAU_io

// }




