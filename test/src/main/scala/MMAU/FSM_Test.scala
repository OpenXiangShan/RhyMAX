package MMAU

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers


import common._






// class FSMTestExpect extends AnyFreeSpec with Matchers {
  
//   "FSM should PASS" in {
//     simulate(new FSM) { dut =>

//       val numM = Consts.tileM / Consts.m
//       val numN = Consts.tileN / Consts.n
//       val numK = Consts.tileK / Consts.k

//       dut.clock.step(5) // 预跑几个cycle
      
//       dut.io.sigStart.poke(true.B)
//       dut.clock.step(1)
//       dut.io.sigStart.poke(false.B)

//       for (i <- 0 until numM) {
//         println("**********************************")
//         println(s"*         mState = $i          *")
//         println("**********************************")



//         for (j <- 0 until numN) {
//           println("**********************************")
//           println(s"*         nState = $j          *")
//           println("**********************************")

//           for (p <- 0 until numK) {
//             println("\n\n\n") // 3行空行分隔 kState
//             println(s"======== kState = $p ========")

    
//             apply.printFSM(dut)  //打印所有相关信号

//             dut.clock.step(1)
//           }
//         }
//       }

//       /*    再来一轮    */

//       for (i <- 0 until numM) {
//         println("**********************************")
//         println(s"*         mState = $i          *")
//         println("**********************************")



//         for (j <- 0 until numN) {
//           println("**********************************")
//           println(s"*         nState = $j          *")
//           println("**********************************")

//           for (p <- 0 until numK) {
//             println("\n\n\n") // 3行空行分隔 kState
//             println(s"======== kState = $p ========")

    
//             apply.printFSM(dut)  //打印所有相关信号

//             dut.clock.step(1)
//           }
//         }
//       }

      
//     }
//   }
// }
















