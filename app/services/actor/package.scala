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


  class CDispatcher(roomName:String) extends ConsoleHandler{
    private var logPath:Option[String] = None

    def setLogFile(path:String) = if(logPath.isEmpty) logPath = Some(path)
    def logFile = logPath.get

    import models.reqarg.JSFormatImplicit._
    def consoleRecieve(line:Line):Unit = WSRoom.default.subRoom(roomName) match {
      case Some(topic) => topic.publishMessage( line.content.get )
        // topic.publishMessage( Json.toJson(line) )
      case None => 
    }
  }


  object ConsoleDispatcher{
    def apply(projName:String):ConsoleHandler = new CDispatcher(projName)
  }
}
