package MMAU

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers


import common._


object apply{

  def checkOutput(dut: MMAU): Unit = {
    // 装填 vecA
    for (i <- 0 until Consts.m) {
      val addrReadA = dut.io.addrReadA(i).peek().litValue.toInt  // 获取地址值
      if(addrReadA < MMAUTestData.A.head.length && addrReadA >= 0){
        dut.io.vecA(i).poke(MMAUTestData.A(i)(addrReadA))  // 根据地址装填数据
      }
      
    }

    // 装填 vecB
    for (i <- 0 until Consts.n) {
      val addrReadB = dut.io.addrReadB(i).peek().litValue.toInt  // 获取地址值
      if(addrReadB < MMAUTestData.B.head.length && addrReadB >= 0){
        dut.io.vecB(i).poke(MMAUTestData.B(i)(addrReadB))  // 根据地址装填数据
      }
      
    }

    dut.clock.step(1) // 推进时钟

    // 装填 vecCin
    for (i <- 0 until Consts.n / 4) {
      val addrReadC = dut.io.addrReadC(i).peek().litValue.toInt  // 获取地址值
      if(addrReadC < MMAUTestData.Ctmp.head.length && addrReadC >= 0){
        dut.io.vecCin(i).poke(MMAUTestData.Ctmp(i)(addrReadC))  // 根据地址装填数据
      }
      
    }

    // 检查 vecCout
    for (i <- 0 until Consts.n / 4) {
      val addrWriteC = dut.io.addrWriteC(i).peek().litValue.toInt  // 获取地址值
      if (dut.io.sigEnWriteC(i).peek().litToBoolean) {  // 检查写使能信号
        dut.io.vecCout(i).expect(MMAUTestData.C(i)(addrWriteC))  // 验证输出
        // val vecCout = dut.io.vecCout(i).peek().litValue.toString(16)
        // println(s"i = $i , addrWriteC = $addrWriteC , sigEnWriteC = ${dut.io.sigEnWriteC(i).peek().litToBoolean} vecCout = $vecCout")
      }
    }

    

  }
  


  def checkOutputLatency(dut: MMAU): Unit = {
    // 暂存上一周期地址
    var prevAddrReadA = Seq.fill(Consts.m)(0)
    var prevAddrReadB = Seq.fill(Consts.n)(0)
    var prevAddrReadC = Seq.fill(Consts.n / 4)(0)

    // 先取当前周期地址（第一个cycle只是暂存地址，不poke）
    for (i <- 0 until Consts.m) {
      prevAddrReadA = prevAddrReadA.updated(i, dut.io.addrReadA(i).peek().litValue.toInt)
    }
    for (i <- 0 until Consts.n) {
      prevAddrReadB = prevAddrReadB.updated(i, dut.io.addrReadB(i).peek().litValue.toInt)
    }
    for (i <- 0 until Consts.n / 4) {
      prevAddrReadC = prevAddrReadC.updated(i, dut.io.addrReadC(i).peek().litValue.toInt)
    }

    
    dut.clock.step(1)// 推进一个 cycle，使用暂存的地址 poke 数据

    for (i <- 0 until Consts.m) {
      val addr = prevAddrReadA(i)
      if (addr < MMAUTestData.A.head.length && addr >= 0) {
        dut.io.vecA(i).poke(MMAUTestData.A(i)(addr))
      }
    }

    for (i <- 0 until Consts.n) {
      val addr = prevAddrReadB(i)
      if (addr < MMAUTestData.B.head.length && addr >= 0) {
        dut.io.vecB(i).poke(MMAUTestData.B(i)(addr))
      }
    }

    for (i <- 0 until Consts.n / 4) {
      val addr = prevAddrReadC(i)
      if (addr < MMAUTestData.Ctmp.head.length && addr >= 0) {
        dut.io.vecCin(i).poke(MMAUTestData.Ctmp(i)(addr))
      }
    }

    // 验证 vecCout，输出验证保持不变
      for (i <- 0 until Consts.n / 4) {
    val addrWriteC = dut.io.addrWriteC(i).peek().litValue.toInt
    if (dut.io.sigEnWriteC(i).peek().litToBoolean) {
      val expectedValue = MMAUTestData.C(i)(addrWriteC)
      dut.io.vecCout(i).expect(expectedValue)
    }
  }


  }


  def printFSM(dut: FSM): Unit = {
    // 打印 muxCtrlC
    println("muxCtrlC:")
    dut.io.muxCtrlC.foreach { row =>
    println(row.map(_.peek().litValue.toInt).mkString(" "))
    }


    // // 打印 muxCtrlSum
    // println("muxCtrlSum:")
    // dut.io.muxCtrlSum.foreach { row =>
    //   println(row.map(_.peek().litValue.toInt).mkString(" "))
    // }

    // 打印 addrReadA（十进制输出）
    val addrReadAStr = dut.io.addrReadA.map(_.peek().litValue.toInt).mkString(" ")
    println(s"addrReadA: $addrReadAStr")

    // 打印 addrReadB（十进制输出）
    val addrReadBStr = dut.io.addrReadB.map(_.peek().litValue.toInt).mkString(" ")
    println(s"addrReadB: $addrReadBStr")

    // 打印 addrReadC（十进制输出）
    val addrReadCStr = dut.io.addrReadC.map(_.peek().litValue.toInt).mkString(" ")
    println(s"addrReadC: $addrReadCStr")

    // 打印 addrWriteC（十进制输出）
    val addrWriteCStr = dut.io.addrWriteC.map(_.peek().litValue.toInt).mkString(" ")
    println(s"addrWriteC: $addrWriteCStr")

    // 打印sigEnWriteC
    // val sigEnWriteCStr = dut.io.sigEnWriteC.map(_.peek().litValue.toBoolean).mkString(" ")
    val sigEnWriteCStr = dut.io.sigEnWriteC.map(_.peek().litToBoolean).mkString(" ")
    println(s"sigEnWriteC = $sigEnWriteCStr")

    // 打印sigDone
    println(s"sigDone = ${dut.io.sigDone.peek().litToBoolean}")
  }
}

