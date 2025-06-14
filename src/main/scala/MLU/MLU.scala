package MLU

import chisel3._
import chisel3.util._

import common._
import Expander._
import RegFile._
import IssueQueen._



class MLU extends Module{
    val io = IO(new Bundle {
        val FSM_MLU_io = Flipped(new FSM_MLU_IO)    //接FSM
        val MLU_L2_io = new MLU_L2_IO   //访问L2
        val RegFileAllWrite_io = Flipped(new RegFileAllWrite_IO)  //写RF
    })

    /*  read from L2 */
    val readBuffer = Seq.fill(8)(Module(new SimpleHandshakeFIFO(depth = 10, width = Consts.L2_ADDR_LEN + Consts.L2_ID_LEN))) //L2读请求缓冲
    for(y <- 0 until 8){//对每个Cacheline
        //写入FIFO
        readBuffer(y).io.enq_valid := io.FSM_MLU_io.Cacheline_Read_io(y).valid
        io.FSM_MLU_io.Cacheline_Read_io(y).ready := readBuffer(y).io.enq_ready  //实际没有作用,FSM不管这个信号,因此FIFO要足够大
        readBuffer(y).io.enq_bits := Cat(io.FSM_MLU_io.Cacheline_Read_io(y).addr , io.FSM_MLU_io.Cacheline_Read_io(y).id) 

        //读出FIFO
        io.MLU_L2_io.Cacheline_Read_io(y).valid := readBuffer(y).io.deq_valid
        readBuffer(y).io.deq_ready := io.MLU_L2_io.Cacheline_Read_io(y).ready
        io.MLU_L2_io.Cacheline_Read_io(y).addr := readBuffer(y).io.deq_bits(Consts.L2_ADDR_LEN + Consts.L2_ID_LEN - 1 , Consts.L2_ID_LEN)
        io.MLU_L2_io.Cacheline_Read_io(y).id := readBuffer(y).io.deq_bits(Consts.L2_ID_LEN - 1 , 0)
    }
    // io.FSM_MLU_io.Cacheline_Read_io <> io.MLU_L2_io.Cacheline_Read_io

    /*    写RF    */
    val wireSplit = Wire(Vec(8, Vec(8, UInt((64 + 5 + 3).W))))  //接MultiInputMux，每个cacheline有8路
    val subMultiInputBuffer = Seq.fill(8)(Module(new MultiInputBuffer(numWay = 8, width = 64 + 5 + 3, depth = 32)))    //8个cacheline , 每个cacheline拆成8路，每路是8B的向量，外加一个5bit的id ,再加一个index的偏移量offset（0～7）

    io.RegFileAllWrite_io.addr := io.FSM_MLU_io.md    //选择写入的寄存器
    // io.RegFileAllWrite_io.act := true.B      
    io.RegFileAllWrite_io.act := io.FSM_MLU_io.sigPortState // 由FSM控制(实际上是由MLU自己控制，因为只有MLU才知道这条指令什么时候结束，不过状态机在FSM内)

    for(i <- 0 until Consts.numAllBank){    //写端口赋缺省值
        io.RegFileAllWrite_io.w(i).req.bits.setIdx := 0.U
        io.RegFileAllWrite_io.w(i).req.bits.data.head := 0.U
        io.RegFileAllWrite_io.w(i).req.valid := false.B
    }
    
    
    for(y <- 0 until 8){//为每个Cacheline分时缓冲

        // 入，8路
        val id = io.MLU_L2_io.Cacheline_ReadBack_io(y).id
        val valid = io.MLU_L2_io.Cacheline_ReadBack_io(y).valid

        for(i <- 0 until 8){//cacheline内部拆8份
            // wireSplit(y)(i) := Cat(i.U(3.W) , io.MLU_L2_io.Cacheline_ReadBack_io(y).data(i * 64 + 63 , i * 64) , id )
            wireSplit(y)(i) := Cat( (7 - i).U(3.W) , io.MLU_L2_io.Cacheline_ReadBack_io(y).data(i * 64 + 63 , i * 64) , id )
        }

        for(i <- 0 until 8){
            subMultiInputBuffer(y).io.in(i) := wireSplit(y)(i)
            // subMultiInputBuffer(y).io.in(i) := wireSplit(y)(7 - i)  //大端
        }
        subMultiInputBuffer(y).io.in_valid := valid  //有效

        // // debug
        // printf(p"\n[DEBUG] ====== Cacheline_ReadBack_io($y) ======\n")
        // printf(p"[DEBUG] id    = 0x${Hexadecimal(id)}\n")
        // printf(p"[DEBUG] valid = $valid\n")
        // for (i <- 0 until 8) {
        // printf(p"[DEBUG] wireSplit($y)($i) = 0x${Hexadecimal(wireSplit(y)(i))}\n")
        // }
        // printf(p"===============================================\n")

        // 出，1路

        subMultiInputBuffer(y).io.out.ready := true.B  //始终希望读

        val readvalid = Wire(Bool())
        readvalid := subMultiInputBuffer(y).io.out.valid    //判断读出的数据是否有意义

        val output = Wire(UInt(72.W))
        output := subMultiInputBuffer(y).io.out.bits //读数据

        val data = Wire(UInt(64.W))
        data := output(68 , 5)   //数据段

        val bankId = Wire(UInt(5.W))
        bankId := Cat(output(1,0), y.U(3.W)) //bank号

        val index = Wire(UInt(Consts.All_INDEX_LEN.W))
        index := Mux(output(2) === 0.U , output(2) * 4.U + output(4,3) * 8.U , output(2) * 4.U + output(4,3) * 8.U + 28.U)  //bank内地址
        
        val offset = Wire(UInt(Consts.All_INDEX_LEN.W))
        offset := output(71 , 69)    //偏移量，加上index才是真正的地址

        for (i <- 0 until 4) {//对每条特定的cacheline，仅有4个路由目标
            when(bankId === (i * 8 + y).U) { //！！！
                io.RegFileAllWrite_io.w(i * 8 + y).req.bits.setIdx := index + offset
                io.RegFileAllWrite_io.w(i * 8 + y).req.bits.data.head := data
                io.RegFileAllWrite_io.w(i * 8 + y).req.valid := readvalid
            }.otherwise {
                io.RegFileAllWrite_io.w(i * 8 + y).req.bits.setIdx := 0.U
                io.RegFileAllWrite_io.w(i * 8 + y).req.bits.data.head := 0.U
                io.RegFileAllWrite_io.w(i * 8 + y).req.valid := false.B
            }
// debug
// printf(p"[DEBUGGGG] y = $y -> bankId = $bankId\n")
// printf(p"[DEBUGGGG] WritePort[${i * 8 + y}] setIdx = 0x${Hexadecimal(io.RegFileAllWrite_io.w({i * 8 + y}).req.bits.setIdx)}\n")
// printf(p"[DEBUGGGG] WritePort[${i * 8 + y}] data = 0x${Hexadecimal(io.RegFileAllWrite_io.w({i * 8 + y}).req.bits.data.head.asUInt)}\n")
// printf(p"[DEBUGGGG] WritePort[${i * 8 + y}] valid  = ${io.RegFileAllWrite_io.w({i * 8 + y}).req.valid}\n")

        }

        io.FSM_MLU_io.sigLineDone(y) := Mux(readvalid , true.B , false.B)   //每写入成功一次，则上报一次


        // debug
        // printf(p"\n[DEBUG] ====== subMultiInputBuffer($y) Output ======\n")
        // printf(p"[DEBUG] readvalid = $readvalid\n")
        // printf(p"[DEBUG] output    = 0x${Hexadecimal(output)}\n")
        // printf(p"[DEBUG] data      = 0x${Hexadecimal(data)}\n")
        // printf(p"[DEBUG] bankId    = ${bankId} (${Hexadecimal(bankId)})\n")
        // printf(p"[DEBUG] index     = ${index} (${Hexadecimal(index)})\n")
        // printf(p"[DEBUG] offset    = ${offset} (${Hexadecimal(offset)})\n")
        // printf(p"========================================\n")


    }

    /*  sigDone   */
    // io.FSM_MLU_io.sigDone := false.B

}