package controllers

import java.net.URLDecoder
import javax.inject.{Singleton, Inject}
import akka.actor.{Props, ActorSystem}
import akka.stream.Materializer
import models.Project
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

  import models.viewparam.EditorTemplateArg._
  def editorView(pname:String) = Action.async {
    (projActor ? Named(pname)).mapTo[AppInfo].map { app => Ok(views.html.codereditorfull(mainarg(pname), pname)) }
  }

  import java.io.PrintWriter;
  def save(proj:String, rpath:String) = Action.async(parse.tolerantText){ implicit req =>
    (projActor ? Named(proj)).mapTo[AppInfo].map { app => 
      new PrintWriter((app.proj.root / rpath).path) { write(req.body); close }
      Ok("file did save")
    }
  }

  def src(pname:String, relativePath:String) = Action.async {
    (projActor ? Named(pname)).mapTo[AppInfo].map{ app => Ok(AppEnv.srcContent((app.proj.root / relativePath).path)) }
  }

  def srcFiles(projName:String, ext:String) = Action.async {
    (projActor ? Files(projName, ext)).mapTo[Map[String, String]].map { result => Ok(Json.toJson(result))}
  }

  def registerProj(name:String, url:String) = Action.async { val projLocation = URLDecoder.decode(url, "UTF-8")
    (projActor ? NewProj(name, projLocation)).mapTo[AppInfo].map{ r => Ok(Json.toJson(r))}
  }

  def projinfo(name:String) = Action.async {
    (projActor ? Named(name)).mapTo[AppInfo].map{ app => Ok(Json.toJson(app))}
  }

  def allproj() = Action.async {
    (projActor ? All).mapTo[Seq[AppInfo]].map{ info => Ok(Json.toJson(info))}
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
    (projActor ? listCmd(proj, relativePath)).mapTo[SourcePath].map { r => Ok(Json.toJson(r)) }
  }

  def listSubView(proj:String, relativePath:String) = Action.async {
    (projActor ? listCmd(proj, relativePath)).mapTo[SourcePath].map { r => Ok(views.html.treeview(r)) }
  }     

  def consoleScreen(runningProjName:String) = Action.async { // test method
    (projActor ? Console(runningProjName)).map{ r=> Ok(r.toString) }
  }

  def tv = Action.async { // test method
    (projActor ? listCmd("autotoolt6", "")).mapTo[SourcePath].map { r=> Ok(views.html.treeview(r)) }
  }

  def subs = Action.async { // test method
    (projActor ? listCmd("autotoolt6", "app")).mapTo[SourcePath].map { r => Ok(views.html.tags.treeitem(r)) }
  }
}
