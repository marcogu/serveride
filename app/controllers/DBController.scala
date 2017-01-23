package controllers

import javax.inject.Inject
import models.reqarg._
import play.Logger
import play.api.mvc._
import play.api.db.Database
import play.api.mvc.Controller
import services.mpos.DBService
import play.api.libs.json.Json

/**
  * Created by marco on 2017/1/22.
  */
class DBController @Inject() (db:Database)  extends Controller{
  var session = DBService.session(db)

  def query() = Action(parse.form(sqlForm)) { req =>
    Logger.debug(req.body.sql)
    Ok(Json.toJson(session.query(req.body.sql)))
  }
}
