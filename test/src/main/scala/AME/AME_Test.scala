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
      AME.apply.writeTestDataToAll(MMAUTestData.A, 0, dut)
      AME.apply.writeTestDataToAll(MMAUTestData.B, 1, dut)
      AME.apply.writeTestDataToAll(MMAUTestData.Ctmp, 4, dut)

      dut.clock.step(1000) //随便跑几个cycle

      AME.apply.AMEStart(dut, 32, 32, 64)//启动AME

      var cycleCount = 0

      while(!dut.io.sigDone.peek().litToBoolean){
        dut.clock.step(1)
        cycleCount += 1
      }

      println(s"Total cycles: $cycleCount")

      AME.apply.readTestDataFromAll(MMAUTestData.C, 4, dut) //验证结果是否正确


      /***********************************************************************************/

      // /*  跑第二条指令，看是否存在“脏数据”问题  */
      // println(s"run again")
      
      // AME.apply.writeTestDataToAll(MMAUTestData.Ctmp, 4, dut)

      // AME.apply.AMEStart(dut)//启动AME

      // while(!dut.io.sigDone.peek().litToBoolean){
      //   dut.clock.step(1)
      //   // println(s"run in AME")
      // }

      // AME.apply.readTestDataFromAll(MMAUTestData.C, 4, dut) //验证结果是否正确

      


    }
  }
}


// class AMETest extends AnyFreeSpec with Matchers {

//   "AME should PASS" in {
//     simulate(new AME) { dut =>
    
      
//       /*  提前手动写入A、B、C*/
//       AME.apply.writeTestDataToTr(MMAUTestData.A, 0, dut)
//       AME.apply.writeTestDataToTr(MMAUTestData.B, 1, dut)
//       AME.apply.writeTestDataToAcc(MMAUTestData.Ctmp, 0, dut)
//       // AME.apply.readTestDataFromTr(MMAUTestData.A, 0, dut)
//       // AME.apply.readTestDataFromTr(MMAUTestData.B, 1, dut)
//       // AME.apply.readTestDataFromAcc(MMAUTestData.Ctmp, 0, dut)

//       dut.clock.step(1000) //随便跑几个cycle

//       AME.apply.AMEStart(dut)//启动AME

//       while(!dut.io.sigDone.peek().litToBoolean){
//         dut.clock.step(1)
//         // println(s"run in AME")
//       }

//       AME.apply.readTestDataFromAcc(MMAUTestData.C, 0, dut)
      


//     }
//   }
// }



