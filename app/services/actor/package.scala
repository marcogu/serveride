package services

import models.Project

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

  class ProjConsoleHandler(room:WebsocketRoom) extends ConsoleHandler{
    private var logPath:Option[String] = None

    def setLogFile(path:String) = if(logPath.isEmpty) logPath = Some(path)
    def logFile = logPath.get

    def consoleRecieve(line:Line):Unit = {
//      room.publishMessage(line)
    }
  }
}
