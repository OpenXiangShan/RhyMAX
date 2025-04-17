package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._


object applyTileHandler{
  // 向上补齐到对齐数 align 的倍数
  def ceilAlign(x: UInt, align: UInt): UInt = {
    val a = align
    ((x + a - 1.U) / a) * a
  }
}


class TileHandler_IO extends Bundle{
    val tilem = Output(UInt(log2Ceil(Consts.tileM+1).W))   //padding后m维度的长度，供计算单元使用
    val tilen = Output(UInt(log2Ceil(Consts.tileN+1).W))   //padding后n维度的长度，供计算单元使用
    val tilek = Output(UInt(log2Ceil(Consts.tileK+1).W))   //padding后k维度的长度，供计算单元使用
    val numm = Output(UInt(log2Ceil(Consts.numM+1).W))     
    val numn = Output(UInt(log2Ceil(Consts.numN+1).W))
    val numk = Output(UInt(log2Ceil(Consts.numK+1).W))
}

class mtileConfig_IO extends Bundle{
  val mtilem = Input(UInt(log2Ceil(Consts.tileM+1).W))    //用户配置m维度长度
  val mtilen = Input(UInt(log2Ceil(Consts.tileN+1).W))    //用户配置n维度长度
  val mtilek = Input(UInt(log2Ceil(Consts.tileK+1).W))    //用户配置k维度长度
}