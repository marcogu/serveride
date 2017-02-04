package services.actor

import akka.actor.Actor
import models.Project

/**
  * Created by marco on 2017/2/4.
  */
class GenProjActor extends Actor {
  import GenProjActor._

  def receive = {
    case Generate(Project(n, p)) =>
  }
}

object GenProjActor{
  case class Generate(proj:Project)
  case class Remove(proj:Project)
}
