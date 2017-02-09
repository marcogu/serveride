package services.actor

import akka.actor.Actor

/**
  * Created by marco on 2017/2/8.
  */
class RunningStdLog extends Actor{
  var lnum = 0

  def receive = {
    null
  }


}


object RunningStdLog{
  case class Line(content:String, lineNum:Option[Int])
}
