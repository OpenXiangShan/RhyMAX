package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._


//用于处理Tile各维度相关参数
class TileHandler extends MMAUFormat{
  val io = IO(new Bundle {
    // val mtilem = Input(UInt(log2Ceil(tileM+1).W))    //用户配置m维度长度
    // val mtilen = Input(UInt(log2Ceil(tileN+1).W))    //用户配置n维度长度
    // val mtilek = Input(UInt(log2Ceil(tileK+1).W))    //用户配置k维度长度
    val mtileConfig_io = new mtileConfig_IO

    // val tilem = Output(UInt(log2Ceil(tileM+1).W))   //padding后m维度的长度，供计算单元使用
    // val tilen = Output(UInt(log2Ceil(tileN+1).W))   //padding后n维度的长度，供计算单元使用
    // val tilek = Output(UInt(log2Ceil(tileK+1).W))   //padding后k维度的长度，供计算单元使用
    // val numm = Output(UInt(log2Ceil(numM+1).W))     
    // val numn = Output(UInt(log2Ceil(numN+1).W))
    // val numk = Output(UInt(log2Ceil(numK+1).W))
    val TileHandler_io = new TileHandler_IO 
  })

  /*对mnk维度进行padding*/
  val tilem = applyTileHandler.ceilAlign(io.mtileConfig_io.mtilem, m.U)
  io.TileHandler_io.tilem := tilem

  val tilen = applyTileHandler.ceilAlign(io.mtileConfig_io.mtilen, n.U)
  io.TileHandler_io.tilen := tilen

  val tilek = Wire(UInt(log2Ceil(tileK+1).W))
  when(io.mtileConfig_io.mtilek < (m * k).U) {
        tilek := (m * k).U
    }.otherwise {
        tilek := applyTileHandler.ceilAlign(io.mtileConfig_io.mtilek, k.U)
    }
  io.TileHandler_io.tilek := tilek
  
  /*计算新的num*/
  io.TileHandler_io.numm := tilem / m.U

  io.TileHandler_io.numn := tilen / n.U

  io.TileHandler_io.numk := tilek / k.U
}