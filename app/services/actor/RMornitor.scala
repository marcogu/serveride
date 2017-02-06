package services.actor

import akka.actor.Actor

/**
  * Created by marco on 2017/2/6.
  */
class RMornitor(path:String) extends Actor{
  def receive = {
    case DevApp.Run => sender() ! DevApp.RunningInfo("test","test","run")
    case DevApp.Stop => sender() ! DevApp.RunningInfo("test","test","stop")
  }
}
