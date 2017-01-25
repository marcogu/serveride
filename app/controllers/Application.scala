package controllers

import javax.inject.Inject

import models.viewparam.MainTempateArguments
import play.api.{Logger, Environment}
import play.api.mvc._
import services.inspection.ServerEnv


class Application @Inject() (env:Environment) extends Controller {

  def testEditor = Action(Ok(views.html.codereditorfull(MainTempateArguments())))

  def defaultSouceCode = {
    Logger.debug(ServerEnv(env).srcroot.path)
    Action(Ok(ServerEnv(env).defaultTest))
  }
}