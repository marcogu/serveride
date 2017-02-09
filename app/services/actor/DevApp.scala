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

  val actorName = "monitor"
  private val genAppCmd = Seq[String](script.path, proj.path, proj.pname, template.path)
  private var running = false
  private var isValide = false
  def appArchitectureCheck:Boolean = (Path(proj.path) / proj.pname) exists

  override def preStart(): Unit = {
    isValide = appArchitectureCheck
  }

  def receive = {
    case Info => check()

    case Run => running match {
      case true => check()
      case false => running = true; forwardMornitor(DevApp.Run(DevApp.runscript))
    }

    case Stop => running match {
      case true => running = false; forwardMornitor(Stop)
      case false => check()
    }

    case Files(f, e) => sender() ! sourceCodes(f, e)
    case services.actor.ProjOnH2Actor.Console(name) => running match {
      case true => forwardMornitor(services.actor.RunningStdLog.AllCached)
      case false =>
    }
  }

  private def check() = isValide match {
    case true => forwardMornitor(Info)
    case false => sender() ! AppInfo(proj, running, Some(RunningInfo("init", "init", genAppCmd !!)))
  }

  private def forwardMornitor(cmd:AnyRef) = isValide match {
    case true =>
      context.child(actorName).fold(context.actorOf(Props(new RMornitor(proj)), actorName).forward(cmd))(_.forward(cmd))
    case false => sender() ! "application is not be create"
  }

  /** Get Play application source codes where under project/app folder
    *
    * @param filter The name like parterm
    * @param ext Search limited with file extension, ex: scala, java, js, html, less, cass, css ...
    * @return Collect all source codes in Map data struct.
    * */
  private def sourceCodes(filter:String, ext:String):Map[String, String] = {
    val srcs = proj.root / "app"
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
  val runscript = selfRoot / "public/apprunscript/sbtrunplay.sh"

  case object Info
  case object Stop
  case object Run

  case class Run(runScript: Path)
  case class Files(filter:String, extentions:String)
  case class RunningInfo(sessionId:String, logId:String, logInfo:String)
  case class AppInfo(proj:Project, runing:Boolean, lastLogger:Option[RunningInfo])
}
