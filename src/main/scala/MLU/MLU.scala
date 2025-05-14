package MLU

import chisel3._
import chisel3.util._

import common._
import Expander._
import RegFile._



class MLU extends Module{
    val io = IO(new Bundle {
        val FSM_MLU_io = Flipped(new FSM_MLU_IO)    //接FSM
        val MLU_L2_io = new MLU_L2_IO   //访问L2
        val RegFileAllWrite_io = Flipped(new RegFileAllWrite_IO)  //写RF
    })

    /*  read from L2 */
    io.FSM_MLU_io.Cacheline_Read_io <> io.MLU_L2_io.Cacheline_Read_io

    /*    写RF    */
    val wireSplit = Vec(8 , Vec(8 , Wire(UInt((64 + 5).W))))    //接MultiInputMux，每个cacheline有8路
    val subMultiInputBuffer = Seq.fill(8)(Module(new MultiInputBuffer(numWay = 8, width = 64 + 5 , depth = 3)))    //8个cacheline , 每个cacheline拆成8路，每路是8B的向量，外加一个5bit的id

    io.RegFileAllWrite_io.addr := io.FSM_MLU_io.md    //选择写入的寄存器
    io.RegFileAllWrite_io.act := true.B      //端口始终激活，但是写数据时valid不一定生效
    
    
    for(y <- 0 until 8){//为每个Cacheline分时缓冲

        // 入，8路
        val id = io.MLU_L2_io.Cacheline_ReadBack_io(y).id
        val valid = io.MLU_L2_io.Cacheline_ReadBack_io(y).valid

        for(i <- 0 until 8){
            wireSplit(y)(i) := Cat( io.MLU_L2_io.Cacheline_ReadBack_io(y).data(i * 8 + 7 , i * 8) , id)
        }

        for(i <- 0 until 8){
            subMultiInputBuffer(y).io.in(i) := wireSplit(y)(i)
        }
        subMultiInputBuffer(y).io.in_valid := valid  //有效

        // 出，1路

        subMultiInputBuffer(y).io.out.ready := true.B  //永远都想读
        val readvalid = subMultiInputBuffer(y).io.out.valid    //判断读出的数据是否有意义
        val output = subMultiInputBuffer(y).io.out.bits //读数据
        val data = output(68 , 5)   //数据段
        val bankId = output(1 , 0) * 8.U + y.U  //bank号
        val index = Mux(output(2) === 0.U , output(2) * 4.U + output(4,3) * 8.U , output(2) * 4.U + output(4,3) * 8.U + 28.U)  //bank内地址

        //!!!这样写会有问题
        io.RegFileAllWrite_io.w(bankId).req.bits.setIdx := index
        io.RegFileAllWrite_io.w(bankId).req.bits.data.head := data
        io.RegFileAllWrite_io.w(bankId).req.valid := readvalid
    }

}