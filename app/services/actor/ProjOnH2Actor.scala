package services.actor

import akka.actor._
import models.Project
import models.Project.H2Checker
import services.inspection.AppEnv


object ProjOnH2Actor {
  def props = Props[ProjOnH2Actor]
  val h2actName = "ProjOnH2Actor"

  private var inited:ActorRef = null
  private var initedSysName:String = ""

  // ! this is not a thread safe function.
  def apply(system: ActorSystem) = {
    val sname = system.name
    initedSysName match {
      case "" => initedSysName = system.name
        inited = system.actorOf(props, h2actName)
        inited
      case `sname` => inited
      case other => throw new RuntimeException(s"ProjOnH2Actor has been initialed on akka system:$initedSysName")
    }
  }

  case class Named(specName:String)
  case object All
  case class NewProj(pname:String, path:String)
  case class Files(pname:String, extension:String)
  case class Run(spcname:String)
  case class Stop(spcname:String)
}

class ProjOnH2Actor extends Actor {
  import ProjOnH2Actor._
  implicit val session = H2Checker().genQuerySession

  def receive = {
    case Named(n) => context.child(n).fold( actorFromQuery(n).forward(DevApp.Info) )(_.forward(DevApp.Info))
    case All => sender() ! Project.all
    case NewProj(n, p) => context.child(n).fold(newProject(n, p))(_.forward(DevApp.Info))
    case Files(n, ext) => val cmd = DevApp.Files("", ext)
      context.child(n).fold(actorFromQuery(n).forward(cmd))(_.forward(cmd))
    case Run(n) => context.child(n).fold( actorFromQuery(n).forward(DevApp.Run) )(_.forward(DevApp.Run))
    case Stop(n) => context.child(n).fold( actorFromQuery(n).forward(DevApp.Stop) )(_.forward(DevApp.Stop))
  }

  private def actorFromQuery(pname:String):ActorRef = Project.named(pname) match {
    case null => null
    case proj => context.actorOf(DevApp.props(proj), pname)
  }

  private def newProject(name:String, path:String):Unit = Project.named(name) match {
    case null => context.actorOf(DevApp.props(Project.newProj(name, path)), name).forward(DevApp.Gen)
    case other => context.actorOf(DevApp.props(other), name).forward(DevApp.Gen)
  }
}