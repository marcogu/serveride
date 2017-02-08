package controllers

import play.api.Environment
import play.api.libs.json.Json
import play.api.mvc._
import services.inspection.AppEnv


class Application extends Controller {
  def env = Action { Ok(Json.toJson(AppEnv.hostInfo)) }
  def pid = Action { Ok(AppEnv.processId) }
  def tt = Action {Ok(Environment.simple().rootPath.getCanonicalPath)}
}