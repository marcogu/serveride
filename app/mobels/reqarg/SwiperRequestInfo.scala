package mobels.reqarg

import business.{SwiperDeal, WK}

/**
  * Created by marco on 2017/1/11.
  */
case class SwiperRequestInfo(trackWk:WK, macWK:WK, request:DealDescriptor, swiperInfo:SwiperDeal)

object SwiperInfoBuild {
  def apply(deal:DealDescriptor, swiperDeal:SwiperDeal, mkey:String) = {
    val fullPsamFromKsn:String = swiperDeal.psam.substring(4, 20)
    val merchantId:String = fullPsamFromKsn.substring(0, 8)
    val trackWorkKey = WK(mkey, merchantId, fullPsamFromKsn, swiperDeal.trackRandom, deal.transLogNo)
    val macWorkKey = WK(mkey, merchantId, fullPsamFromKsn, swiperDeal.macRandom, deal.transLogNo)
    SwiperRequestInfo(trackWorkKey, macWorkKey,deal, swiperDeal)
  }
}
