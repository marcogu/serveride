package services.actor

import akka.actor.{Props, Actor}
import models.Project

import scala.reflect.io.Path
import language.postfixOps


/**
  * Created by marco on 2017/2/5.
  */
class DevApp(proj:Project) extends Actor{
  import DevApp._
  import scala.sys.process._

  private val genAppCmd = Seq[String](script.path, proj.path, proj.pname, template.path)
  private val appRoot = Path(proj.path) / proj.pname

  private var running = false
  private var isValide = false
  def appArchitectureCheck:Boolean = (Path(proj.path) / proj.pname) exists

  def receive = {
    case Info => sender() ! AppInfo(proj, running, check())
    case Gen => sender() ! AppInfo(proj,running, check())
    case Run => forwardMornitor(Run)
    case Stop => forwardMornitor(Stop)
    case Files(f, e) => sender() ! sourceCodes(f, e)
  }

  private def check():Option[RunningInfo] ={
    isValide = appArchitectureCheck
    if(!isValide){ Some(RunningInfo("init", "init", genAppCmd !!))} else None
  }

  private def forwardMornitor(cmd:AnyRef) = isValide match {
    case true =>
      context.child("monitor").fold(context.actorOf(Props( new RMornitor(appRoot.path) )).forward(cmd))(_.forward(cmd))
    case false => sender() ! "application is not be create"
  }

  private def sourceCodes(filter:String, ext:String):Map[String, String] = {
    val srcs = appRoot / "app"
    def pMapTo(p:Path) = p.name -> p.path
    ext match {
      case null | ""  => Map(srcs.walk.map( pMapTo).toList:_*)
      case other => Map(srcs.walk.filter( p => p.extension.equals(other) && p.isFile).map(pMapTo).toList:_*)
    }
  }
}


object DevApp{
  def props(proj:Project) = Props(new DevApp(proj))

  import play.api.Environment
  val selfRoot = Path(Environment.simple().rootPath.getCanonicalPath)
  val (script, template) = (selfRoot / "public/playprojcr/playg.sh", selfRoot / "public/playprojcr/tpfolder")

  case object Info
  case object Gen
  case object Run
  case object Stop

  case class Files(filter:String, extentions:String)
  case class RunningInfo(sessionId:String, logId:String, logInfo:String)
  case class AppInfo(proj:Project, runing:Boolean, lastLogger:Option[RunningInfo])
}
