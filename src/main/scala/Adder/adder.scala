package adder
import utility._
import utility.sram._
// import freechips.rocketchip.util.FromRational

import chisel3._

class Adder extends Module {
    val io = IO(new Bundle {
        val a = Input(UInt(8.W))
        val b = Input(UInt(8.W))
        val o = Output(UInt(8.W))
    })
    io.o := io.a + io.b
}


// class Adder extends Module {
//   val io = IO(new Bundle {
//     val a = Input(UInt(8.W))
//     val b = Input(UInt(8.W))
//     val o = Output(UInt(8.W))
   
//   })

//   val subFucku = Module(new Fucku())

//   subFucku.io.a := io.a
//   subFucku.io.b := io.b
//   io.o := subFucku.io.o


  
// }