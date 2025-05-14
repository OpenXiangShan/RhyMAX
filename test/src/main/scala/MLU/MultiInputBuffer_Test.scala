package MLU

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import chisel3.experimental.BundleLiterals._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MultiInputBuffer_Test extends AnyFreeSpec with Matchers {
  "MultiInputBuffer should PASS" in {
    simulate(new MultiInputBuffer(numWay = 8, width = 8 , depth = 1)) { dut =>
      // 准备好 8 路输入数据
      for (i <- 0 until 8) {
        dut.io.in(i).poke((i + 1).U)
      }
      dut.io.in_valid.poke(true.B)

      // 先等一拍，让它批量吞入
      dut.clock.step(1)


      // 第二次 8 路输入数据
      for (i <- 0 until 8) {
        dut.io.in(i).poke((i*2 + 1).U)
      }
      dut.io.in_valid.poke(true.B)

      // 先等一拍，让它批量吞入
      dut.clock.step(1)

      // 吞入后关闭 valid
      dut.io.in_valid.poke(false.B)

      println("Cycle | OutputValid | OutputBits")
      println("-------------------------------")

      // 吞掉的 8 条数据按顺序读出
      for (cycle <- 0 until 24) {
        dut.io.out.ready.poke(true.B) // 外部一直准备好

        

        val outValid = dut.io.out.valid.peek().litToBoolean
        val outBits  = dut.io.out.bits.peek().litValue

        println(f"$cycle%5d | $outValid%11s | $outBits%11d")


        dut.clock.step(1)
      }
    }
  }
}
