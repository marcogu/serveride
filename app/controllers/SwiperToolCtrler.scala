package controllers

import business.{MACer, WK, SwiperDeal}
import models.reqarg._
import security.TripleDesEncrypt
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.api.mvc._
/**
  * Created by marco on 2017/1/17.
  */
class SwiperToolCtrler extends Controller{
  implicit val swiperdb:Map[String, String] = Map(
    "F00182570008000427508257000800042750"->"11111111111111111111111111111111",
    "F00182570010004877258257001000487725"->"9D37F2AF79A3B42F9D37F2AF79A3B42F")

  /**
    * 老mac 算法
    * curl -d "requestXml=%3cQtPay+appUser%3d%22hlf%22+application%3d%22BankCardBalance.Req%22+clientType%3d%2202%22+mobileSerialNum%3d%22null000000000000000000000000000000000000%22+osType%3d%22android5.1%22+phone%3d%2218801911528%22+token%3d%22E3615CBE1B46A881A19DE207FF6DC8B6%22+userIP%3d%2210.141.2.154%22+version%3d%222.0.6%22%3e%3cICCardSerial%2f%3e%3cmobileNo%3e18801911528%3c%2fmobileNo%3e%3ctransTime%3e182526%3c%2ftransTime%3e%3csign%3e0D507A74A0FE1E7155B1B10DF4576A21%3c%2fsign%3e%3corderId%3e2016022581801851%3c%2forderId%3e%3ccardPassword%3e348516BDBE2274D0A7F654C58335743C1BAFD64A87829CE64985813880C063928FFA5703CE8441916399C34E37681A1DFA08419E350C75919D959E1CC37017ACC015B7321BB1C7C0954F3EE0CD125CB7642FE75CAF1BF2D7F88512CBEE10953912AE81B460014340D8E4B76F5EDA8B460618761BDB2E0F88F9CCA3DD71017DE2%3c%2fcardPassword%3e%3ccardInfo%3eFF000054001800041214BB7D783A981055D71EAB2D8C8E25A451F54D6C9E02A44E5751309FA0F00182570010004877258257001000487725303632313738353038303030303833323535343832343037F7D9AC525176BD4A%3c%2fcardInfo%3e%3cmerchantId%2f%3e%3cICCardInfo%2f%3e%3ctransLogNo%3e000188%3c%2ftransLogNo%3e%3cICCardValidDate%3e32343037%3c%2fICCardValidDate%3e%3cproductId%2f%3e%3ctransDate%3e20170109%3c%2ftransDate%3e%3cencodeType%3ebankpassword%3c%2fencodeType%3e%3c%2fQtPay%3e" "http://localhost:9000/mockserivce/olderMac"
    * 按文档 新mac算法
    * curl -d "requestXml=%3cQtPay+appUser%3d%22hlf%22+application%3d%22BankCardBalance.Req%22+clientType%3d%2202%22+mobileSerialNum%3d%22null000000000000000000000000000000000000%22+osType%3d%22android5.1%22+phone%3d%2218801911528%22+token%3d%22E3615CBE1B46A881A19DE207FF6DC8B6%22+userIP%3d%2210.141.2.154%22+version%3d%222.0.6%22%3e%3cICCardSerial%2f%3e%3cmobileNo%3e18801911528%3c%2fmobileNo%3e%3ctransTime%3e182526%3c%2ftransTime%3e%3csign%3e0D507A74A0FE1E7155B1B10DF4576A21%3c%2fsign%3e%3corderId%3e2016022581801851%3c%2forderId%3e%3ccardPassword%3e348516BDBE2274D0A7F654C58335743C1BAFD64A87829CE64985813880C063928FFA5703CE8441916399C34E37681A1DFA08419E350C75919D959E1CC37017ACC015B7321BB1C7C0954F3EE0CD125CB7642FE75CAF1BF2D7F88512CBEE10953912AE81B460014340D8E4B76F5EDA8B460618761BDB2E0F88F9CCA3DD71017DE2%3c%2fcardPassword%3e%3ccardInfo%3eFF000054001800041214BB7D783A981055D71EAB2D8C8E25A451F54D6C9E02A44E5751309FA0F00182570010004877258257001000487725303632313738353038303030303833323535343832343037F7D9AC525176BD4A%3c%2fcardInfo%3e%3cmerchantId%2f%3e%3cICCardInfo%2f%3e%3ctransLogNo%3e000188%3c%2ftransLogNo%3e%3cICCardValidDate%3e32343037%3c%2fICCardValidDate%3e%3cproductId%2f%3e%3ctransDate%3e20170109%3c%2ftransDate%3e%3cencodeType%3ebankpassword%3c%2fencodeType%3e%3c%2fQtPay%3e" "http://localhost:9000/mockserivce/a"
    * {"track2":"6217850800008325548D24072201000058300FFFFFFFFFFF","track3":"","reMac":"E69C257B3A9C8CDB"}%
    */
  def cqjrMockDeal(useOldMacCac:String) = Action(parse.form(xmlForm)) { req =>
    val requestPram:DealDescriptor = ParamBuilde.build(req.body)
    Logger.debug(s"request xml=${requestPram.contentXml}")
    val swiperDeal:SwiperDeal = SwiperDeal.parser(requestPram.cardInfo)

    val plaintInfo = useOldMacCac match {
      case "olderMac" =>SwiperInfoBuild.cacPlaint(SwiperInfoBuild(requestPram, swiperDeal,
        MockMkQuery.queryMainKey(swiperDeal.psam)), SwiperInfoBuild.mbaOld)
      case other =>SwiperInfoBuild.cacPlaint(SwiperInfoBuild(requestPram, swiperDeal,
        MockMkQuery.queryMainKey(swiperDeal.psam)), SwiperInfoBuild.MBA)
    }

    Logger.debug(s"paint info track2=${plaintInfo.track2}, track3=${plaintInfo.track3}, mac=${plaintInfo.reMac}")
    Ok(Json.toJson(plaintInfo))
  }

  val dealInfoJsonForamt = Json.format[SwiperDeal]

  def parserCardInfo(str:String) = Action {
    val swiperDeal:SwiperDeal = SwiperDeal.parser(str)
    Ok(Json.toJson(swiperDeal)(dealInfoJsonForamt))
  }

  import security.TripleDesEncrypt._

  def decodeTranc2By(cinfo:String, tslog:String, oldMac:String) = Action {
    val swiperDeal:SwiperDeal = SwiperDeal.parser(cinfo)
    val fullPsamFromKsn:String = swiperDeal.psam.substring(4, 20)
    val merchantId:String = fullPsamFromKsn.substring(0, 8)
    val mainKey = MockMkQuery.queryMainKey(swiperDeal.psam)
    val trackWorkKey = WK(mainKey, merchantId, fullPsamFromKsn, swiperDeal.trackRandom, tslog)
    val macWorkKey = WK(mainKey, merchantId, fullPsamFromKsn, swiperDeal.macRandom, tslog)

    val t2:String = decript(adpatorKeyLen(trackWorkKey.key), swiperDeal.track2)
    val t3:String = decript(adpatorKeyLen(trackWorkKey.key), swiperDeal.track3)
    val jsonResult = Map("trackKey" -> trackWorkKey.key,
      "macKey"->macWorkKey.key,
      "track2"->t2,
      "track3"->t3)

    Ok(Json.toJson(jsonResult))
  }

  def wkey(random:String, tslog:String, ksn:String, mKey:String) = Action {
    val fullPsamFromKsn:String = ksn.substring(4, 20)
    val merchantId:String = fullPsamFromKsn.substring(0, 8)
    val wk = WK(mKey, merchantId, fullPsamFromKsn, random, tslog)
    Ok(Json.toJson(Map("workingKey" -> wk.key )))
  }

  def macCacWith(workingKey:String, data:String) = Action {
    val result:String = MACer.cacMacd(data, workingKey)
    Ok(Json.toJson(Map("mac" -> result )))
  }

  def des3(key:String, data:String) = Action {
    val result:String = TripleDesEncrypt.encrypt(adpatorKeyLen(key), data)
    Ok(result)
  }

  def d3des(key:String, data:String) = Action {
    val result:String = TripleDesEncrypt.decript(adpatorKeyLen(key), data)
    Ok(result)
  }
}
