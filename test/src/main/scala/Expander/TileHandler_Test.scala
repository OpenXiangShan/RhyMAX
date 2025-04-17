package Expander

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers


import common._





class TileHandlerTest extends AnyFreeSpec with Matchers {

  "TileHandler should PASS" in {
    simulate(new TileHandler) { dut =>
    
      // 输入用户配置尺寸
      dut.io.mtileConfig_io.mtilem.poke(16.U)
      dut.io.mtileConfig_io.mtilen.poke(16.U)
      dut.io.mtileConfig_io.mtilek.poke(64.U)

      // 打印输出，不前进时钟
      println(s"tilem = ${dut.io.TileHandler_io.tilem.peek().litValue}")
      println(s"tilen = ${dut.io.TileHandler_io.tilen.peek().litValue}")
      println(s"tilek = ${dut.io.TileHandler_io.tilek.peek().litValue}")
      println(s"numm  = ${dut.io.TileHandler_io.numm.peek().litValue}")
      println(s"numn  = ${dut.io.TileHandler_io.numn.peek().litValue}")
      println(s"numk  = ${dut.io.TileHandler_io.numk.peek().litValue}")
      
    }
  }
}
