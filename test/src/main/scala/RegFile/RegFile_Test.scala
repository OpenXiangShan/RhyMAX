package RegFile

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import scala.util.Random

import common._



// class RegFileTest extends AnyFreeSpec with Matchers {

//   // 测试参数
//   val testParams = new {
//     val gen: Data = UInt(32.W)
//     val set = 8
//     val way = 4
//   }

//   "RegFile should PASS" in {
//     simulate(new RegFile) { dut =>

//       val testData = Seq(
//         Seq("h11111111".U(32.W), "h22222222".U(32.W), "h33333333".U(32.W), "h44444444".U(32.W)),
//         Seq("h55555555".U(32.W), "h66666666".U(32.W), "h77777777".U(32.W), "h88888888".U(32.W)),
//         Seq("h99999999".U(32.W), "hAAAAAAAA".U(32.W), "hBBBBBBBB".U(32.W), "hCCCCCCCC".U(32.W)),
//         Seq("hDDDDDDDD".U(32.W), "hEEEEEEEE".U(32.W), "hFFFFFFFF".U(32.W), "h00000000".U(32.W)),
//         Seq("h12345678".U(32.W), "h9ABCDEF0".U(32.W), "h0FEDCBA9".U(32.W), "h87654321".U(32.W)),
//         Seq("hCAFEBABE".U(32.W), "hDEADBEEF".U(32.W), "hFACECAFE".U(32.W), "hBAADF00D".U(32.W)),
//         Seq("h10203040".U(32.W), "h50607080".U(32.W), "h90A0B0C0".U(32.W), "hD0E0F000".U(32.W)),
//         Seq("hABCDEF01".U(32.W), "h23456789".U(32.W), "h3456789A".U(32.W), "h456789AB".U(32.W))
//       )

//       // 写入操作
//       for (setIdx <- testData.indices) {
//         dut.io.w.req.valid.poke(true.B)
//         dut.io.w.req.bits.setIdx.poke(setIdx.U)

//         // 写入所有 way 的数据
//         for (wayIdx <- 0 until testParams.way) {
//           dut.io.w.req.bits.data(wayIdx).poke(testData(setIdx)(wayIdx))
//         }

//         // waymask 全部打开
//         dut.io.w.req.bits.waymask.foreach(_.poke("b1111".U))

//         // 等待 ready
//         while (!dut.io.w.req.ready.peek().litToBoolean) {
//           dut.clock.step()
//         }

//         dut.clock.step()
//       }

//       // 保证写完
//       dut.io.w.req.valid.poke(false.B)
//       dut.clock.step(5)

//       // 读取操作
//       for (setIdx <- testData.indices) {
//         dut.io.r.req.valid.poke(true.B)
//         dut.io.r.req.bits.setIdx.poke(setIdx.U)

//         // 等待 ready
//         while (!dut.io.r.req.ready.peek().litToBoolean) {
//           dut.clock.step()
//         }

//         dut.clock.step()

//         // 检查所有 way 的数据
//         for (wayIdx <- 0 until testParams.way) {
//           dut.io.r.resp.data(wayIdx).asUInt.expect(testData(setIdx)(wayIdx))
//           val readValue = dut.io.r.resp.data(wayIdx).asUInt.peek()
//           println(f"readValue = 0x${readValue.litValue}%X")
//           // assert(readValue.litValue == testData(setIdx)(wayIdx).litValue)
//         }
//       }
//     }
//   }
// }



class TrTest extends AnyFreeSpec with Matchers {

  // val testParams = new {
  //   val gen: Data = UInt(8.W)
  //   val set: Int = 16
  //   val way: Int = 1
  //   val dataSplit: Int = 1
  //   val setSplit: Int = 1
  //   val waySplit: Int = 1
  //   val useBitmask: Boolean = false
  //   val numBank: Int = 4
  // }

  "Tr should PASS" in {
    simulate(new Tr) { dut =>

      val testDataPerBank = Seq(
        // Bank 0
        Seq(
          1.U(8.W), 2.U(8.W), 3.U(8.W), 4.U(8.W),
          5.U(8.W), 6.U(8.W), 7.U(8.W), 8.U(8.W),
          9.U(8.W), 10.U(8.W), 11.U(8.W), 12.U(8.W),
          13.U(8.W), 14.U(8.W), 15.U(8.W), 16.U(8.W)
        ),
        // Bank 1
        Seq(
          17.U(8.W), 18.U(8.W), 19.U(8.W), 20.U(8.W),
          21.U(8.W), 22.U(8.W), 23.U(8.W), 24.U(8.W),
          25.U(8.W), 26.U(8.W), 27.U(8.W), 28.U(8.W),
          29.U(8.W), 30.U(8.W), 31.U(8.W), 32.U(8.W)
        ),
        // Bank 2
        Seq(
          33.U(8.W), 34.U(8.W), 35.U(8.W), 36.U(8.W),
          37.U(8.W), 38.U(8.W), 39.U(8.W), 40.U(8.W),
          41.U(8.W), 42.U(8.W), 43.U(8.W), 44.U(8.W),
          45.U(8.W), 46.U(8.W), 47.U(8.W), 48.U(8.W)
        ),
        // Bank 3
        Seq(
          49.U(8.W), 50.U(8.W), 51.U(8.W), 52.U(8.W),
          53.U(8.W), 54.U(8.W), 55.U(8.W), 56.U(8.W),
          57.U(8.W), 58.U(8.W), 59.U(8.W), 60.U(8.W),
          61.U(8.W), 62.U(8.W), 63.U(8.W), 64.U(8.W)
        )
      )


      // 写入阶段
      for ((bankData, bankIdx) <- testDataPerBank.zipWithIndex) {
        for ((data, setIdx) <- bankData.zipWithIndex) {

          // 写请求
          dut.io.w(bankIdx).req.valid.poke(true.B)
          dut.io.w(bankIdx).req.bits.setIdx.poke(setIdx.U)
          dut.io.w(bankIdx).req.bits.data.head.poke(data) //等价于dut.io.w(bankIdx).req.bits.data(0).poke(data)
          // dut.io.w(bankIdx).req.bits.waymask.foreach(_.poke("b1".U))

          // 等待 ready
          while (!dut.io.w(bankIdx).req.ready.peek().litToBoolean) {
            dut.clock.step()
          }

          dut.clock.step()
        }

        // 停止写请求
        dut.io.w(bankIdx).req.valid.poke(false.B)
      }

      dut.clock.step(5)

      // 读取阶段
      for ((bankData, bankIdx) <- testDataPerBank.zipWithIndex) {
        for ((expectedData, setIdx) <- bankData.zipWithIndex) {

          // 读请求
          dut.io.r(bankIdx).req.valid.poke(true.B)
          dut.io.r(bankIdx).req.bits.setIdx.poke(setIdx.U)

          // 等待 ready
          while (!dut.io.r(bankIdx).req.ready.peek().litToBoolean) {
            dut.clock.step()
          }

          dut.clock.step()

          val readValue = dut.io.r(bankIdx).resp.data.head.asUInt.peek()
          println(s"Bank $bankIdx, setIdx $setIdx: readValue = ${readValue.litValue}")

          dut.io.r(bankIdx).resp.data.head.asUInt.expect(expectedData)
        }

        // 停止读请求
        dut.io.r(bankIdx).req.valid.poke(false.B)
      }

    }
  }
}
