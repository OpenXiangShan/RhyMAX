package Expander

import chisel3._
import chisel3.util._

import common._
import MMAU._


// //用于处理Tile各维度相关参数
class TileHandler_MMAU extends MMAUFormat {
  val io = IO(new Bundle {
    val mtileConfig_io = new mtileConfig_IO   //用户配置尺寸
    val TileHandler_MMAU_io = new TileHandler_MMAU_IO   //padding后的numm,numn,numk
  })

  // 计算 log2 常量（m/n/k 是 2 的幂次）
  val log2m = log2Ceil(m)
  val log2n = log2Ceil(n)
  val log2k = log2Ceil(k)

  // padding 向上补齐，避免使用除法
  val tilem = applyTileHandler.ceilAlignPow2(io.mtileConfig_io.mtilem, log2m)
  val tilen = applyTileHandler.ceilAlignPow2(io.mtileConfig_io.mtilen, log2n)

  val tilek = Wire(UInt(log2Ceil(tileK + 1).W))
  when(io.mtileConfig_io.mtilek < (m * k).U) {
    tilek := (m * k).U
  }.otherwise {
    tilek := applyTileHandler.ceilAlignPow2(io.mtileConfig_io.mtilek, log2k)
  }

  // io.TileHandler_MMAU_io.tilem := tilem
  // io.TileHandler_MMAU_io.tilen := tilen
  // io.TileHandler_MMAU_io.tilek := tilek

  // 替代除法为右移
  io.TileHandler_MMAU_io.numm := tilem >> log2m.U
  io.TileHandler_MMAU_io.numn := tilen >> log2n.U
  io.TileHandler_MMAU_io.numk := tilek >> log2k.U
}
