package MMAU

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers


import common._





class MMAUTestExpect extends AnyFreeSpec with Matchers {


  def checkOutput(dut: MMAU): Unit = {
    // 装填 vecA
    for (i <- 0 until Consts.m) {
      val addrReadA = dut.io.addrReadA(i).peek().litValue.toInt  // 获取地址值
      if(addrReadA < TestData.A.head.length && addrReadA >= 0){
        dut.io.vecA(i).poke(TestData.A(i)(addrReadA))  // 根据地址装填数据
      }
      
    }

    // 装填 vecB
    for (i <- 0 until Consts.n) {
      val addrReadB = dut.io.addrReadB(i).peek().litValue.toInt  // 获取地址值
      if(addrReadB < TestData.B.head.length && addrReadB >= 0){
        dut.io.vecB(i).poke(TestData.B(i)(addrReadB))  // 根据地址装填数据
      }
      
    }

    // 推进时钟
    dut.clock.step(1)

    // 装填 vecCin
    for (i <- 0 until Consts.n / 4) {
      val addrReadC = dut.io.addrReadC(i).peek().litValue.toInt  // 获取地址值
      if(addrReadC < TestData.Ctmp.head.length && addrReadC >= 0){
        dut.io.vecCin(i).poke(TestData.Ctmp(i)(addrReadC))  // 根据地址装填数据
        // dut.io.vecCin(i).poke(0.U)
      }
      
    }

    // 检查 vecCout
    for (i <- 0 until Consts.n / 4) {
      val addrWriteC = dut.io.addrWriteC(i).peek().litValue.toInt  // 获取地址值
      if (dut.io.sigEnWriteC(i).peek().litToBoolean) {  // 检查写使能信号
        dut.io.vecCout(i).expect(TestData.C(i)(addrWriteC))  // 验证输出
        // val vecCout = dut.io.vecCout(i).peek().litValue.toString(16)
        // println(s"i = $i , addrWriteC = $addrWriteC , sigEnWriteC = ${dut.io.sigEnWriteC(i).peek().litToBoolean} vecCout = $vecCout")
      }
    }
  }

  "MMAU should PASS" in {
    simulate(new MMAU) { dut =>

      val numM = Consts.tileM / Consts.m
      val numN = Consts.tileN / Consts.n
      val numK = Consts.tileK / Consts.k

      for (i <- 0 until 5) { // 预跑几个cycle
        dut.clock.step(1)
      }

      dut.io.sigStart.poke(true.B)
      dut.clock.step(1)
      dut.io.sigStart.poke(false.B)

      for (i <- 0 until numM) {
        for (j <- 0 until numN) {
          for (p <- 0 until numK) {
            // println(s"mState = $i , nState = $j , kState = $p")
            checkOutput(dut)
            // println("\n")
          }
        }
      }


      for (p <- 0 until numK) {
            // println(s"mState = $i , nState = $j , kState = $p")
            checkOutput(dut)
            // println(s"sigDone = ${dut.io.sigDone.peek().litToBoolean}") // 打印sigDone
            // println("\n")
      }

      for (p <- 0 until numK) {
            // println(s"mState = $i , nState = $j , kState = $p")
            checkOutput(dut)
            // println(s"sigDone = ${dut.io.sigDone.peek().litToBoolean}") // 打印sigDone
            // println("\n")
      }
      

      
    }
  }
}






















