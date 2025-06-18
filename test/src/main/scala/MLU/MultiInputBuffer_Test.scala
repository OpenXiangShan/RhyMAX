package MLU

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import chisel3.experimental.BundleLiterals._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers




class MultiInputBuffer_Test extends AnyFreeSpec with Matchers {
  "MultiInputBuffer should PASS" in {
    simulate(new MultiInputBuffer(numWay = 8, width = 136, depth = 32)) { dut =>

    dut.io.out.ready.poke(true.B)

      // 写入 32 轮，每轮 8 路
      for (round <- 0 until 32) {
        for (i <- 0 until 8) {
          val data = (round << 4) + i
          dut.io.in(i).poke(data.U)

          if(i % 2 == 0){
            dut.io.in_valid(i).poke(true.B)
          }else{
            dut.io.in_valid(i).poke(false.B)
          }
          
        }
        dut.clock.step(1)
      }

      // 写入完毕后，关闭输入 valid
      for (i <- 0 until 8) {
        dut.io.in_valid(i).poke(false.B)
      }

      // 启动输出，读取直到读空
      while (dut.io.out.valid.peek().litToBoolean) {
        // println(s"[OUT] valid=${dut.io.out.valid.peek()}, bits=0x${dut.io.out.bits.peek().litValue}%11d")
        println(s"[OUT] valid=${dut.io.out.valid.peek()}, bits=0x${dut.io.out.bits.peek().litValue.toString(16)}")

        dut.io.out.ready.poke(true.B)
        dut.clock.step(1)
      }

    }
  }
}



// class MultiInputBuffer_Test extends AnyFreeSpec with Matchers {
//   "MultiInputBuffer should PASS" in {
//     simulate(new MultiInputBuffer(numWay = 8, width = 136 , depth = 32)) { dut =>

//       for(round <- 0 until 32){

//         for (i <- 0 until 8) {
//           val data = round.U << 4 + i.U
//           dut.io.in(i).poke(data)
//           dut.io.in_valid(i).poke(true.B)
//         }

//         dut.clock.step(1)

//       }

//       for (i <- 0 until 8) { dut.io.in_valid(i).poke(false.B) }
      
//       while(dut.io.out.valid.litToBoolean()){

//         println(p"")
//       }
      

//     }
//   }
// }



// 0617
// class MultiInputBuffer_Test extends AnyFreeSpec with Matchers {
//   "MultiInputBuffer should PASS" in {
//     simulate(new MultiInputBuffer(numWay = 8, width = 64 + 5 + 3 , depth = 3)) { dut =>
//       // 准备好 8 路输入数据
//       for (i <- 0 until 8) {
//         dut.io.in(i).poke((i + 1).U)
//         dut.io.in_valid(i).poke(true.B)
//       }
      

//       // 先等一拍，让它批量吞入
//       dut.clock.step(1)


//       // 第二次 8 路输入数据
//       for (i <- 0 until 8) {
//         dut.io.in(i).poke((i*2 + 1).U)
//         dut.io.in_valid(i).poke(true.B)
//       }
      

//       // 先等一拍，让它批量吞入
//       dut.clock.step(1)

//       // 吞入后关闭 valid
//       for (i <- 0 until 8) {
//         dut.io.in_valid(i).poke(false.B)
//       }

//       println("Cycle | OutputValid | OutputBits")
//       println("-------------------------------")

//       // 吞掉的 8 条数据按顺序读出
//       for (cycle <- 0 until 24) {
//         dut.io.out.ready.poke(true.B) // 外部一直准备好

        

//         val outValid = dut.io.out.valid.peek().litToBoolean
//         val outBits  = dut.io.out.bits.peek().litValue

//         println(f"$cycle%5d | $outValid%11s | $outBits%11d")


//         dut.clock.step(1)
//       }
//     }
//   }
// }



// class MultiInputBuffer_Test extends AnyFreeSpec with Matchers {
//   "MultiInputBuffer should PASS" in {
//     simulate(new MultiInputBuffer(numWay = 8, width = 8 , depth = 1)) { dut =>
//       // 准备好 8 路输入数据
//       for (i <- 0 until 8) {
//         dut.io.in(i).poke((i + 1).U)
//       }
//       dut.io.in_valid.poke(true.B)

//       // 先等一拍，让它批量吞入
//       dut.clock.step(1)


//       // 第二次 8 路输入数据
//       for (i <- 0 until 8) {
//         dut.io.in(i).poke((i*2 + 1).U)
//       }
//       dut.io.in_valid.poke(true.B)

//       // 先等一拍，让它批量吞入
//       dut.clock.step(1)

//       // 吞入后关闭 valid
//       dut.io.in_valid.poke(false.B)

//       println("Cycle | OutputValid | OutputBits")
//       println("-------------------------------")

//       // 吞掉的 8 条数据按顺序读出
//       for (cycle <- 0 until 24) {
//         dut.io.out.ready.poke(true.B) // 外部一直准备好

        

//         val outValid = dut.io.out.valid.peek().litToBoolean
//         val outBits  = dut.io.out.bits.peek().litValue

//         println(f"$cycle%5d | $outValid%11s | $outBits%11d")


//         dut.clock.step(1)
//       }
//     }
//   }
// }
