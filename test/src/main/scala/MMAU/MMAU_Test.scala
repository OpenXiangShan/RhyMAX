// package MMAU

// import chisel3._
// import chisel3.experimental.BundleLiterals._
// import chisel3.simulator.EphemeralSimulator._
// import org.scalatest.freespec.AnyFreeSpec
// import org.scalatest.matchers.must.Matchers


// import common._




// class MMAUTestExpect extends AnyFreeSpec with Matchers {

//   "MMAU should PASS" in {
//     simulate(new MMAU) { dut =>


//       for (i <- 0 until 5) { // 先随便跑几个cycle
//         dut.clock.step(1)
//       }

//       dut.io.sigStart.poke(true.B)  //启动信号
//       dut.clock.step(1)
//       dut.io.sigStart.poke(false.B)

//       for (i <- 0 until Consts.numM) {
//         for (j <- 0 until Consts.numN) {
//           for (p <- 0 until Consts.numK) {
//             // println(s"mState = $i , nState = $j , kState = $p")
//             apply.checkOutputLatency(dut)
//             // println("\n")
//           }
//         }
//       }


//       for (p <- 0 until Consts.numK) {
//             // println(s"mState = $i , nState = $j , kState = $p")
//             apply.checkOutputLatency(dut)
//             // println(s"sigDone = ${dut.io.sigDone.peek().litToBoolean}") // 打印sigDone
//             // println("\n")
//       }

//       for (p <- 0 until Consts.numK) {
//             // println(s"mState = $i , nState = $j , kState = $p")
//             apply.checkOutputLatency(dut)
//             // println(s"sigDone = ${dut.io.sigDone.peek().litToBoolean}") // 打印sigDone
//             // println("\n")
//       }
      
//     }
//   }
// }




// // class MMAUTestExpect extends AnyFreeSpec with Matchers {

// //   "MMAU should PASS" in {
// //     simulate(new MMAU) { dut =>


// //       for (i <- 0 until 5) { // 先随便跑几个cycle
// //         dut.clock.step(1)
// //       }

// //       dut.io.sigStart.poke(true.B)  //启动信号
// //       dut.clock.step(1)
// //       dut.io.sigStart.poke(false.B)

// //       for (i <- 0 until Consts.numM) {
// //         for (j <- 0 until Consts.numN) {
// //           for (p <- 0 until Consts.numK) {
// //             // println(s"mState = $i , nState = $j , kState = $p")
// //             apply.checkOutput(dut)
// //             // println("\n")
// //           }
// //         }
// //       }


// //       for (p <- 0 until Consts.numK) {
// //             // println(s"mState = $i , nState = $j , kState = $p")
// //             apply.checkOutput(dut)
// //             // println(s"sigDone = ${dut.io.sigDone.peek().litToBoolean}") // 打印sigDone
// //             // println("\n")
// //       }

// //       for (p <- 0 until Consts.numK) {
// //             // println(s"mState = $i , nState = $j , kState = $p")
// //             apply.checkOutput(dut)
// //             // println(s"sigDone = ${dut.io.sigDone.peek().litToBoolean}") // 打印sigDone
// //             // println("\n")
// //       }
      
// //     }
// //   }
// // }






















