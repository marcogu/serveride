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
  private var running = false

  def appArchitectureCheck:Boolean = (Path(proj.path) / proj.pname) exists

  def receive = {
    case Info => sender() ! AppInfo(proj, running, check())
    case Gen => sender() ! check()
    case Run =>
    case Stop =>
  }

  def check():Option[RunningInfo] = if(!appArchitectureCheck){
    println("-------"+selfRoot)
    Some(RunningInfo("init", "init", genAppCmd !!))
  } else None
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

  case class RunningInfo(sessionId:String, logId:String, logInfo:String)
  case class AppInfo(proj:Project, runing:Boolean, lastLogger:Option[RunningInfo])


}
