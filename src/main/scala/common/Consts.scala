package common

import chisel3._
import chisel3.util._



//m <= tileK/k  
//n >= 4
//m=n,tileM=tileN
object Consts {

  /*    MMAU    */
  val WORD_LEN      = 64


  // val tileM         = 32
  // val tileN         = 32
  // val tileK         = 64

  // val m             = 8   
  // val n             = 8  
  // val k             = 2
  val tileM         = 64
  val tileN         = 64
  val tileK         = 256

  val m             = 32   
  val n             = 32  
  val k             = 8

  val numM          = tileM / m
  val numN          = tileN / n
  val numK          = tileK / k

  /*    RegFile   */
  val sramLatency: Int = 1 
  //Tr
  val Tr_LEN        = 8 * k//Tr单个元素位宽（向量）

  val genTr: Data = UInt(Tr_LEN.W)
  val setTr: Int = numM * numK
  val wayTr: Int = 1
  val dataSplitTr: Int = k
  val setSplitTr: Int = 1
  val waySplitTr: Int = 1
  val useBitmaskTr: Boolean = false

  val numTr: Int = 4
  val numTrBank: Int = m
  val numTrReadPort: Int = 3
  val numTrWritePort: Int = 3

  val Tr_INDEX_LEN    = log2Ceil(setTr) //Tr寻址时index需要的位宽
  val Tr_ADDR_LEN     = log2Ceil(numTr) //表示Tr标号所需位宽（0～3）
  

  //Acc
  val Acc_LEN       = 32 * 4

  val genAcc: Data = UInt(Acc_LEN.W)
  val setAcc: Int = numM * numN * m
  val wayAcc: Int = 1
  val dataSplitAcc: Int = 4
  val setSplitAcc: Int = 1
  val waySplitAcc: Int = 1
  val useBitmaskAcc: Boolean = false

  val numAcc: Int = 4
  val numAccBank: Int = n / 4
  val numAccReadPort: Int = 3
  val numAccWritePort: Int = 3

  val Acc_INDEX_LEN   = log2Ceil(setAcc)
  val Acc_ADDR_LEN     = log2Ceil(numAcc) //表示Acc标号所需位宽（0～3）

  //All
  val All_LEN       = scala.math.max(Acc_LEN , Tr_LEN)

  val genAll: Data = UInt(All_LEN.W)
  val setAll: Int = scala.math.max(setAcc , setTr)
  val wayAll: Int = 1
  val dataSplitAll: Int = 4
  val setSplitAll: Int = 1
  val waySplitAll: Int = 1
  val useBitmaskAll: Boolean = false

  val numAllBank: Int = scala.math.max(numAccBank , numTrBank)
  val numAllReadPort: Int = 1
  val numAllWritePort: Int = 1

  val All_INDEX_LEN   = log2Ceil(setAll)
  val All_ADDR_LEN     = log2Ceil(numAcc + numTr) //表示所有寄存器标号所需位宽（0～7）

}

class MMAUFormat extends Module{
  val WORD_LEN      = Consts.WORD_LEN
  // val ADDR_LEN      = Consts.ADDR_LEN
  val Tr_INDEX_LEN    = Consts.Tr_INDEX_LEN
  val Acc_INDEX_LEN   = Consts.Acc_INDEX_LEN

  val tileM         = Consts.tileM
  val tileN         = Consts.tileN
  val tileK         = Consts.tileK

  val m             = Consts.m
  val n             = Consts.n
  val k             = Consts.k

  val numM          = Consts.numM
  val numN          = Consts.numN
  val numK          = Consts.numK

  val sramLatency = Consts.sramLatency
}

class RegFileFormat extends Module{
  //Tr
  val Tr_LEN        = Consts.Tr_LEN //Tr单个元素位宽（向量）
  val Tr_INDEX_LEN    = Consts.Tr_INDEX_LEN //Tr寻址时index需要的位宽
  val genTr = Consts.genTr
  val setTr = Consts.setTr
  val wayTr = Consts.wayTr
  val dataSplitTr = Consts.dataSplitTr
  val setSplitTr = Consts.setSplitTr
  val waySplitTr = Consts.waySplitTr
  val useBitmaskTr = Consts.useBitmaskTr
  val numTr = Consts.numTr
  val numTrBank = Consts.numTrBank
  val numTrReadPort = Consts.numTrReadPort
  val numTrWritePort = Consts.numTrWritePort

  //Acc
  val Acc_LEN        = Consts.Acc_LEN //Acc单个元素位宽（向量）
  val Acc_INDEX_LEN    = Consts.Acc_INDEX_LEN //Acc寻址时index需要的位宽
  val genAcc = Consts.genAcc
  val setAcc = Consts.setAcc
  val wayAcc = Consts.wayAcc
  val dataSplitAcc = Consts.dataSplitAcc
  val setSplitAcc = Consts.setSplitAcc
  val waySplitAcc = Consts.waySplitAcc
  val useBitmaskAcc = Consts.useBitmaskAcc
  val numAcc = Consts.numAcc
  val numAccBank = Consts.numAccBank
  val numAccReadPort = Consts.numAccReadPort
  val numAccWritePort = Consts.numAccWritePort

  //All
  val All_LEN       = Consts.All_LEN

  val genAll = Consts.genAll
  val setAll = Consts.setAll
  val wayAll = Consts.wayAll
  val dataSplitAll = Consts.dataSplitAll
  val setSplitAll = Consts.setSplitAll
  val waySplitAll = Consts.waySplitAll
  val useBitmaskAll = Consts.useBitmaskAll

  val numAllBank = Consts.numAllBank
  val numAllReadPort = Consts.numAllReadPort
  val numAllWritePort = Consts.numAllWritePort

  val All_INDEX_LEN   = Consts.All_INDEX_LEN

}
