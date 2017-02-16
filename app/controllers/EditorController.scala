package controllers

import java.net.URLDecoder
import javax.inject.{Singleton, Inject}
import akka.actor.{Props, ActorSystem}
import akka.stream.Materializer
import models.Project
import models.viewparam.CodeMirrorModeInfo
import play.api.libs.json.{JsValue, Json}
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc.Controller
import services.actor.DevApp.{SourcePath, RunningInfo, AppInfo}
import services.actor.{RMMember, WSRoom, ProjOnH2Actor}
import services.actor.ProjOnH2Actor._
import services.inspection.AppEnv
import services.actor.ConsoleDispatcher
import play.api.mvc._
import scala.concurrent.Future
import scala.reflect.io.Path
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import models.reqarg.JSFormatImplicit._


/**
  * Created by marco on 2017/1/25.
  */
@Singleton
class EditorController @Inject() (implicit system:ActorSystem, materializer: Materializer) extends Controller{

  implicit val timeout: akka.util.Timeout = 5.seconds
  val projActor = ProjOnH2Actor(system, Project.DDL().genQuerySession)
  val websocketDefaultRoom = WSRoom(system)

  def subscriptConsole(proj:String) = WebSocket.acceptOrResult[String, String] { req =>
    Future.successful(websocketDefaultRoom.subRoom(proj) match {
      case None => Left(NotFound)
      case Some(room) => Right(ActorFlow.actorRef( out => room.inRoom(out)) )
    })
  }

  def editorView(path:String = null) = {
    println(s"get load request with path $path")
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

  def runapp(name:String) = Action.async { appCmdHelper(Run(name, ConsoleDispatcher(name))){ isRepeate =>
    if(!isRepeate) {
      websocketDefaultRoom.createSubRoom(name)
    }
  }}

  def stopapp(name:String) = Action.async { appCmdHelper(Stop(name)){ isRepeate =>
    if(!isRepeate){
      println(s"will stop room $name")
      websocketDefaultRoom.removeSubroom(name)
    } 
  }}

  def appCmdHelper(cmd:AnyRef)(preHandler:(Boolean)=>Unit):Future[Result] = (projActor ? cmd).collect{
    case result:RunningInfo => preHandler(false)
      Ok(Json.toJson(result))
    case areadyRun:AppInfo => preHandler(true)
      Ok(Json.toJson(areadyRun))
  }

  def listSub(proj:String, relativePath:String) = Action.async{
    (projActor ? listCmd(proj, relativePath)).mapTo[SourcePath].map { r => Ok(Json.toJson(r)) } //
  }

  def consoleScreen(runningProjName:String) = Action.async { // test method
    (projActor ? Console(runningProjName)).map{ r=>
//      websocketDefaultRoom.subRoom(runningProjName)
      Ok(r.toString)
    }
  }

  // TODO: finish it
  def save(path:String) = Action(parse.tolerantText){ implicit req =>
    Logger.debug(s"${req.body}")
    Ok("----")
  }
}
