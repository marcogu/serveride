package services

import play.api.libs.json.Json

/**
  * Created by marco on 2017/2/14.
  */
package object actor {

  import services.actor.RunningStdLog.Line


  trait ConsoleHandler{
    def consoleRecieve(line:Line):Unit
    def setLogFile(path:String):Unit
    def logFile:String
  }


  class CDispatcher(room:WebsocketRoom) extends ConsoleHandler{
    private var logPath:Option[String] = None

    def setLogFile(path:String) = if(logPath.isEmpty) logPath = Some(path)
    def logFile = logPath.get

    def consoleRecieve(line:Line):Unit = {
      import models.reqarg.JSFormatImplicit._
      room.publishMessage( Json.toJson(line) )
    }
  }


  object ConsoleDispatcher{
    def apply(room:WebsocketRoom):ConsoleHandler = new CDispatcher(room)
  }
}
