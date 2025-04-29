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



//完整Expander版本
class AMETest extends AnyFreeSpec with Matchers {

  "AME should PASS" in {
    simulate(new AME) { dut =>
    
      
      /*  提前手动写入A、B、C*/
      AME.apply.writeTestDataToAll(AMETestData.A, 0, dut)
      AME.apply.writeTestDataToAll(AMETestData.B, 1, dut)
      AME.apply.writeTestDataToAll(AMETestData.Ctmp, 4, dut)
      AME.apply.writeTestDataToAll(AMETestData.Ctmp, 5, dut)
      AME.apply.writeTestDataToAll(AMETestData.Ctmp, 6, dut)
      AME.apply.writeTestDataToAll(AMETestData.Ctmp, 7, dut)

      dut.clock.step(1000) //随便跑几个cycle

      



      // // ins1
      var cycleCountMMAU = 0
      var cycleCountReady = 0

      AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 4, 0, 0, true.B, true.B, false.B)  //启动AME，配置矩阵形状，确定操作数矩阵标号（ABC标号范围均是0～7)

      while(!dut.io.Uop_io.ShakeHands_io.ready.peek().litToBoolean){ //等到ready
        dut.clock.step(1)
        cycleCountReady += 1
      }

      dut.clock.step(1) //ready后需要主动前进一个时钟周期

println(s"ins 1 excuting")


      AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 5, 0, 0, true.B, true.B, false.B)  //下一条指令ins2，应该没有响应


      while(!dut.io.sigDone.peek().litToBoolean){ //等到执行完毕
        dut.clock.step(1)
        cycleCountMMAU += 1
      }


      println(s"cycleCountReady = $cycleCountReady , cycleCountMMAU = $cycleCountMMAU")


      // ins2

      cycleCountReady = 0
      cycleCountMMAU = 0  //计数清零


      // AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 5, true.B, true.B)  //下一条指令，换一个C

      while(!dut.io.Uop_io.ShakeHands_io.ready.peek().litToBoolean){ //等到ready
        dut.clock.step(1)
        cycleCountReady += 1
      }

      dut.clock.step(1) //ready后需要主动前进一个时钟周期

println(s"ins 2 excuting")

      AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 6, 0, 0, true.B, true.B, false.B)  //下一条指令ins3，应该没有响应

      while(!dut.io.sigDone.peek().litToBoolean){ //等到执行完毕
        dut.clock.step(1)
        cycleCountMMAU += 1
      }

      println(s"cycleCountReady = $cycleCountReady , cycleCountMMAU = $cycleCountMMAU")


      // ins3

      cycleCountReady = 0
      cycleCountMMAU = 0  //计数清零


      // AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 6, true.B, true.B)  //下一条指令，换一个C

      while(!dut.io.Uop_io.ShakeHands_io.ready.peek().litToBoolean){ //等到ready
        dut.clock.step(1)
        cycleCountReady += 1
      }

      dut.clock.step(1) //ready后需要主动前进一个时钟周期

println(s"ins 3 excuting")

      while(!dut.io.sigDone.peek().litToBoolean){ //等到执行完毕
        dut.clock.step(1)
        cycleCountMMAU += 1
      }

      println(s"cycleCountReady = $cycleCountReady , cycleCountMMAU = $cycleCountMMAU")



      

      AME.apply.AMEStop(dut)  //停止，无效指令，valid = false

      AME.apply.readTestDataFromAll(AMETestData.C, 4, dut) //验证结果是否正确
      AME.apply.readTestDataFromAll(AMETestData.C, 5, dut) //验证结果是否正确
      AME.apply.readTestDataFromAll(AMETestData.C, 6, dut) //验证结果是否正确
      // AME.apply.readTestDataFromAll(AMETestData.C, 7, dut) //验证结果是否正确


 


    }
  }
}



// //完整Expander版本,带恶心的debug
// class AMETest extends AnyFreeSpec with Matchers {

//   "AME should PASS" in {
//     simulate(new AME) { dut =>
    
      
//       /*  提前手动写入A、B、C*/
//       AME.apply.writeTestDataToAll(AMETestData.A, 0, dut)
//       AME.apply.writeTestDataToAll(AMETestData.B, 1, dut)
//       AME.apply.writeTestDataToAll(AMETestData.Ctmp, 4, dut)
//       AME.apply.writeTestDataToAll(AMETestData.Ctmp, 5, dut)
//       AME.apply.writeTestDataToAll(AMETestData.Ctmp, 6, dut)
//       AME.apply.writeTestDataToAll(AMETestData.Ctmp, 7, dut)

//       dut.clock.step(1000) //随便跑几个cycle



//       // ins1
//       var cycleCountMMAU = 0
//       var cycleCountReady = 0

//       AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 4, true.B, true.B)  //启动AME，配置矩阵形状，确定操作数矩阵标号（ABC标号范围均是0～7)

//       while(!dut.io.ShakeHands_io.ready.peek().litToBoolean){ //等到ready
//         dut.clock.step(1)
//         cycleCountReady += 1
//       }

//       dut.clock.step(1) //ready后需要主动前进一个时钟周期

// AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 5, true.B, true.B)  //

//       while(!dut.io.sigDone.peek().litToBoolean){ //等到执行完毕
//         dut.clock.step(1)
//         cycleCountMMAU += 1

// AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 5, true.B, true.B)  //
//       }


//       println(s"cycleCountReady = $cycleCountReady , cycleCountMMAU = $cycleCountMMAU")

// // println(s"Done = ${dut.io.sigDone.peek().litToBoolean}")

// // println(s"ready = ${dut.io.ShakeHands_io.ready.peek().litToBoolean}")


//       // ins2

//       cycleCountReady = 0
//       cycleCountMMAU = 0  //计数清零


//       AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 5, true.B, true.B)  //下一条指令，换一个C
// // println(s"Done = ${dut.io.sigDone.peek().litToBoolean}")

//       while(!dut.io.ShakeHands_io.ready.peek().litToBoolean){ //等到ready
//         dut.clock.step(1)
//         cycleCountReady += 1
        
// // println(s"ready = ${dut.io.ShakeHands_io.ready.peek().litToBoolean}")
// // println(s"Done = ${dut.io.sigDone.peek().litToBoolean}")
//       }

// // println(s"Done = ${dut.io.sigDone.peek().litToBoolean}")

//       dut.clock.step(1) //ready后需要主动前进一个时钟周期
// // println(s"ready = ${dut.io.ShakeHands_io.ready.peek().litToBoolean}")
// // println(s"Done = ${dut.io.sigDone.peek().litToBoolean}")

//       while(!dut.io.sigDone.peek().litToBoolean){ //等到执行完毕
//         dut.clock.step(1)
//         cycleCountMMAU += 1

// AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 4, true.B, true.B)
// // println(s"ready = ${dut.io.ShakeHands_io.ready.peek().litToBoolean}")
//       }

// // println(s"ready = ${dut.io.ShakeHands_io.ready.peek().litToBoolean}")
//       println(s"cycleCountReady = $cycleCountReady , cycleCountMMAU = $cycleCountMMAU")


//       // ins3

//       cycleCountReady = 0
//       cycleCountMMAU = 0  //计数清零


//       AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 6, true.B, true.B)  //下一条指令，换一个C
// // println(s"ready = ${dut.io.ShakeHands_io.ready.peek().litToBoolean}")
//       while(!dut.io.ShakeHands_io.ready.peek().litToBoolean){ //等到ready
//         dut.clock.step(1)
//         cycleCountReady += 1
//       }

//       dut.clock.step(1) //ready后需要主动前进一个时钟周期

//       while(!dut.io.sigDone.peek().litToBoolean){ //等到执行完毕
//         dut.clock.step(1)
//         cycleCountMMAU += 1
//       }

//       println(s"cycleCountReady = $cycleCountReady , cycleCountMMAU = $cycleCountMMAU")



//       // ins4

//       cycleCountReady = 0
//       cycleCountMMAU = 0  //计数清零


//       AME.apply.AMEStart(dut, 32, 32, 64, 0, 1, 7, true.B, true.B)  //下一条指令，换一个C

//       while(!dut.io.ShakeHands_io.ready.peek().litToBoolean){ //等到ready
//         dut.clock.step(1)
//         cycleCountReady += 1
//       }

//       dut.clock.step(1) //ready后需要主动前进一个时钟周期

//       while(!dut.io.sigDone.peek().litToBoolean){ //等到执行完毕
//         dut.clock.step(1)
//         cycleCountMMAU += 1
//       }

//       println(s"cycleCountReady = $cycleCountReady , cycleCountMMAU = $cycleCountMMAU")

      

//       AME.apply.AMEStop(dut)  //停止，无效指令，valid = false

//       AME.apply.readTestDataFromAll(AMETestData.C, 4, dut) //验证结果是否正确
//       AME.apply.readTestDataFromAll(AMETestData.C, 5, dut) //验证结果是否正确
//       // AME.apply.readTestDataFromAll(AMETestData.C, 6, dut) //验证结果是否正确
//       // AME.apply.readTestDataFromAll(AMETestData.C, 7, dut) //验证结果是否正确


 


//     }
//   }
// }








// //无Expander版本
// class AMETest extends AnyFreeSpec with Matchers {

//   "AME should PASS" in {
//     simulate(new AME) { dut =>
    
      
//       /*  提前手动写入A、B、C*/
//       AME.apply.writeTestDataToAll(AMETestData.A, 0, dut)
//       AME.apply.writeTestDataToAll(AMETestData.B, 1, dut)
//       AME.apply.writeTestDataToAll(AMETestData.Ctmp, 4, dut)

//       dut.clock.step(1000) //随便跑几个cycle

//       AME.apply.AMEStart(dut, 10, 10, 20, 0, 1, 4)  //启动AME，配置矩阵形状，确定操作数矩阵标号（ABC标号范围均是0～7)

//       var cycleCountMMAU = 0

//       while(!dut.io.sigDone.peek().litToBoolean){
//         dut.clock.step(1)
//         cycleCountMMAU += 1
//       }

      

//       AME.apply.readTestDataFromAll(AMETestData.C, 4, dut) //验证结果是否正确


//       println(s"Total cycles: $cycleCountMMAU")


//       /***********************************************************************************/

//       // /*  跑第二条指令，看是否存在“脏数据”问题  */
//       // println(s"run again")
      
//       // AME.apply.writeTestDataToAll(AMETestData.Ctmp, 4, dut)

//       // AME.apply.AMEStart(dut)//启动AME

//       // while(!dut.io.sigDone.peek().litToBoolean){
//       //   dut.clock.step(1)
//       //   // println(s"run in AME")
//       // }

//       // AME.apply.readTestDataFromAll(AMETestData.C, 4, dut) //验证结果是否正确

//     }
//   }
// }


