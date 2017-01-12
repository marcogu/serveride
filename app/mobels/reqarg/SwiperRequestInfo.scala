package mobels.reqarg

import business.{MACer, SwiperDeal, WK}


/**
  * Created by marco on 2017/1/11.
  */

case class PaintSwiperInfo(track2:String, track3:String, reMac:String)
case class SwiperRequestInfo(trackWk:WK, macWK:WK, request:DealDescriptor, swiperInfo:SwiperDeal)


object SwiperInfoBuild {
  def apply(deal:DealDescriptor, swiperDeal:SwiperDeal, mkey:String):SwiperRequestInfo = {
    val fullPsamFromKsn:String = swiperDeal.psam.substring(4, 20)
    val merchantId:String = fullPsamFromKsn.substring(0, 8)
    val trackWorkKey = WK(mkey, merchantId, fullPsamFromKsn, swiperDeal.trackRandom, deal.transLogNo)
    val macWorkKey = WK(mkey, merchantId, fullPsamFromKsn, swiperDeal.macRandom, deal.transLogNo)
    SwiperRequestInfo(trackWorkKey, macWorkKey,deal, swiperDeal)
  }

  import security.TripleDesEncrypt._
  def cacPlaint(reqInfo:SwiperRequestInfo):PaintSwiperInfo = PaintSwiperInfo (
    byts2hex(decript(adpatorKeyLen(reqInfo.trackWk.key), reqInfo.swiperInfo.track2)),
    byts2hex(decript(adpatorKeyLen(reqInfo.trackWk.key), reqInfo.swiperInfo.track3)),
    MACer.cacMacd( MBA(reqInfo.swiperInfo) , reqInfo.macWK.key )
  )

  def MBA(swiperInfo:SwiperDeal):String = swiperInfo.track2 + swiperInfo.track3 + swiperInfo.trackRandom + "amount"
}
