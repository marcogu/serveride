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
    val dbservice = DBService(DbCfg(url = "jdbc:h2:./public/toolappdef.db", act = "marco", pwd = "123456",
      driver = "org.h2.Driver"))
    val session = dbservice.genQuerySession
    session.exc("CREATE TABLE project ( pn VARCHAR(128), purl VARCHAR(1024) )")
    session.exc("INSERT INTO project VALUES ('crjqMobileDevServer', " +
      "'/Users/marco/Documents/workspace/jvm/scala/CrjqSwiperMockServer')")
    val info = session.query("SELECT * FROM project")
    session.finish()
    Ok(Json.toJson(info))
  }
}
