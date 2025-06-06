package IssueQueen

import chisel3._
import chisel3.util._

import common._
import MMAU._
import Expander._






class SimpleHandshakeFIFO(val depth: Int, val width: Int) extends Module {
  val io = IO(new Bundle {
    val enq_valid = Input(Bool())
    val enq_ready = Output(Bool())
    val enq_bits  = Input(UInt(width.W))

    val deq_valid = Output(Bool())
    val deq_ready = Input(Bool())
    val deq_bits  = Output(UInt(width.W))
  })

  val fifo = Module(new Queue(UInt(width.W), depth))

  fifo.io.enq.valid := io.enq_valid
  fifo.io.enq.bits  := io.enq_bits
  io.enq_ready      := fifo.io.enq.ready

  io.deq_valid      := fifo.io.deq.valid
  io.deq_bits       := fifo.io.deq.bits
  fifo.io.deq.ready := io.deq_ready
}

