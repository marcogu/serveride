package controllers

import business.SwiperDeal
import play.api._
import play.api.mvc._
import mobels.reqarg._


class Application extends Controller {
  def index = Action { Ok(views.html.index("Your new application is ready."))}

  implicit val swiperdb:Map[String, String] = Map(
    "F00182570008000427508257000800042750"->"11111111111111111111111111111111",
    "F00182570010004877258257001000487725"->"9D37F2AF79A3B42F9D37F2AF79A3B42F")

  def cqjrMockDeal = Action(parse.form(xmlForm)) { req =>
    val requestPram:DealDescriptor = ParamBuilde.build(req.body)
    val swiperDeal:SwiperDeal = SwiperDeal(requestPram.cardInfo)
    val builder = SwiperInfoBuild(requestPram, swiperDeal, MockMkQuery.queryMainKey(swiperDeal.psam))

    val responseInfo = s"\ntrack working key is: ${builder.trackWk.key}\n" +
      s"mac working key is: ${builder.macWK.key}\n"
    Ok(responseInfo)
  }
}