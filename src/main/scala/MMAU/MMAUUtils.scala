package MMAU

import chisel3._
import chisel3.util._

import common._



//CTRL、MMAU、FSM公共
class FSM_IO extends Bundle {   
    // val sigStart = Input(Bool())    //启动信号，由FSM传入
    // val sigDone = Input(Bool())    //结束信号，由FSM传入
    val firstMuxCtrl = Input(UInt(Consts.m.W)) //muxCtrlC和muxCtrlSum第一个值
    val firstAddrReadA = Input(UInt(Consts.Tr_INDEX_LEN.W))  //AddrReadA第一个值
    val firstAddrReadB = Input(UInt(Consts.Tr_INDEX_LEN.W))  //AddrReadB第一个值
    val firstAddrPublicC = Input(UInt(Consts.Acc_INDEX_LEN.W)) //AddrC第一个读写公共地址
    val firstEnWriteC = Input(Bool()) //C第一个写使能
}

