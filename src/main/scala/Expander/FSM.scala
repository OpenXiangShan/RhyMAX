package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._






//MMAU latency = numm * numn * numk + 2*sramlatency + m + n/4 -2
class FSM_MMAU extends MMAUFormat{
  val io = IO(new Bundle {
    val sigStart = Input(Bool())    //启动信号
    val Ops_io = new Ops_IO //操作数矩阵

    // val firstMuxCtrl = Input(UInt(Consts.m.W)) //muxCtrlC和muxCtrlSum第一个值
    // val firstAddrReadA = Input(UInt(Consts.Tr_INDEX_LEN.W))  //AddrReadA第一个值
    // val firstAddrReadB = Input(UInt(Consts.Tr_INDEX_LEN.W))  //AddrReadB第一个值
    // val firstAddrPublicC = Input(UInt(Consts.Acc_INDEX_LEN.W)) //AddrC第一个读写公共地址
    // val firstEnWriteC = Input(Bool()) //C第一个写使能
    val FSM_io = Flipped(new FSM_IO) //输入计算单元的信号，均为寄存器链首数据
    val TileHandler_io = Flipped(new TileHandler_IO)

    val sigDone = Output(Bool())    //结束信号

  })

  /*  表示工作状态或空闲状态 */
  val regFSM_is_busy = RegInit(false.B) //true：工作状态；false：空闲状态，此时C写使能必为false



  /*  表示工作状态内部的状态  */
  val regOffM = RegInit(0.U(log2Ceil(numM).W))   //log2,向上取整
  val regOffN = RegInit(0.U(log2Ceil(numN).W))
  val regOffK = RegInit(0.U(log2Ceil(numK).W))   //表示nState内部的numK个kState状态
  val regOffK_1H = RegInit(1.U(numK.W))    //独热编码，表示nState内部的numK个kState状态
  
  
  regOffM := Mux(io.sigStart === true.B , 0.U , 
                Mux(regOffN === io.TileHandler_io.numn - 1.U && regOffK === io.TileHandler_io.numk - 1.U , 
                  Mux(regOffM === io.TileHandler_io.numm - 1.U , 0.U , regOffM + 1.U) , regOffM)) //可能会有一定开销，可以加一个regOffN_1H优化

  regOffN := Mux(io.sigStart === true.B , 0.U ,
                Mux(regOffK === io.TileHandler_io.numk - 1.U , 
                  Mux(regOffN === io.TileHandler_io.numn - 1.U , 0.U , regOffN + 1.U) , regOffN))    //regOffK最大时更新

  regOffK := Mux(io.sigStart === true.B , 0.U , 
                Mux(regOffK === io.TileHandler_io.numk - 1.U , 0.U , regOffK + 1.U))  //递增，循环，最大到numk-1
  
  regOffK_1H := Mux(io.sigStart === true.B , 1.U , 
                  Mux(regOffK === io.TileHandler_io.numk - 1.U , 1.U , Cat(regOffK_1H(numK-2 , 0) , regOffK_1H(numK-1))))   //循环左移,最左到bit[numk-1]
  
  
  /*    muxCtrlC muxCtrlSum    */

  io.FSM_io.firstMuxCtrl := regOffK_1H(m-1 , 0)

  /*    read matrixA    */
  
  io.FSM_io.firstAddrReadA := regOffM * (numK).U + regOffK


  /*    read matrixB    */

  io.FSM_io.firstAddrReadB := regOffN * (numK).U + regOffK


  /*    read & write matrixC    */

  
  val wireNumStep = Wire(UInt(log2Ceil(numM * numN).W))

  when(regOffN === 0.U && regOffM === 0.U){
    wireNumStep := (io.TileHandler_io.numm - 1.U) * numN.U + (io.TileHandler_io.numn - 1.U)
  }.elsewhen(regOffN === 0.U){
    wireNumStep := numN.U * regOffM + regOffN - (numN.U - io.TileHandler_io.numn + 1.U)
  }.otherwise{
    wireNumStep := numN.U * regOffM + regOffN - 1.U
  }
  io.FSM_io.firstAddrPublicC := wireNumStep * m.U + regOffK  //只有kState(0) ~ kState(m-1) 的数据是有意义的

  //write C enable

  val regCntZero = RegInit(0.U(1.W))  //记录kState为 numk-1 的次数,1次之后不再更新
  when(io.sigStart === true.B){
    regCntZero := 0.U
  }.elsewhen(regCntZero === 1.U){
    regCntZero := regCntZero
  }.elsewhen(regOffK === io.TileHandler_io.numk - 1.U){
    regCntZero := regCntZero + 1.U
  }.otherwise{
    regCntZero := regCntZero
  }

  val regCntDone = RegInit(0.U(log2Ceil(n/4 + m).W)) //上电时即为满，是属于sigDone相关寄存器，C的第一个bank的index0结束后（regCntDone开始计时）,还需等待n/4 + m 个周期,所有bank结束

  when(m.U < io.TileHandler_io.numk){
    io.FSM_io.firstEnWriteC := Mux(regCntZero === 1.U && regOffK >= 0.U && regOffK <= (m-1).U && regCntDone < m.U - sramLatency.U && regFSM_is_busy, true.B , false.B)
  }.otherwise{
    io.FSM_io.firstEnWriteC := Mux(regCntZero === 1.U && regCntDone < m.U - sramLatency.U && regFSM_is_busy, true.B , false.B)
  }


  /*    signal done    */
  
  val wireDone = Wire(Bool()) //C的第一个bank的index0结束
  wireDone := Mux(regOffM === 0.U && regOffN === 0.U && regOffK === 1.U && regCntZero === 1.U && regFSM_is_busy === true.B, true.B , false.B)  //仅在工作时才有意义



  //结束时Done拉高，随后拉低
  when(io.sigStart === true.B){
    regCntDone := 0.U
  }.elsewhen(regCntDone === (n/4 + m - 1).U){ //满了则归零，Done信号不会一直拉高
    regCntDone := 0.U
  }.elsewhen(wireDone === true.B || regCntDone > 0.U){
    regCntDone := regCntDone + 1.U
  }.otherwise{
    regCntDone := regCntDone
  }

  val wireSigDone = Wire(Bool())
  wireSigDone := Mux(regCntDone === (n/4 + m - 1).U , true.B , false.B)
  io.sigDone := wireSigDone //上电时，默认输出true



  /*  表示FSM是否工作 */
  //由空闲进入工作只能是start拉高，反之只能是done拉高
  when(io.sigStart){
    regFSM_is_busy := true.B  //start拉高，进入工作状态
  }.elsewhen(wireSigDone){
    regFSM_is_busy := false.B //done为真，进入空闲状态
  }.otherwise{
    regFSM_is_busy := regFSM_is_busy
  }



  /*    actPort   */
  val regActPort = RegInit(false.B)

  when(io.sigStart){  //仅在启动和结束之间的时间激活
    regActPort := true.B
  }.elsewhen(io.sigDone){
    regActPort := false.B
  }.otherwise{
    regActPort := regActPort
  }

  io.FSM_io.actPortReadA := regActPort
  io.FSM_io.actPortReadB := regActPort
  io.FSM_io.actPortReadC := regActPort
  io.FSM_io.actPortWriteC := regActPort

  /*    Ops   */
  io.FSM_io.Ops_io <> io.Ops_io

  // 打印提示信息（仅仿真有效）
  // when(io.FSM_io.firstEnWriteC) {
  //   printf(p"[INFO] firstEnWriteC is HIGH at cycle, offK = ${regOffK}, offN = ${regOffN}, offM = ${regOffM}\n")
  // }

  // printf(p"offK = ${regOffK}, offN = ${regOffN}, offM = ${regOffM}    \n")
  // printf(p"addrA = ${io.FSM_io.firstAddrReadA} , addrB = ${io.FSM_io.firstAddrReadB} , addrC = ${io.FSM_io.firstAddrPublicC}\n")
  // printf(p"sigStart = ${io.sigStart} , sigDone = ${io.sigDone}  , regCntDone = ${regCntDone}  regFSM_is_busy = ${regFSM_is_busy}\n")
}




