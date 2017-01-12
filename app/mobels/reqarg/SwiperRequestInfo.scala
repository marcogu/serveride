package mobels.reqarg

import business.{MACer, SwiperDeal, WK}
import play.api.Logger
import play.api.libs.json.Json
import security.BCDCoder


/**
  * Created by marco on 2017/1/11.
  */

case class PaintSwiperInfo(track2:String, track3:String, reMac:String)
case class SwiperRequestInfo(trackWk:WK, macWK:WK, request:DealDescriptor, swiperInfo:SwiperDeal)

object PaintSwiperInfo{
  implicit val jsFormat = Json.format[PaintSwiperInfo]
}


object SwiperInfoBuild {
  def apply(deal:DealDescriptor, swiperDeal:SwiperDeal, mkey:String):SwiperRequestInfo = {
    val fullPsamFromKsn:String = swiperDeal.psam.substring(4, 20)
    val merchantId:String = fullPsamFromKsn.substring(0, 8)
    val trackWorkKey = WK(mkey, merchantId, fullPsamFromKsn, swiperDeal.trackRandom, deal.transLogNo)
    val macWorkKey = WK(mkey, merchantId, fullPsamFromKsn, swiperDeal.macRandom, deal.transLogNo)
    SwiperRequestInfo(trackWorkKey, macWorkKey,deal, swiperDeal)
  }

  import security.TripleDesEncrypt._
  def cacPlaint(reqInfo:SwiperRequestInfo):PaintSwiperInfo = {
    Logger.debug(s"track working key=${reqInfo.trackWk.key}, mac working key=${reqInfo.macWK.key}")
    val t2:String = decript(adpatorKeyLen(reqInfo.trackWk.key), reqInfo.swiperInfo.track2)
    val t3:String = decript(adpatorKeyLen(reqInfo.trackWk.key), reqInfo.swiperInfo.track3)
    val mc:String = MACer.cacMacd( mbaOld(reqInfo) , reqInfo.macWK.key )
    PaintSwiperInfo ( t2, t3, mc)
  }

  def mbaOld(d:SwiperRequestInfo):String ={
    val r = d.swiperInfo.track2 +
      d.swiperInfo.track3 +
      d.swiperInfo.trackRandom +
      d.swiperInfo.psam +
      BCDCoder.ascToBcd(d.request.orderId)
    Logger.debug(s"MBA info=$r")
    r
  }

  def MBA(swiperInfo:SwiperDeal):String = swiperInfo.track2 + swiperInfo.track3 + swiperInfo.trackRandom + "0000001"
}
