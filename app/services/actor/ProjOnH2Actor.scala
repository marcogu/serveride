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

  def listCmd(proj:String, container:String):PathList = PathList(proj, SourcePath(container, false, None) )
}


class ProjOnH2Actor(implicit session:QuerySession) extends Actor {
  import ProjOnH2Actor._

  def receive = {
    case All => sender() ! Project.all
    case NewProj(n, p) => forwardProjActor(Named(p))( _ => context.actorOf(DevApp.props(Project.newProj(n, p)), n))
    case pcmd:NamedProject => forwardProjActor(pcmd)(actorFromQuery)

    case DevApp.AppInfo(proj, isRuning, runInfo) => println(s"$proj, $isRuning, $runInfo")
  }

  private def forwardProjActor(cmd:NamedProject)(projActorCreator:(String)=>ActorRef):Unit = {
    context.child(cmd.pname).getOrElse(projActorCreator(cmd.pname)).forward(cmd)
  }

  private def actorFromQuery(pname:String):ActorRef = Project.named(pname) match {
    case null => null
    case proj => context.actorOf(DevApp.props(proj), pname)
  }
}