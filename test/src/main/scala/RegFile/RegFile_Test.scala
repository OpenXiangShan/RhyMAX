package RegFile

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import scala.util.Random

import utility.sram._
import common._
import RegFile._
import MMAU._


class RegFileTestAll extends AnyFreeSpec with Matchers {

  "RegFile should PASS" in {
    simulate(new RegFile) { dut =>

      for (portIdxWrite <- 0 until Consts.numAllWritePort) { //所有写端口都试一遍
        
        //对Tr操作
        val testData1 = MMAUTestData.A 

        for (allAddrW <- 0 until 4) { // 对所有Tr进行写操作
          RegFile.apply.writeTestDataToAll(testData1, portIdxWrite, allAddrW, dut)
        }

        for (portIdxRead <- 0 until Consts.numAllReadPort) { // 写完数据后，进行读取操作（所有port读所有Tr）

          for (allAddrR <- 0 until 4) { // 对所有Tr进行读操作
            RegFile.apply.readTestDataFromAll(testData1, portIdxRead, allAddrR, dut)
          }
        }



        //对Acc操作
        val testData2 = MMAUTestData.C 

        for (allAddrW <- 4 until 8) { // 对所有Tr进行写操作
          RegFile.apply.writeTestDataToAll(testData2, portIdxWrite, allAddrW, dut)
        }

        for (portIdxRead <- 0 until Consts.numAllReadPort) { // 写完数据后，进行读取操作（所有port读所有Tr）

          for (allAddrR <- 4 until 8) { // 对所有Tr进行读操作
            RegFile.apply.readTestDataFromAll(testData2, portIdxRead, allAddrR, dut)
          }
        }



      }
    }
  }
}


class RegFileTestTr extends AnyFreeSpec with Matchers {

  "RegFile should PASS" in {
    simulate(new RegFile) { dut =>

      for (portIdxWrite <- 0 until Consts.numTrWritePort) { //所有写端口都试一遍
        // val testData = if (portIdxWrite == 0) RegFileTestData.testData1 else RegFileTestData.testData2
        val testData = if (portIdxWrite == 0) MMAUTestData.A else MMAUTestData.B

        for (trAddr <- 0 until Consts.numTr) { // 对所有Tr进行写操作
          RegFile.apply.writeTestDataToTr(testData, portIdxWrite, trAddr, dut)
        }

        for (portIdxRead <- 0 until Consts.numTrReadPort) { // 写完数据后，进行读取操作（所有port读所有Tr）

          for (trAddr <- 0 until Consts.numTr) { // 对所有Tr进行读操作
            RegFile.apply.readTestDataFromTr(testData, portIdxRead, trAddr, dut)
          }
        }
      }
    }
  }
}




class RegFileTestAcc extends AnyFreeSpec with Matchers {

  "RegFile should PASS" in {
    simulate(new RegFile) { dut =>

      for (portIdxWrite <- 0 until Consts.numAccWritePort) { //所有写端口都试一遍
        // val testData = if (portIdxWrite == 0) RegFileTestData.testData1 else RegFileTestData.testData2
        val testData = MMAUTestData.C

        for (accAddr <- 0 until Consts.numAcc) { // 对所有Acc进行写操作
          apply.writeTestDataToAcc(testData, portIdxWrite, accAddr, dut)
        }

        for (portIdxRead <- 0 until Consts.numAccReadPort) { // 写完数据后，进行读取操作（所有port读所有Acc）

          for (accAddr <- 0 until Consts.numAcc) { // 对所有Acc进行读操作
            apply.readTestDataFromAcc(testData, portIdxRead, accAddr, dut)
          }
        }
      }
    }
  }
}
