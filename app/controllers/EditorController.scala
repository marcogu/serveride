package controllers

import java.net.URLDecoder
import javax.inject.Inject
import akka.actor.ActorSystem
import models.Project
import models.viewparam.CodeMirrorModeInfo
import play.api.libs.json.Json
import play.api.{Logger, Environment}
import play.api.mvc.Controller
import services.actor.DevApp.{RunningInfo, AppInfo}
import services.actor.ProjOnH2Actor
import services.actor.ProjOnH2Actor._
import services.inspection.AppEnv
import play.api.mvc._
import scala.concurrent.Future
import scala.reflect.io.Path
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


/**
  * Created by marco on 2017/1/25.
  */
class EditorController @Inject() (env:Environment, system:ActorSystem) extends Controller{
  implicit val timeout: akka.util.Timeout = 5.seconds
  implicit val jsProj = Json.format[Project]
  implicit val jsRunInfo = Json.format[RunningInfo]
  implicit val jsAppInfo = Json.format[AppInfo]

  val projActor = ProjOnH2Actor(system)

  def editorView(path:String = null) = {
    val pageInfo = path match {
      case null | "" =>  CodeMirrorModeInfo(Path(""))
      case other => CodeMirrorModeInfo(Path(URLDecoder.decode(path, "UTF-8")))
    }
    val ctt = AppEnv.srcContent(pageInfo.filePath.path)
    Action(Ok(views.html.codereditorfull(pageInfo.mainarg, pageInfo.cmtype, ctt, path, pageInfo.filePath.name)))
  }

  def srcFiles(projName:String, ext:String) = Action.async {
    (projActor ? Files(projName, ext)).mapTo[Map[String, String]].map { result => Ok(Json.toJson(result))}
  }

  def registerProj(name:String, url:String) = Action.async { val projLocation = URLDecoder.decode(url, "UTF-8")
    (projActor ? NewProj(name, projLocation)).mapTo[AppInfo].map{ r => Ok(Json.toJson(r))}
  }

  def projinfo(name:String) = Action.async {
    (projActor ? Named(name)).mapTo[AppInfo].map{ info => Ok(Json.toJson(info))}
  }

  def runapp(name:String) = Action.async { appCmdHelper(Run(name))}
  def stopapp(name:String) = Action.async { appCmdHelper(Stop(name))}

  def appCmdHelper(cmd:AnyRef):Future[Result] = (projActor ? cmd).collect{
    case result:RunningInfo => Ok(Json.toJson(result))
    case areadyRun:AppInfo =>  Ok(Json.toJson(areadyRun))
  }

  def consoleScreen(runningProjName:String) = Action.async {
    (projActor ? Console(runningProjName)).map{ r=> Ok(r.toString)}
  }

  // TODO: finish it
  def save(path:String) = Action(parse.tolerantText){ implicit req =>
    Logger.debug(s"${req.body}")
    Ok("----")
  }
}
