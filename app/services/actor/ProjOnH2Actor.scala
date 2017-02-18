package services.actor

import akka.actor._
import models.Project
import services.mpos.QuerySession


object ProjOnH2Actor {
  val h2actName = "ProjOnH2Actor"

  private var inited:ActorRef = null
  private var initedSysName:String = ""
  // ! this is not a thread safe function.
  def apply(implicit system: ActorSystem, s:QuerySession) = {
    val sname = system.name
    initedSysName match {
      case "" => initedSysName = system.name
        inited = system.actorOf(Props(new ProjOnH2Actor), h2actName)
        inited
      case `sname` => inited
      case other => throw new RuntimeException(s"ProjOnH2Actor has been initialed on akka system:$initedSysName")
    }
  }

  trait NamedProject{
    def pname:String
  }


  import services.actor.DevApp.SourcePath
  case object All

  case class Named(pname:String) extends NamedProject
  case class NewProj(pname:String, path:String) extends NamedProject
  case class Files(pname:String, extension:String) extends NamedProject
  case class Run(pname:String, consoleDispatcher:ConsoleHandler) extends NamedProject
  case class Stop(pname:String) extends NamedProject
  case class Console(pname:String) extends NamedProject
  case class PathList(pname:String, specPath:SourcePath) extends NamedProject
  case class AddFile(pname:String, relativePath:String, isFolder:Boolean) extends NamedProject
  case class DelFile(pname:String, relativePath:String) extends NamedProject

  case class OperationResponse(actorMsg:AnyRef, succ:Boolean, msg:String)

  def listCmd(proj:String, container:String):PathList = PathList(proj, SourcePath(container, isFile=false, None) )
}


class ProjOnH2Actor(implicit session:QuerySession) extends Actor {
  import ProjOnH2Actor._
  import DevApp.{AppInfo, RunningInfo}
  import collection.mutable.{Map=>Mmap}

  private val runingActor:Mmap[String, Int] = Mmap()

  def receive = {
    case All => sender() ! allProj()
    case NewProj(n, p) => forwardProjActor(Named(p))( _ => context.actorOf(DevApp.props(Project.newProj(n, p)), n))
    // this message from RMornitor actor, when the project run or stop.
    case AppInfo(proj, isRuning, runInfo) => isRuning match {
      case false => runingActor.remove(proj.pname)
      case true => val pid = runInfo.fold(-1)(rinfo => rinfo.sessionId.toInt)
        runingActor.put(proj.pname, pid)
    }
    case pcmd:NamedProject => forwardProjActor(pcmd)(actorFromQuery)
  }

  private def allProj():Seq[AppInfo] = Project.all.map { proj =>
    runingActor.get(proj.pname) match {
      case None => AppInfo(proj, runing = false, None)
      case Some(pid) => AppInfo(proj, runing = true, Some(RunningInfo(pid.toString, "", "" )))
    }
  }

  private def forwardProjActor(cmd:NamedProject)(projActorCreator:(String)=>ActorRef):Unit = {
    context.child(cmd.pname).getOrElse(projActorCreator(cmd.pname)).forward(cmd)
  }

  private def actorFromQuery(pname:String):ActorRef = Project.named(pname) match {
    case null => null
    case proj => context.actorOf(DevApp.props(proj), pname)
  }
}