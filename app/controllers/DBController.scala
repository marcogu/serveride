package controllers

import models.reqarg._
import play.Logger
import play.api.mvc._
import play.api.mvc.Controller
import services.mpos.{DbCfg, DBService}
import services.Project
import play.api.libs.json.Json

/**
  * Created by marco on 2017/1/22.
  */
class DBController extends Controller{

  @deprecated("This is a test function, use query(driver) instead")
  def query() = Action(parse.form(sqlForm)) { req =>
    Logger.debug(req.body.sql)
    Ok(Json.toJson(DBService(DbCfg.default()).query(req.body.sql)))
  }

  def clearalldb() = Action {
    DBService.cleanall()
    Ok(Json.toJson("complete"))
  }

  def queryWithCfg(url:String, act:String, pwd:String, driver:String) = Action(parse.form(sqlForm)) { req=>
    Logger.debug(s"url=$url act=$act pwd=$pwd driver=$driver\nquery=${req.body.sql}")
    Ok(Json.toJson(DBService(DbCfg(url = url, act = act, pwd = pwd, driver = driver)).query(req.body.sql)))
  }

  def listds() = Action {
    Ok(Json.toJson(DBService.alldsCfg(cfg => Map(cfg.act->cfg.url)).toArray))
  }

  // test did passed
  def embededH2() = Action {
    Ok(Project.named(Project.selfName).path)
  }
}
