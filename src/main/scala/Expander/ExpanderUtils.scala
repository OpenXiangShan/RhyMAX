package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._








class ShakeHands_IO extends Bundle{//握手
  val valid = Input(Bool())
  val ready = Output(Bool())
}

class Operands_IO extends Bundle{//译码信号
  val ms1 = Input(UInt(Consts.All_ADDR_LEN.W))
  val ms2 = Input(UInt(Consts.All_ADDR_LEN.W))
  val md = Input(UInt(Consts.All_ADDR_LEN.W))
}

class InsType_IO extends Bundle{//指令类型
  val is_mmacc = Input(Bool())      //整型矩阵乘
}

class ScoreboardVisit_IO extends Bundle{//与计分板交互
  val read_RF = Input(UInt(8.W))    //读取RF资源使用情况
  val read_Unit = Input(UInt(4.W))  //读取功能单元使用情况
  val writeMaskAlloc_RF = Output(UInt(8.W))   //分配RF，掩码形式  
  val writeMaskAlloc_Unit = Output(UInt(4.W)) //分配功能单元，掩码形式
  val writeMaskFree_RF = Output(UInt(8.W))   //释放RF，掩码形式  
  val writeMaskFree_Unit = Output(UInt(4.W)) //释放功能单元，掩码形式
}






/*    TileHandler   */


object applyTileHandler {
  // 向上补齐到 2^log2align 的倍数（不使用除法）
  def ceilAlignPow2(x: UInt, log2align: Int): UInt = {
    val mask = ((1 << log2align) - 1).U(x.getWidth.W)
    (x + mask) & (~mask)
  }
}




class TileHandler_IO extends Bundle{
    // val tilem = Output(UInt(log2Ceil(Consts.tileM+1).W))   //padding后m维度的长度，供计算单元使用
    // val tilen = Output(UInt(log2Ceil(Consts.tileN+1).W))   //padding后n维度的长度，供计算单元使用
    // val tilek = Output(UInt(log2Ceil(Consts.tileK+1).W))   //padding后k维度的长度，供计算单元使用
    val numm = Output(UInt(log2Ceil(Consts.numM+1).W))     
    val numn = Output(UInt(log2Ceil(Consts.numN+1).W))
    val numk = Output(UInt(log2Ceil(Consts.numK+1).W))
}

class mtileConfig_IO extends Bundle{
  val mtilem = Input(UInt(log2Ceil(Consts.tileM+1).W))    //用户配置m维度长度
  val mtilen = Input(UInt(log2Ceil(Consts.tileN+1).W))    //用户配置n维度长度
  val mtilek = Input(UInt(log2Ceil(Consts.tileK+1).W))    //用户配置k维度长度
}





/*    FSM   */

//CTRL、MMAU、FSM公共
class FSM_IO extends Bundle {   
  // val sigStart = Input(Bool())    //启动信号，由FSM传入
  // val sigDone = Input(Bool())    //结束信号，由FSM传入
  val firstMuxCtrl = Input(UInt(Consts.m.W)) //muxCtrlC和muxCtrlSum第一个值
  val firstAddrReadA = Input(UInt(Consts.Tr_INDEX_LEN.W))  //AddrReadA第一个值
  val firstAddrReadB = Input(UInt(Consts.Tr_INDEX_LEN.W))  //AddrReadB第一个值
  val firstAddrPublicC = Input(UInt(Consts.Acc_INDEX_LEN.W)) //AddrC第一个读写公共地址
  val firstEnWriteC = Input(Bool()) //C第一个写使能
  val actPortReadA = Input(Bool())  //A读端口激活
  val actPortReadB = Input(Bool())  //B读端口激活
  val actPortReadC = Input(Bool())  //C读端口激活
  val actPortWriteC = Input(Bool())  //C写端口激活
  // val ms1 = Input(UInt(Consts.Tr_ADDR_LEN.W))
  // val ms2 = Input(UInt(Consts.Tr_ADDR_LEN.W))
  // val md = Input(UInt(Consts.Acc_ADDR_LEN.W))
  val Ops_io = new Ops_IO
}

class Ops_IO extends Bundle{
  val ms1 = Input(UInt(Consts.Tr_ADDR_LEN.W))
  val ms2 = Input(UInt(Consts.Tr_ADDR_LEN.W))
  val md = Input(UInt(Consts.Acc_ADDR_LEN.W))
}



//IssueMMAU

class IssueMMAU_Excute_IO extends Bundle{//连接ExcuteHandler
  val sigStart = Input(Bool())    //启动信号
  val is_shaked = Input(Bool()) //是否握手成功
  val sigDone = Output(Bool())    //结束信号
  val ms1 = Output(UInt(Consts.All_ADDR_LEN.W))
  val ms2 = Output(UInt(Consts.All_ADDR_LEN.W))
  val md = Output(UInt(Consts.All_ADDR_LEN.W))
}