package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._




class FSM extends MMAUFormat{
  val io = IO(new Bundle {
    val sigStart = Input(Bool())    //启动信号

    val FSM_io = Flipped(new FSM_IO) //输入计算单元的信号，均为寄存器链首数据

    val sigDone = Output(Bool())    //结束信号

  })

  // io.FSM_io.sigStart := io.sigStart     //启动信号,同步传递给MMAU


  val regOffM = RegInit(0.U(log2Ceil(numM).W))   //log2,向上取整
  val regOffN = RegInit(0.U(log2Ceil(numN).W))
  val regOffK = RegInit(0.U(log2Ceil(numK).W))   //表示nState内部的numK个kState状态
  val regOffK_1H = RegInit(1.U(numK.W))    //独热编码，表示nState内部的numK个kState状态

  regOffK_1H := Mux(io.sigStart === true.B , 1.U , Cat(regOffK_1H((numK)-2 , 0) , regOffK_1H((numK)-1)))   //循环左移
  regOffK := Mux(io.sigStart === true.B , 0.U , regOffK + 1.U)  //递增，循环
  regOffN := Mux(io.sigStart === true.B , 0.U , 
                Mux(regOffK_1H(numK - 1)===1.U , regOffN + 1.U , regOffN))    //regOffK_1H最高位为1时更新
  regOffM := Mux(io.sigStart === true.B , 0.U , 
                Mux(regOffN === (numN - 1).U && regOffK_1H(numK - 1) === 1.U , regOffM + 1.U , regOffM)) //可能会有一定开销，可以加一个regOffN_1H优化


  /*    muxCtrlC muxCtrlSum    */

  io.FSM_io.firstMuxCtrl := regOffK_1H(m-1 , 0)

  /*    read matrixA    */
  
  io.FSM_io.firstAddrReadA := regOffM * (numK).U + regOffK


  /*    read matrixB    */

  io.FSM_io.firstAddrReadB := regOffN * (numK).U + regOffK


  /*    read & write matrixC    */

  
  val wireNumStep = Wire(UInt(log2Ceil(numM * numN).W))
  wireNumStep := numN.U * regOffM + regOffN - 1.U
  io.FSM_io.firstAddrPublicC := wireNumStep * m.U + regOffK  //只有kState(0) ~ kState(m-1) 的数据是有意义的

  //write C enable

  val regCntZero = RegInit(0.U(1.W))  //记录kState为 numK-1 的次数,1次之后不再更新
  when(io.sigStart === true.B){
    regCntZero := 0.U
  }.elsewhen(regCntZero === 1.U){
    regCntZero := regCntZero
  }.elsewhen(regOffK_1H(numK-1) === 1.U){
    regCntZero := regCntZero + 1.U
  }.otherwise{
    regCntZero := regCntZero
  }

  val regCntDone = RegInit(0.U(log2Ceil(n/4 + m).W)) //是属于sigDone相关寄存器，C的第一个bank的index0结束后（regCntDone开始计时）,还需等待n/4 + m 个周期,所有bank结束

  if(m < numK){
    io.FSM_io.firstEnWriteC := Mux(regCntZero === 1.U && regOffK >= 0.U && regOffK <= (m-1).U && regCntDone < m.U - sramLatency.U, true.B , false.B)
  }else{
    io.FSM_io.firstEnWriteC := Mux(regCntZero === 1.U && regCntDone < m.U - sramLatency.U , true.B , false.B)
  }


  /*    signal done    */
  // val regCntDone = RegInit(0.U(log2Ceil(n/4 + m).W)) //放到前面了，C的第一个bank的index0结束后,还需等待n/4 + m 个周期,所有bank结束
  val wireDone = Wire(Bool()) //C的第一个bank的index0结束

  if(m < numK){
    wireDone := Mux(regOffM === 0.U && regOffN === 0.U && regOffK === 1.U && regCntZero === 1.U , true.B , false.B)
  }else{
    wireDone := Mux(regOffM === 0.U && regOffN === 0.U && regOffK === 1.U && regCntZero === 1.U , true.B , false.B)
  }

  when(io.sigStart === true.B){
    regCntDone := 0.U
  }.elsewhen(regCntDone === (n/4 + m - 1).U){ //满了则不再变
    regCntDone := regCntDone
  }.elsewhen(wireDone === true.B || regCntDone > 0.U){
    regCntDone := regCntDone + 1.U
  }.otherwise{
    regCntDone := 0.U
  }

  io.sigDone := Mux(regCntDone === (n/4 + m - 1).U , true.B , false.B)

}


