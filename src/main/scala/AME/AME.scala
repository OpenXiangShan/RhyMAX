package AME

import chisel3._
import chisel3.util._

import utility.sram._
import common._
import RegFile._
import MMAU._
import Expander._



class AME extends Module {
  val io = IO(new Bundle {
    val sigStart = Input(Bool())    // 启动信号

    val mtileConfig_io = new mtileConfig_IO
    val writeAll = new RegFileAllWriteIO  //通用读端口
    val readAll = new RegFileAllReadIO  //通用写端口


    val sigDone = Output(Bool())    // 结束信号
  })

  val subMMAU = Module(new MMAU)
  val subRegFile = Module(new RegFile)
  val subFSM = Module(new FSM)
  val subTileHandler = Module(new TileHandler)


  /*  between TileHandler and AME*/
  subTileHandler.io.mtileConfig_io <> io.mtileConfig_io

  /*  between TileHandler and FSM */
  subTileHandler.io.TileHandler_io <> subFSM.io.TileHandler_io


  

  /* between FSM and AME */
  subFSM.io.sigStart := io.sigStart
  io.sigDone := subFSM.io.sigDone



  /* between RF and AME */
  subRegFile.io := DontCare

  io.writeAll <> subRegFile.io.writeAll(0)
  io.readAll <> subRegFile.io.readAll(0)

  /* between RF and MMAU*/
    //read A(Tr0),using subRegFile.io.readTr(0)
  connectPort.toTrReadPort(
    subRegFile.io.readTr(0),
    0.U,
    subMMAU.io.addrReadA,
    subMMAU.io.vecA
  )

    //read B(Tr1),using subRegFile.io.readTr(1)
  connectPort.toTrReadPort(
    subRegFile.io.readTr(1),
    1.U,
    subMMAU.io.addrReadB,
    subMMAU.io.vecB
  )

    //read Cin(Acc0),using subRegFile.io.readAcc(0)
  connectPort.toAccReadPort(
    subRegFile.io.readAcc(0),
    0.U,
    subMMAU.io.addrReadC,
    subMMAU.io.vecCin
  )

    //write Cout(Acc0),using subRegFile.io.writeAcc(0)
  connectPort.toAccWritePort(
    subRegFile.io.writeAcc(0),
    0.U,
    subMMAU.io.addrWriteC,
    subMMAU.io.vecCout,
    subMMAU.io.sigEnWriteC
  )

  // 结束后注销端口
  when(subFSM.io.sigDone) {
    subRegFile.io.readTr(0).act := false.B
    subRegFile.io.readTr(1).act := false.B
    subRegFile.io.readAcc(0).act := false.B
    subRegFile.io.writeAcc(0).act := false.B
  }


  /* between FSM and MMAU*/
  subMMAU.io.FSM_io <> subFSM.io.FSM_io

}




