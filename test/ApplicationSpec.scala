import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import controllers.Application

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Your new application is ready.")
    }

    "marco test hello world" in new WithApplication{
      val fakeRequestXmlBody = "requestXml=%3CQtPay+userIP%3D%2210.141.2.154%22+clientType%3D%2202%22+osType%3D%22an" +
        "droid5.1%22+token%3D%22E3615CBE1B46A881A19DE207FF6DC8B6%22+mobileSerialNum%3D%22null00000000000000000000000" +
        "0000000000000%22+version%3D%222.0.6%22+phone%3D%2218801911528%22+application%3D%22BankCardBalance.Req%22+ap" +
        "pUser%3D%22hlf%22%3E%3CICCardSerial%2F%3E%3CmobileNo%3E18801911528%3C%2FmobileNo%3E%3CtransTime%3E182526%3C" +
        "%2FtransTime%3E%3Csign%3E0D507A74A0FE1E7155B1B10DF4576A21%3C%2Fsign%3E%3CorderId%3E0000000000000000%3C%2For" +
        "derId%3E%3CcardPassword%3E348516BDBE2274D0A7F654C58335743C1BAFD64A87829CE64985813880C063928FFA5703CE8441916" +
        "399C34E37681A1DFA08419E350C75919D959E1CC37017ACC015B7321BB1C7C0954F3EE0CD125CB7642FE75CAF1BF2D7F88512CBEE10" +
        "953912AE81B460014340D8E4B76F5EDA8B460618761BDB2E0F88F9CCA3DD71017DE2%3C%2FcardPassword%3E%3CcardInfo%3EFF00" +
        "008B0018380412130ABC499FBA838948F709D6AFCC0942080B88D75B4F74DF9FB432D95756278BB1407C1B3BAD7AE3420CA366CFEE5" +
        "3AA1E2A6CF33683A49E17E17340F1E2D412E154134EEB7B0D47A666D99D34720723EF3D80F6D4F00182570008000427508257000800" +
        "0427503632323834383038383035393834373932313534393132CFE8266695BDAF28%3C%2FcardInfo%3E%3CmerchantId%2F%3E%3C" +
        "ICCardInfo%2F%3E%3CtransLogNo%3E000119%3C%2FtransLogNo%3E%3CICCardValidDate%3E34393132%3C%2FICCardValidDate" +
        "%3E%3CproductId%2F%3E%3CtransDate%3E20170109%3C%2FtransDate%3E%3CencodeType%3Ebankpassword%3C%2FencodeType%3" +
        "E%3C%2FQtPay%3E"
      val tempCase = """ {"name": "New Group", "collabs": ["foo", "asdf"]} """
      val fakeRequest = FakeRequest(POST, "/test", FakeHeaders(), body=fakeRequestXmlBody)


//      val testAction = new Application().testAction.apply(fakeRequest)
//      status(testAction) must equalTo(OK)
//      contentAsString(testAction) must contain ("Hello play")
    }
  }


}
