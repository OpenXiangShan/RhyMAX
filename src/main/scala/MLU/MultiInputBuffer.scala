package MLU

import chisel3._
import chisel3.util._


class MultiInputBuffer(val numWay: Int, val width: Int, val depth: Int) extends Module {
  require(depth > 0, "Depth must be positive")
  require(numWay > 0, "numWay must be positive")
  
  val io = IO(new Bundle {
    val in = Input(Vec(numWay, UInt(width.W)))
    val in_valid = Input(Bool())
    val out = Decoupled(UInt(width.W))
  })

  // 使用寄存器数组作为存储
  val buffer = Reg(Vec(depth, Vec(numWay, UInt(width.W))))
  val valids = RegInit(VecInit(Seq.fill(depth)(false.B)))
  
  // 写指针
  val wptr = RegInit(0.U(log2Ceil(depth).W))
  
  // 写入逻辑
  when(io.in_valid) {
    buffer(wptr) := io.in
    valids(wptr) := true.B
    wptr := Mux(wptr === (depth-1).U, 0.U, wptr + 1.U)
  }
  
  // 读指针和输出计数器
  val rptr = RegInit(0.U(log2Ceil(depth).W))
  val outCnt = RegInit(0.U(log2Ceil(numWay).W))
  
  // 输出逻辑
  io.out.valid := valids(rptr) && (outCnt < numWay.U)
  io.out.bits := buffer(rptr)(outCnt)
  
  // 更新逻辑
  when(io.out.fire) {
    outCnt := outCnt + 1.U
    when(outCnt === (numWay-1).U) {
      valids(rptr) := false.B
      outCnt := 0.U
      rptr := Mux(rptr === (depth-1).U, 0.U, rptr + 1.U)
    }
  }
}


// class MultiInputBuffer(val numWay: Int, val width: Int, val depth: Int) extends Module {
//   val io = IO(new Bundle {
//     val in = Input(Vec(numWay, UInt(width.W)))
//     val in_valid = Input(Bool())
//     val out = Decoupled(UInt(width.W))
//   })

//   // 数据存储寄存器
//   val dataReg = Reg(Vec(numWay, UInt(width.W)))
//   val dataValid = RegInit(false.B)
  
//   // 输出计数器
//   val counter = RegInit(0.U(log2Ceil(numWay).W))
  
//   // 输入逻辑 - 一次性接收所有数据
//   when(io.in_valid) {
//     dataReg := io.in
//     dataValid := true.B
//     counter := 0.U
//   }
  
//   // 输出逻辑 - 逐个输出
//   io.out.valid := dataValid && (counter < numWay.U)
//   io.out.bits := dataReg(counter)
  
//   // 更新计数器
//   when(io.out.fire) {
//     counter := counter + 1.U
//     when(counter === (numWay - 1).U) {
//       dataValid := false.B
//     }
//   }
// }
