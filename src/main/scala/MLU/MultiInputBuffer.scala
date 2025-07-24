package MLU

import chisel3._
import chisel3.util._



//每路输入有独立的invalid
class MultiInputBuffer(val numWay: Int, val width: Int, val depth: Int) extends Module {
  require(depth > 0, "Depth must be positive")
  require(numWay > 0, "numWay must be positive")

  val io = IO(new Bundle {
    val in = Input(Vec(numWay, UInt(width.W)))
    val in_valid = Input(Vec(numWay, Bool()))
    val out = Decoupled(UInt(width.W))
  })

  val buffer = Reg(Vec(depth, Vec(numWay, UInt(width.W))))
  val valids = RegInit(VecInit(Seq.fill(depth)(VecInit(Seq.fill(numWay)(false.B)))))

  val wptr = RegInit(0.U(log2Ceil(depth).W))
  val rptr = RegInit(0.U(log2Ceil(depth).W))
  val outCnt = RegInit(0.U(log2Ceil(numWay).W))

  // 是否写爆（buffer满）：写指针+1 == 读指针，且该行有效（未完全清空）
  val nextWptr = Mux(wptr === (depth - 1).U, 0.U, wptr + 1.U)
  val bufferFull = (nextWptr === rptr) && valids(rptr).reduce(_ || _)

  val doWrite = io.in_valid.reduce(_ || _) && !bufferFull

  when(doWrite) {
    buffer(wptr) := io.in
    valids(wptr) := io.in_valid
    wptr := nextWptr
  }

  val currentLine = buffer(rptr)
  val currentValid = valids(rptr)

  val activeValid = VecInit((0 until numWay).map { i =>
    currentValid(i) && (i.U >= outCnt)
  })

  val nextValidIdx = PriorityEncoder(activeValid)
  val found = activeValid.asUInt.orR

  io.out.valid := found
  io.out.bits := currentLine(nextValidIdx)

  when(io.out.fire) {
    valids(rptr)(nextValidIdx) := false.B

    val remaining = valids(rptr).zipWithIndex.map {
      case (v, i) => v && (i.U > nextValidIdx)
    }.reduce(_ || _)

    when(remaining) {
      outCnt := nextValidIdx + 1.U
    }.otherwise {
      outCnt := 0.U
      rptr := Mux(rptr === (depth - 1).U, 0.U, rptr + 1.U)
    }
  }
}





//旧 多路输入共用invalid
// class MultiInputBuffer(val numWay: Int, val width: Int, val depth: Int) extends Module {
//   require(depth > 0, "Depth must be positive")
//   require(numWay > 0, "numWay must be positive")
  
//   val io = IO(new Bundle {
//     val in = Input(Vec(numWay, UInt(width.W)))
//     val in_valid = Input(Bool())
//     val out = Decoupled(UInt(width.W))
//   })

//   // 使用寄存器数组作为存储
//   val buffer = Reg(Vec(depth, Vec(numWay, UInt(width.W))))
//   val valids = RegInit(VecInit(Seq.fill(depth)(false.B)))
  
//   // 写指针
//   val wptr = RegInit(0.U(log2Ceil(depth).W))
  
//   // 写入逻辑
//   when(io.in_valid) {
//     buffer(wptr) := io.in
//     valids(wptr) := true.B
//     wptr := Mux(wptr === (depth-1).U, 0.U, wptr + 1.U)
//   }
  
//   // 读指针和输出计数器
//   val rptr = RegInit(0.U(log2Ceil(depth).W))
//   val outCnt = RegInit(0.U(log2Ceil(numWay).W))
  
//   // 输出逻辑
//   io.out.valid := valids(rptr) && (outCnt < numWay.U)
//   io.out.bits := buffer(rptr)(outCnt)
  
//   // 更新逻辑
//   when(io.out.fire) {
//     outCnt := outCnt + 1.U
//     when(outCnt === (numWay-1).U) {
//       valids(rptr) := false.B
//       outCnt := 0.U
//       rptr := Mux(rptr === (depth-1).U, 0.U, rptr + 1.U)
//     }
//   }
// }

