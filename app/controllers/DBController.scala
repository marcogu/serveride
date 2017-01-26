package controllers

import models.reqarg._
import play.Logger
import play.api.mvc._
import play.api.mvc.Controller
import services.mpos.{DbCfg, DBService}
import play.api.libs.json.Json

/**
  * Created by marco on 2017/1/22.
  */
class DBController extends Controller{

  def query() = Action(parse.form(sqlForm)) { req =>
    Logger.debug(req.body.sql)
    Ok(Json.toJson(DBService(DbCfg.default()).query(req.body.sql)))
  }
}
