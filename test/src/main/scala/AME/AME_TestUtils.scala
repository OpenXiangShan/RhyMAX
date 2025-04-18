package AME

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import scala.util.Random

import common._




object apply {

  // // 写入 Tr 数据
  // def writeTestDataToTr(testData: Seq[Seq[UInt]], trAddr: Int, dut: AME): Unit = {
  //   // 激活写端口
  //   dut.io.writeTr.act.poke(true.B)
  //   dut.io.writeTr.addr.poke(trAddr.U) // 定位到目标 Tr 地址

  //   for ((bankData, bankIdx) <- testData.zipWithIndex) { // 对每个 bank 进行写操作
  //     for ((data, setIdx) <- bankData.zipWithIndex) {
  //       // 写入 setIdx 和 data 到对应的 bank
  //       dut.io.writeTr.w(bankIdx).req.bits.setIdx.poke(setIdx.U)
  //       dut.io.writeTr.w(bankIdx).req.bits.data.head.poke(data)
  //       dut.io.writeTr.w(bankIdx).req.valid.poke(true.B)

  //       dut.clock.step() // 等待一个 cycle

  //       dut.io.writeTr.w(bankIdx).req.valid.poke(false.B)
  //     }
  //   }

  //   // 注销写端口
  //   dut.io.writeTr.act.poke(false.B)
  // }

  // // 写入 Acc 数据
  // def writeTestDataToAcc(testData: Seq[Seq[UInt]], accAddr: Int, dut: AME): Unit = {
  //   // 激活写端口
  //   dut.io.writeAcc.act.poke(true.B)
  //   dut.io.writeAcc.addr.poke(accAddr.U) // 定位到目标 Acc 地址

  //   for ((bankData, bankIdx) <- testData.zipWithIndex) { // 对每个 bank 进行写操作
  //     for ((data, setIdx) <- bankData.zipWithIndex) {
  //       dut.io.writeAcc.w(bankIdx).req.bits.setIdx.poke(setIdx.U)
  //       dut.io.writeAcc.w(bankIdx).req.bits.data.head.poke(data)
  //       dut.io.writeAcc.w(bankIdx).req.valid.poke(true.B)

  //       dut.clock.step() // 等待一个 cycle

  //       dut.io.writeAcc.w(bankIdx).req.valid.poke(false.B)
  //     }
  //   }

  //   // 注销写端口
  //   dut.io.writeAcc.act.poke(false.B)
  // }

  // 写入 All 数据
  def writeTestDataToAll(testData: Seq[Seq[UInt]], allAddr: Int, dut: AME): Unit = {
    // 激活写端口
    dut.io.writeAll.act.poke(true.B)
    dut.io.writeAll.addr.poke(allAddr.U) // 定位到目标 Reg 地址

    for ((bankData, bankIdx) <- testData.zipWithIndex) { // 对每个 bank 进行写操作
      for ((data, setIdx) <- bankData.zipWithIndex) {
        // 写入 setIdx 和 data 到对应的 bank
        dut.io.writeAll.w(bankIdx).req.bits.setIdx.poke(setIdx.U)
        dut.io.writeAll.w(bankIdx).req.bits.data.head.poke(data)
        dut.io.writeAll.w(bankIdx).req.valid.poke(true.B)

        dut.clock.step() // 等待一个 cycle

        dut.io.writeAll.w(bankIdx).req.valid.poke(false.B)
      }
    }

    // 注销写端口
    dut.io.writeAll.act.poke(false.B)
  }


  // // 读取 Tr 数据
  // def readTestDataFromTr(expectData: Seq[Seq[UInt]], trAddr: Int, dut: AME): Unit = {
  //   dut.io.readTr.act.poke(true.B) // 复用写接口作为读接口，或者你有独立读接口就改成对应的端口
  //   dut.io.readTr.addr.poke(trAddr.U) // 定位到目标 Tr 地址
  //   println(s"Reading Tr address: $trAddr")

  //   for ((bankData, bankIdx) <- expectData.zipWithIndex) {
  //     for ((data, setIdx) <- bankData.zipWithIndex) {
  //       dut.io.readTr.r(bankIdx).req.bits.setIdx.poke(setIdx.U)
  //       dut.io.readTr.r(bankIdx).req.valid.poke(true.B)

  //       dut.clock.step() // 等待一个 cycle

  //       val readValue = dut.io.readTr.r(bankIdx).resp.data.head.asUInt.peek()
  //       println(s"Bank $bankIdx, Set $setIdx - Read value: ${readValue.litValue}, Expected: ${data.litValue}")

  //       dut.io.readTr.r(bankIdx).resp.data.head.asUInt.expect(data)

  //       dut.io.readTr.r(bankIdx).req.valid.poke(false.B)
  //     }
  //   }

  //   dut.io.readTr.act.poke(false.B)
  // }

  // // 读取 Acc 数据
  // def readTestDataFromAcc(expectData: Seq[Seq[UInt]], accAddr: Int, dut: AME): Unit = {
  //   dut.io.readAcc.act.poke(true.B)
  //   dut.io.readAcc.addr.poke(accAddr.U)
  //   println(s"Reading Acc address: $accAddr")

  //   for ((bankData, bankIdx) <- expectData.zipWithIndex) {
  //     for ((data, setIdx) <- bankData.zipWithIndex) {
  //       dut.io.readAcc.r(bankIdx).req.bits.setIdx.poke(setIdx.U)
  //       dut.io.readAcc.r(bankIdx).req.valid.poke(true.B)

  //       dut.clock.step() // 等待一个 cycle

  //       val readValue = dut.io.readAcc.r(bankIdx).resp.data.head.asUInt.peek()
  //       println(s"Bank $bankIdx, Set $setIdx - Read value: ${readValue.litValue}, Expected: ${data.litValue}")

  //       dut.io.readAcc.r(bankIdx).resp.data.head.asUInt.expect(data)

  //       dut.io.readAcc.r(bankIdx).req.valid.poke(false.B)
  //     }
  //   }

  //   dut.io.readAcc.act.poke(false.B)
  // }

  


  // 读取 All 数据
  def readTestDataFromAll(expectData: Seq[Seq[UInt]], allAddr: Int, dut: AME): Unit = {
    dut.io.readAll.act.poke(true.B) // 复用写接口作为读接口，或者你有独立读接口就改成对应的端口
    dut.io.readAll.addr.poke(allAddr.U) // 定位到目标 Tr 地址
    println(s"Reading \"all\" address: $allAddr")

    for ((bankData, bankIdx) <- expectData.zipWithIndex) {
      for ((data, setIdx) <- bankData.zipWithIndex) {
        dut.io.readAll.r(bankIdx).req.bits.setIdx.poke(setIdx.U)
        dut.io.readAll.r(bankIdx).req.valid.poke(true.B)

        dut.clock.step() // 等待一个 cycle

        val readValue = dut.io.readAll.r(bankIdx).resp.data.head.asUInt.peek()
        val status = if (readValue.litValue == data.litValue) "PASS" else "FAILED"
        println(s"Bank $bankIdx, Set $setIdx - Read value: ${readValue.litValue}, Expected: ${data.litValue} [$status]")

        dut.io.readAll.r(bankIdx).resp.data.head.asUInt.expect(data)

        dut.io.readAll.r(bankIdx).req.valid.poke(false.B)
      }
    }

    dut.io.readAll.act.poke(false.B)
  }










  def AMEStart(dut: AME , mtilem: Int , mtilen: Int , mtilek: Int): Unit = {  //启动AME
    dut.io.sigStart.poke(true.B)  //启动信号

    // 输入用户配置尺寸
    dut.io.mtileConfig_io.mtilem.poke(mtilem.U)
    dut.io.mtileConfig_io.mtilen.poke(mtilen.U)
    dut.io.mtileConfig_io.mtilek.poke(mtilek.U)

    dut.clock.step(1)

    dut.io.sigStart.poke(false.B)
  }

}
