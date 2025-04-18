package AME

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






class AMETest extends AnyFreeSpec with Matchers {

  "AME should PASS" in {
    simulate(new AME) { dut =>
    
      
      /*  提前手动写入A、B、C*/
      AME.apply.writeTestDataToAll(AMETestData.A, 0, dut)
      AME.apply.writeTestDataToAll(AMETestData.B, 1, dut)
      AME.apply.writeTestDataToAll(AMETestData.Ctmp, 4, dut)

      dut.clock.step(1000) //随便跑几个cycle

      AME.apply.AMEStart(dut, 13, 12, 31)//启动AME

      var cycleCount = 0

      while(!dut.io.sigDone.peek().litToBoolean){
        dut.clock.step(1)
        cycleCount += 1
      }

      

      AME.apply.readTestDataFromAll(AMETestData.C, 4, dut) //验证结果是否正确


      println(s"Total cycles: $cycleCount")


      /***********************************************************************************/

      // /*  跑第二条指令，看是否存在“脏数据”问题  */
      // println(s"run again")
      
      // AME.apply.writeTestDataToAll(AMETestData.Ctmp, 4, dut)

      // AME.apply.AMEStart(dut)//启动AME

      // while(!dut.io.sigDone.peek().litToBoolean){
      //   dut.clock.step(1)
      //   // println(s"run in AME")
      // }

      // AME.apply.readTestDataFromAll(AMETestData.C, 4, dut) //验证结果是否正确

      


    }
  }
}


// class AMETest extends AnyFreeSpec with Matchers {

//   "AME should PASS" in {
//     simulate(new AME) { dut =>
    
      
//       /*  提前手动写入A、B、C*/
//       AME.apply.writeTestDataToTr(AMETestData.A, 0, dut)
//       AME.apply.writeTestDataToTr(AMETestData.B, 1, dut)
//       AME.apply.writeTestDataToAcc(AMETestData.Ctmp, 0, dut)
//       // AME.apply.readTestDataFromTr(AMETestData.A, 0, dut)
//       // AME.apply.readTestDataFromTr(AMETestData.B, 1, dut)
//       // AME.apply.readTestDataFromAcc(AMETestData.Ctmp, 0, dut)

//       dut.clock.step(1000) //随便跑几个cycle

//       AME.apply.AMEStart(dut)//启动AME

//       while(!dut.io.sigDone.peek().litToBoolean){
//         dut.clock.step(1)
//         // println(s"run in AME")
//       }

//       AME.apply.readTestDataFromAcc(AMETestData.C, 0, dut)
      


//     }
//   }
// }



