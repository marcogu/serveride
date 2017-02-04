package controllers

import java.net.URLDecoder
import javax.inject.Inject
import akka.actor.ActorSystem
import models.Project
import models.viewparam.CodeMirrorModeInfo
import play.api.libs.json.Json
import play.api.{Logger, Environment}
import play.api.mvc.Controller
import services.actor.ProjOnH2Actor
import services.actor.ProjOnH2Actor.{NewProj, Files}
import services.inspection.ServerEnv
import play.api.mvc._
import scala.reflect.io.Path
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


/**
  * Created by marco on 2017/1/25.
  */
class EditorController @Inject() (env:Environment, system:ActorSystem) extends Controller{
  val projActor = ProjOnH2Actor(system)
  implicit val timeout: akka.util.Timeout = 5.seconds

  def editorView(path:String = null) = {
    val pageInfo = path match {
      case null | "" =>  CodeMirrorModeInfo(Path(""))
      case other => CodeMirrorModeInfo(Path(URLDecoder.decode(path, "UTF-8")))
    }
    val ctt = ServerEnv.srcContent(pageInfo.filePath.path)
    Action(Ok(views.html.codereditorfull(pageInfo.mainarg, pageInfo.cmtype, ctt, path, pageInfo.filePath.name)))
  }

  def srcFiles(projName:String, ext:String) = Action.async {
    (projActor ? Files(projName, ext)).mapTo[Map[String, String]].map { result => Ok(Json.toJson(result))}
  }

  def registerProj(name:String, url:String) = Action.async { val projLocation = URLDecoder.decode(url, "UTF-8")
    (projActor ? NewProj(name, projLocation)).mapTo[Project].map{ result => Ok("succ")}
  }

  // TODO: finish it
  def save(path:String) = Action(parse.tolerantText){ implicit req =>
    Logger.debug(s"${req.body}")
    Ok("----")
  }
}
