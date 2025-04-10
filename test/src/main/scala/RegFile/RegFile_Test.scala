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


class RegFileTest extends AnyFreeSpec with Matchers {

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




// class RegFileTest extends AnyFreeSpec with Matchers {

//   "RegFile should PASS" in {
//     simulate(new RegFile) { dut =>

//       for (portIdxWrite <- 0 until Consts.numAccWritePort) { //所有写端口都试一遍
//         // val testData = if (portIdxWrite == 0) RegFileTestData.testData1 else RegFileTestData.testData2
//         val testData = MMAUTestData.C

//         for (accAddr <- 0 until Consts.numAcc) { // 对所有Acc进行写操作
//           apply.writeTestDataToAcc(testData, portIdxWrite, accAddr, dut)
//         }

//         for (portIdxRead <- 0 until Consts.numAccReadPort) { // 写完数据后，进行读取操作（所有port读所有Acc）

//           for (accAddr <- 0 until Consts.numAcc) { // 对所有Acc进行读操作
//             apply.readTestDataFromAcc(testData, portIdxRead, accAddr, dut)
//           }
//         }
//       }
//     }
//   }
// }
