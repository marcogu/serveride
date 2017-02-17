package services.actor

import akka.actor.{Props, Actor}
import models.Project
import scala.sys.process._
import scala.reflect.io.Path
import java.io.{File=>JFile}
import language.postfixOps
import collection.mutable.ArrayBuffer

/**
  * Created by marco on 2017/2/5.
  */
class DevApp(proj:Project) extends Actor{
  import DevApp._

  import services.actor.ProjOnH2Actor._

  val actorName = "monitor"
  private val genAppCmd = Seq[String](script.path, proj.path, proj.pname, template.path)
  private var running = false
  private var isValide = false
  def appArchitectureCheck:Boolean = (Path(proj.path) / proj.pname) exists

  override def preStart(): Unit = {
    isValide = appArchitectureCheck
  }

  def receive = {
    case Named(_) | NewProj(_, _) => check()
    case Run(_, consoleHandler) => running match {
      case true => check()
      case false => running = true; forwardMornitor(Run(DevApp.runscript.path, consoleHandler))
    }

    case cmd:Stop => running match {
      case true => running = false; forwardMornitor(cmd)
      case false => check()
    }

    case Files(f, e) => sender() ! sourceCodes(f, e)
    case Console(name) => running match {
      case true => forwardMornitor(services.actor.RunningStdLog.AllCached)
      case false =>
    }

    case PathList(_, SourcePath(rp, isD, _)) => sender() ! SourcePath(rp, isD, Some(listRoot(rp)))
  }

  def listRoot(spec:String) = (proj.root / spec).jfile.listFiles.map{ f =>
    SourcePath(proj.root.relativize(f.getCanonicalPath).path, f.isFile, None)
  }

  def allstructs(parentFile:JFile, c:SourcePath):Unit = parentFile.listFiles.foreach{ f => 
    val subs:ArrayBuffer[SourcePath] = c.subs.get.asInstanceOf[ArrayBuffer[SourcePath]]
    f.isDirectory match{
      case false => subs += SourcePath(proj.root.relativize(f.getCanonicalPath).path, false, None)
      case true =>
        val node = SourcePath(proj.root.relativize(f.getCanonicalPath).path, true, Some(ArrayBuffer()))
        subs += node
        allstructs(f, node)
    }
  }

  private def check() = isValide match {
    case true => forwardMornitor(GetInfo)
    case false => sender() ! AppInfo(proj, running, Some(RunningInfo("init", "init", genAppCmd !!)))
      isValide = appArchitectureCheck
  }

  private def forwardMornitor(cmd:AnyRef) = isValide match {
    case true => context.child(actorName).getOrElse(context.actorOf(Props(new RMornitor(proj)), actorName)).forward(cmd)
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

  case object GetInfo
  case class RunningInfo(sessionId:String, logId:String, logInfo:String)
  case class AppInfo(proj:Project, runing:Boolean, lastLogger:Option[RunningInfo])
  case class SourcePath(rpath: String, isFile: Boolean, subs: Option[Seq[SourcePath]]){
    def fname = Path(rpath).name
  }
}
