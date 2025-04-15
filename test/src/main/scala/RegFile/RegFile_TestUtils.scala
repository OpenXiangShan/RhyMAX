package RegFile

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import scala.util.Random

import common._


object apply {
  def writeTestDataToTr(testData: Seq[Seq[UInt]], portIdxWrite: Int, trAddr: Int, dut: RegFile): Unit = { 
    
    dut.io.writeTr(portIdxWrite).act.poke(true.B)// 激活指定的写端口

    
    dut.io.writeTr(portIdxWrite).addr.poke(trAddr.U)  // 定位到目标 Tr 地址

    
    for ((bankData, bankIdx) <- testData.zipWithIndex) {  // 对每个 bank 进行写操作
      for ((data, setIdx) <- bankData.zipWithIndex) {
        // 写入 setIdx 和 data 到对应的 bank
        dut.io.writeTr(portIdxWrite).w(bankIdx).req.bits.setIdx.poke(setIdx.U)
        dut.io.writeTr(portIdxWrite).w(bankIdx).req.bits.data.head.poke(data)
        dut.io.writeTr(portIdxWrite).w(bankIdx).req.valid.poke(true.B)

        dut.clock.step()  // 等待一个 cycle

        dut.io.writeTr(portIdxWrite).w(bankIdx).req.valid.poke(false.B)
      }
    }

    // 注销该写端口
    dut.io.writeTr(portIdxWrite).act.poke(false.B)
  }



  def readTestDataFromTr(expectData: Seq[Seq[UInt]], portIdxRead: Int, trAddr: Int, dut: RegFile): Unit = {
    dut.io.readTr(portIdxRead).act.poke(true.B)  // 激活读端口

    dut.io.readTr(portIdxRead).addr.poke(trAddr.U)  // 定位是哪个Tr
    println(s"Reading Tr address: $trAddr")

    for ((bankData, bankIdx) <- expectData.zipWithIndex) {
      for ((data, setIdx) <- bankData.zipWithIndex) {
        
        dut.io.readTr(portIdxRead).r(bankIdx).req.bits.setIdx.poke(setIdx.U)
        dut.io.readTr(portIdxRead).r(bankIdx).req.valid.poke(true.B)

        dut.clock.step()  // 等待一个cycle

        val readValue = dut.io.readTr(portIdxRead).r(bankIdx).resp.data.head.asUInt.peek()
        println(s"Port $portIdxRead, Bank $bankIdx, Set $setIdx - Read value: ${readValue.litValue}, Expected: ${data.litValue}")

        // 验证读取的值是否符合预期
        dut.io.readTr(portIdxRead).r(bankIdx).resp.data.head.asUInt.expect(data)

        dut.io.readTr(portIdxRead).r(bankIdx).req.valid.poke(false.B)
      }
    }

    dut.io.readTr(portIdxRead).act.poke(false.B)  // 注销该读端口
  }


  def writeTestDataToAcc(testData: Seq[Seq[UInt]], portIdxWrite: Int, accAddr: Int, dut: RegFile): Unit = { 
    
    dut.io.writeAcc(portIdxWrite).act.poke(true.B)// 激活指定的写端口

    
    dut.io.writeAcc(portIdxWrite).addr.poke(accAddr.U)  // 定位到目标 Acc 地址

    
    for ((bankData, bankIdx) <- testData.zipWithIndex) {  // 对每个 bank 进行写操作
      for ((data, setIdx) <- bankData.zipWithIndex) {
        // 写入 setIdx 和 data 到对应的 bank
        dut.io.writeAcc(portIdxWrite).w(bankIdx).req.bits.setIdx.poke(setIdx.U)
        dut.io.writeAcc(portIdxWrite).w(bankIdx).req.bits.data.head.poke(data)
        dut.io.writeAcc(portIdxWrite).w(bankIdx).req.valid.poke(true.B)

        dut.clock.step()  // 等待一个 cycle

        dut.io.writeAcc(portIdxWrite).w(bankIdx).req.valid.poke(false.B)
      }
    }

    // 注销该写端口
    dut.io.writeAcc(portIdxWrite).act.poke(false.B)
  }



  def readTestDataFromAcc(expectData: Seq[Seq[UInt]], portIdxRead: Int, accAddr: Int, dut: RegFile): Unit = {
    dut.io.readAcc(portIdxRead).act.poke(true.B)  // 激活读端口

    dut.io.readAcc(portIdxRead).addr.poke(accAddr.U)  // 定位是哪个Acc
    println(s"Reading Acc address: $accAddr")

    for ((bankData, bankIdx) <- expectData.zipWithIndex) {
      for ((data, setIdx) <- bankData.zipWithIndex) {
        
        dut.io.readAcc(portIdxRead).r(bankIdx).req.bits.setIdx.poke(setIdx.U)
        dut.io.readAcc(portIdxRead).r(bankIdx).req.valid.poke(true.B)

        dut.clock.step()  // 等待一个cycle

        val readValue = dut.io.readAcc(portIdxRead).r(bankIdx).resp.data.head.asUInt.peek()
        println(s"Port $portIdxRead, Bank $bankIdx, Set $setIdx - Read value: ${readValue.litValue}, Expected: ${data.litValue}")

        // 验证读取的值是否符合预期
        dut.io.readAcc(portIdxRead).r(bankIdx).resp.data.head.asUInt.expect(data)

        dut.io.readAcc(portIdxRead).r(bankIdx).req.valid.poke(false.B)
      }
    }

    dut.io.readAcc(portIdxRead).act.poke(false.B)  // 注销该读端口
  }


  def writeTestDataToAll(testData: Seq[Seq[UInt]], portIdxWrite: Int, allAddr: Int, dut: RegFile): Unit = { 
    
    dut.io.writeAll(portIdxWrite).act.poke(true.B)// 激活指定的写端口

    
    dut.io.writeAll(portIdxWrite).addr.poke(allAddr.U)  // 定位到目标 Tr 地址

    
    for ((bankData, bankIdx) <- testData.zipWithIndex) {  // 对每个 bank 进行写操作
      for ((data, setIdx) <- bankData.zipWithIndex) {
        // 写入 setIdx 和 data 到对应的 bank
        dut.io.writeAll(portIdxWrite).w(bankIdx).req.bits.setIdx.poke(setIdx.U)
        dut.io.writeAll(portIdxWrite).w(bankIdx).req.bits.data.head.poke(data)
        dut.io.writeAll(portIdxWrite).w(bankIdx).req.valid.poke(true.B)

        dut.clock.step()  // 等待一个 cycle

        dut.io.writeAll(portIdxWrite).w(bankIdx).req.valid.poke(false.B)
      }
    }

    // 注销该写端口
    dut.io.writeAll(portIdxWrite).act.poke(false.B)
  }


  def readTestDataFromAll(expectData: Seq[Seq[UInt]], portIdxRead: Int, allAddr: Int, dut: RegFile): Unit = {
    dut.io.readAll(portIdxRead).act.poke(true.B)  // 激活读端口

    dut.io.readAll(portIdxRead).addr.poke(allAddr.U)  // 定位是哪个Acc
    println(s"Reading \"All\" address: $allAddr")

    for ((bankData, bankIdx) <- expectData.zipWithIndex) {
      for ((data, setIdx) <- bankData.zipWithIndex) {
        
        dut.io.readAll(portIdxRead).r(bankIdx).req.bits.setIdx.poke(setIdx.U)
        dut.io.readAll(portIdxRead).r(bankIdx).req.valid.poke(true.B)

        dut.clock.step()  // 等待一个cycle

        val readValue = dut.io.readAll(portIdxRead).r(bankIdx).resp.data.head.asUInt.peek()
        println(s"Port $portIdxRead, Bank $bankIdx, Set $setIdx - Read value: ${readValue.litValue}, Expected: ${data.litValue}")

        // 验证读取的值是否符合预期
        dut.io.readAll(portIdxRead).r(bankIdx).resp.data.head.asUInt.expect(data)

        dut.io.readAll(portIdxRead).r(bankIdx).req.valid.poke(false.B)
      }
    }

    dut.io.readAll(portIdxRead).act.poke(false.B)  // 注销该读端口
  }

}
