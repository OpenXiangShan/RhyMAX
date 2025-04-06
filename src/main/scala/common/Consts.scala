package common

import chisel3._

//m <= tileK/k  
//n >= 4
object Consts {

  /*    MMAU    */
  val WORD_LEN      = 64
  val ADDR_LEN      = 16
  val Tr_LEN        = 8
  val Acc_LEN       = 32

  val tileM         = 16
  val tileN         = 16
  val tileK         = 64

  val m             = 4   
  val n             = 8  
  val k             = 16
  // val tileM         = 64
  // val tileN         = 64
  // val tileK         = 256

  // val m             = 32   
  // val n             = 32  
  // val k             = 8

  val numM          = tileM / m
  val numN          = tileN / n
  val numK          = tileK / k

  /*    RegFile   */
  val genTr: Data = UInt(Tr_LEN.W)
  val setTr: Int = tileM
  val wayTr: Int = 1
  val dataSplitTr: Int = 1
  val setSplitTr: Int = 1
  val waySplitTr: Int = 1
  val useBitmaskTr: Boolean = false
  val numTrBank: Int = m
}

class MMAUFormat extends Module{
  val WORD_LEN      = Consts.WORD_LEN
  val ADDR_LEN      = Consts.ADDR_LEN
  val tileM         = Consts.tileM
  val tileN         = Consts.tileN
  val tileK         = Consts.tileK

  val m             = Consts.m
  val n             = Consts.n
  val k             = Consts.k

  val numM          = Consts.numM
  val numN          = Consts.numN
  val numK          = Consts.numK
}

class RegFileFormat extends Module{
  val genTr = Consts.genTr
  val setTr = Consts.setTr
  val wayTr = Consts.wayTr
  val dataSplitTr = Consts.dataSplitTr
  val setSplitTr = Consts.setSplitTr
  val waySplitTr = Consts.waySplitTr
  val useBitmaskTr = Consts.useBitmaskTr
  val numTrBank = Consts.numTrBank
}
