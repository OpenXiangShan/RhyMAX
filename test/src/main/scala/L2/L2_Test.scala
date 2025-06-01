// package L2

// import chisel3._
// import chisel3.experimental.BundleLiterals._
// import chisel3.simulator.EphemeralSimulator._
// import org.scalatest.freespec.AnyFreeSpec
// import org.scalatest.matchers.must.Matchers
// import scala.util.Random



// import utility.sram._
// import common._
// import RegFile._
// import MMAU._
// import AME._



// //简单测一下模拟的L2读功能
// class L2SimTest extends AnyFreeSpec with Matchers {

//   "L2Sim should PASS" in {
//     simulate(new AME) { dut =>
//       // 构造你想测试的数据
//       val testData: Array[Byte] = Array(
//         0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
//         0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
//         0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
//         0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20,
//         0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28,
//         0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30,
//         0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38,
//         0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F, 0x40
//       ).map(_.toByte)  // 明确转换为 Byte 类型

//       // 清除旧数据
//       L2Sim.clearAll()

//       // 将 testData 写入 L2 缓存中的 offset 位置
//       val offset = 0
//       L2Sim.loadDataFrom(testData, offset)
      
//       for(i <- 0 until 10){
//         val (readData, id) = L2Sim.readPort0(i, i + 2)
//         println(f"readData = 0x${readData.litValue}%0128X, id = $id")
//       }
//     }
//   }
// }