package services.actor

import akka.actor._
import models.Project
import models.Project.H2Checker
import services.inspection.ServerEnv


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
  case class All()
  case class NewProj(pname:String, path:String)
  case class Files(pname:String, extension:String)
}

class ProjOnH2Actor extends Actor {
  import ProjOnH2Actor._

  implicit val session = H2Checker().genQuerySession

  def receive = {
    case Named(n) => sender() ! Project.named(filterName(n))
    case All() => sender() ! Project.all
    case NewProj(n, p) => sender() ! Project.newProj(n, p)
    case Files(n, ext) => sender() ! scodeFilter(ext, ServerEnv(Project.named(filterName(n)).path))
  }

  def filterName(name:String):String = name match {
    case null | "self" | "SELF" => Project.selfName
    case other => other
  }

  def scodeFilter(extension:String, cp:ServerEnv):Map[String, String] = extension match {
    case null | "" => Map(cp.srcroot.walk.map( p => p.name -> p.path).toList:_*)
    case other => Map(cp.sourceWithExention(other).map(p => p.name -> p.path):_*)
  }
}