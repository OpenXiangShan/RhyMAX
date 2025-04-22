package ScoreBoard

import chisel3._
import chisel3.util._

import common._
import MMAU._
import Expander._


class ScoreBoard extends Module{
  val io = IO(new Bundle {
    val ScoreboardVisit_io = Flipped(new ScoreboardVisit_IO)
  })

  val regSB_RF = RegInit(0.U(8.W))
  val regSB_Unit = RegInit(0.U(4.W))

  //read
  io.ScoreboardVisit_io.read_RF := regSB_RF
  io.ScoreboardVisit_io.read_Unit := regSB_Unit

  //write
  // regSB_RF := (regSB_RF | io.ScoreboardVisit_io.writeMaskAlloc_RF) & ~io.ScoreboardVisit_io.writeMaskFree_RF      //先分配后释放，不要在同一个cycle对同一个资源“既分配又释放”
  regSB_RF := (regSB_RF  & ~io.ScoreboardVisit_io.writeMaskFree_RF) | io.ScoreboardVisit_io.writeMaskAlloc_RF //必须先释放后分配!!
  regSB_Unit := (regSB_Unit | io.ScoreboardVisit_io.writeMaskAlloc_Unit) & ~io.ScoreboardVisit_io.writeMaskFree_Unit


}



