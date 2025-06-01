// package L2

// import chiseltest._
// import chisel3._
// import org.scalatest.flatspec.AnyFlatSpec


// /*      使用函数模拟一个L2,用于测试     */
// object L2Sim {
//   val cacheSize: Int = 2048 * 2048
//   val lineSize: Int = 64
//   val L2_Cache: Array[Byte] = new Array[Byte](cacheSize)

//   def clearAll(): Unit = {
//     java.util.Arrays.fill(L2_Cache, 0.toByte)
//   }

//   // 从外部数组加载数据，用于初始化L2
//   def loadDataFrom(testData: Array[Byte], offset: Int): Unit = {
//     require(offset >= 0 && offset + testData.length <= cacheSize,
//       s"数据写入越界: offset=$offset, length=${testData.length}")
//     Array.copy(testData, 0, L2_Cache, offset, testData.length)
//   }


//   // //小端模式
//   // def readLine(addr: Int, id: Int): (UInt, Int) = {
//   //     require(addr >= 0 && addr + lineSize <= L2_Cache.length, s"地址越界: $addr")
//   //     val bytes = L2_Cache.slice(addr, addr + lineSize)
  
//   //     // foldRight 保证低地址 byte 映射到低位，符合小端拼接逻辑
//   //     val bigInt = bytes.foldRight(BigInt(0)) {
//   //     case (byte, acc) => (acc << 8) | (byte & 0xFF)
//   //     }
  
//   //     (bigInt.U(512.W), id)
//   // }


//   //大端模式
//   def readLine(addr: Int, id: Int): (UInt, Int) = {
//   require(addr >= 0 && addr + lineSize <= L2_Cache.length, s"地址越界: $addr")
//   val bytes = L2_Cache.slice(addr, addr + lineSize)

//   // foldLeft 实现大端拼接：低地址 byte 映射到高位
//   val bigInt = bytes.foldLeft(BigInt(0)) {
//     case (acc, byte) => (acc << 8) | (byte & 0xFF)
//   }

//   (bigInt.U(512.W), id)
// }


//   // 模拟8条cacheline，可以独立调用
//   def readPort0(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
//   def readPort1(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
//   def readPort2(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
//   def readPort3(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
//   def readPort4(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
//   def readPort5(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
//   def readPort6(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
//   def readPort7(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
// }



package L2

import chiseltest._
import chisel3._
import org.scalatest.flatspec.AnyFlatSpec

/*      使用函数模拟一个L2,用于测试     */
object L2Sim {
  val cacheSize: Int = 2048 * 2048
  val lineSize: Int = 64
  val L2_Cache: Array[Byte] = new Array[Byte](cacheSize)

  def clearAll(): Unit = {
    java.util.Arrays.fill(L2_Cache, 0.toByte)
  }

  // 从外部 UInt 数组加载数据，用于初始化L2（兼容 Seq("hxx".U, ...) 格式）
  def loadDataFrom(testData: Seq[UInt], offset: Int): Unit = {
    require(offset >= 0 && offset + testData.length <= cacheSize,
      s"数据写入越界: offset=$offset, length=${testData.length}")
    val byteData: Array[Byte] = testData.map { u =>
      require(u.getWidth <= 8, s"超出字节范围: $u")
      u.litValue.toByte
    }.toArray
    Array.copy(byteData, 0, L2_Cache, offset, byteData.length)
  }

  // 大端模式读取：低地址 byte 映射到高位
  def readLine(addr: Int, id: Int): (UInt, Int) = {
    require(addr >= 0 && addr + lineSize <= L2_Cache.length, s"地址越界: $addr")
    val bytes = L2_Cache.slice(addr, addr + lineSize)
    val bigInt = bytes.foldLeft(BigInt(0)) {
      case (acc, byte) => (acc << 8) | (byte & 0xFF)
    }
    (bigInt.U(512.W), id)
  }

  // 模拟8条cacheline，可以独立调用
  def readPort0(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
  def readPort1(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
  def readPort2(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
  def readPort3(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
  def readPort4(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
  def readPort5(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
  def readPort6(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
  def readPort7(addr: Int, id: Int): (UInt, Int) = readLine(addr, id)
}

