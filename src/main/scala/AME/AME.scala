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
    val writeAll = new RegFileAllWriteIO
    val readAll = new RegFileAllReadIO

    val sigDone = Output(Bool())    // 结束信号
  })

  val subMMAU = Module(new MMAU)
  val subRegFile = Module(new RegFile)
  val subFSM = Module(new FSM)

  subRegFile.io := DontCare

  /* between FSM and AME */
  subFSM.io.sigStart := io.sigStart
  io.sigDone := subFSM.io.FSM_io.sigDone
  

  /* between RF and AME */
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
  when(subFSM.io.FSM_io.sigDone) {
    subRegFile.io.readTr(0).act := false.B
    subRegFile.io.readTr(1).act := false.B
    subRegFile.io.readAcc(0).act := false.B
    subRegFile.io.writeAcc(0).act := false.B
  }


  /* between FSM and MMAU*/
  subMMAU.io.FSM_io <> subFSM.io.FSM_io

}





// class AME extends Module {
//   val io = IO(new Bundle {
//     val sigStart = Input(Bool())    // 启动信号
//     val writeTr = new RegFileTrWriteIO
//     val writeAcc = new RegFileAccWriteIO
//     val readTr = new RegFileTrReadIO
//     val readAcc = new RegFileAccReadIO

//     val sigDone = Output(Bool())    // 结束信号
//   })

//   val subMMAU = Module(new MMAU)
//   val subRegFile = Module(new RegFile)
//   val subFSM = Module(new FSM)

//   subRegFile.io := DontCare

//   /* between FSM and AME */
//   subFSM.io.sigStart := io.sigStart
//   io.sigDone := subFSM.io.FSM_io.sigDone
  

//   /* between RF and AME */
//   io.writeTr <> subRegFile.io.writeTr(2)
//   io.writeAcc <> subRegFile.io.writeAcc(2)
//   io.readTr <> subRegFile.io.readTr(2)
//   io.readAcc <> subRegFile.io.readAcc(2)

//   /* between RF and MMAU*/
//     //read A(Tr0),using subRegFile.io.readTr(0)
//   connectPort.toTrReadPort(
//     subRegFile.io.readTr(0),
//     0.U,
//     subMMAU.io.addrReadA,
//     subMMAU.io.vecA
//   )

//     //read B(Tr1),using subRegFile.io.readTr(1)
//   connectPort.toTrReadPort(
//     subRegFile.io.readTr(1),
//     1.U,
//     subMMAU.io.addrReadB,
//     subMMAU.io.vecB
//   )

//     //read Cin(Acc0),using subRegFile.io.readAcc(0)
//   connectPort.toAccReadPort(
//     subRegFile.io.readAcc(0),
//     0.U,
//     subMMAU.io.addrReadC,
//     subMMAU.io.vecCin
//   )

//     //write Cout(Acc0),using subRegFile.io.writeAcc(0)
//   connectPort.toAccWritePort(
//     subRegFile.io.writeAcc(0),
//     0.U,
//     subMMAU.io.addrWriteC,
//     subMMAU.io.vecCout,
//     subMMAU.io.sigEnWriteC
//   )

//   // 结束后注销端口
//   when(subFSM.io.FSM_io.sigDone) {
//     subRegFile.io.readTr(0).act := false.B
//     subRegFile.io.readTr(1).act := false.B
//     subRegFile.io.readAcc(0).act := false.B
//     subRegFile.io.writeAcc(0).act := false.B
//   }


//   /* between FSM and MMAU*/
//   subMMAU.io.FSM_io <> subFSM.io.FSM_io

// }



