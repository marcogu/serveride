package mobels.reqarg
/**
  * Created by marco on 2017/1/11.
  */
import language.postfixOps
import scala.xml.{XML, Elem}


case class XMLPack(requestXml:String) extends XMLParam {
  val xml = XML.loadString(requestXml)
  def contentXml = xml
}

trait XMLParam{
  def contentXml:Elem
  def descriptor:String = contentXml \ "@application" text
}

trait DealDescriptor extends XMLParam {
  lazy val mobileNo:String = contentXml \\ "mobileNo" text
  lazy val transTime:String = contentXml \\ "transTime" text
  lazy val cardPassword:String = contentXml \\ "cardPassword" text
  lazy val orderId:String = contentXml \\ "orderId" text
  lazy val cardInfo:String = contentXml \\ "cardInfo" text
  lazy val merchantId:String = contentXml \\ "merchantId" text
  lazy val transLogNo:String = contentXml \\ "transLogNo" text
  lazy val ICCardValidDate:String = contentXml \\ "ICCardValidDate" text
  lazy val transDate:String = contentXml \\ "transDate" text
  lazy val orderAmt:String = contentXml \\ "orderAmt" text
}

class Deal(pack:XMLParam) extends DealDescriptor{
  def contentXml = pack.contentXml
}

object ParamBuilde {
  def build[T](xmlPack:XMLParam):T = xmlPack.descriptor match {
    case "BankCardBalance.Req" | "JFPalCardPay.Req" => new Deal(xmlPack).asInstanceOf[T]
    case other => null.asInstanceOf[T]
  }
}