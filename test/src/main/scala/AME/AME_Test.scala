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
import L2._




//AME完整测试，仅测load B指令
class AMETest_loadB extends AnyFreeSpec with Matchers {

  "AME_loadB should PASS" in {
    simulate(new AME) { dut =>

      L2Sim.loadDataFrom(L2TestData.L2_Data, 0)//初始化L2

      // (AME , mtilem , mtilen , mtilek , ms1 , ms2 , md , rs1 , rs2 , valid , is_mmacc , is_mlbe8)
      // AME.apply.AMEStart(dut, 64, 64, 256, 0, 0, 1, 0, 256, true.B, false.B, true.B) //全尺寸
      // AME.apply.AMEStart(dut, 40, 40, 100, 0, 0, 1, 0, 256, true.B, false.B, true.B) //非全尺寸
      AME.apply.AMEStart(dut, 35, 35, 100, 0, 0, 1, 0, 256, true.B, false.B, true.B) //非全尺寸
 
      var cycleCountMLU = 0
      var cycleCountReady = 0

      while(!dut.io.Uop_io.ShakeHands_io.ready.peek().litToBoolean){ //等到ready
        dut.clock.step(1)
        cycleCountReady += 1
      }

      dut.clock.step(1) //ready后需要主动前进一个时钟周期

println(s"ins 1 excuting")

      // while(!dut.io.sigDone.peek().litToBoolean && cycleCountMLU < 500){ //等到执行完毕
      // while(cycleCountMLU < 100){ //等到执行完毕
      while(!dut.io.sigDone.peek().litToBoolean){ //等到执行完毕
        for(i <- 0 until 8){//8条cacheline
          if(dut.io.MLU_L2_io.Cacheline_Read_io(i).valid.peek().litToBoolean){//对L2读请求有意义
            val addr_req = dut.io.MLU_L2_io.Cacheline_Read_io(i).addr.peek().litValue.toInt
            val id_req = dut.io.MLU_L2_io.Cacheline_Read_io(i).id.peek().litValue.toInt

println(s"addr_req = ${addr_req} , id_req = ${id_req}")

            val (readData, id) = L2Sim.readLine(addr_req, id_req)
println(f"readData = 0x${readData.litValue}%0128X, id = $id")
            dut.io.MLU_L2_io.Cacheline_ReadBack_io(i).data.poke(readData)
            dut.io.MLU_L2_io.Cacheline_ReadBack_io(i).id.poke(id)
            dut.io.MLU_L2_io.Cacheline_ReadBack_io(i).valid.poke(true.B)
          }else{
            dut.io.MLU_L2_io.Cacheline_ReadBack_io(i).valid.poke(false.B)
          }
        }
        
        dut.clock.step(1)
        cycleCountMLU += 1
      }

      for(i <- 0 until 8){
        dut.io.MLU_L2_io.Cacheline_ReadBack_io(i).valid.poke(false.B)
      }


      // println(s"cycleCountReady = $cycleCountReady , cycleCountMLU = $cycleCountMLU")
      AME.apply.readTestDataFromAll(RFTestData.A, 1, dut) //验证结果是否正确


    }
  }
}







//AME完整测试，仅测mma指令
class AMETest_mma extends AnyFreeSpec with Matchers {

  "AME_mma should PASS" in {
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





