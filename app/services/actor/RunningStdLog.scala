package services.actor

import java.io.PrintWriter

import akka.actor.{Props, Actor}
import collection.mutable.{Map=>MMap}
import scala.reflect.io.Path

/**
  * Created by marco on 2017/2/8.
  */
class RunningStdLog(logName:Path, consoleHandler:ConsoleHandler) extends Actor{
  val writer = new PrintWriter(logName.path, RunningStdLog.logFileEncode)
  var lnum = 0

  val logCacher = MMap[Int, String]()
  import RunningStdLog._
  val excDispatch:(Line)=>Unit = consoleHandler match {
    case null => line => null
    case _ => consoleHandler.setLogFile(logName.name)
      consoleHandler.consoleRecieve
  }

  def receive = {
    case Line(Some(lcontent), None) => lnum += 1
      writer.println(lcontent)
      writer.flush()
      excDispatch(Line(Some(fixIfLastCharIsNotNewLineCharater(lcontent)), Some(lnum)))

    case Line(None, Some(idx)) => sender() ! numberLineFormCache(fitLen(idx))
    case MaxLineNum => sender() ! LRange(0, lnum)
    case LRange(s, e) => sender() ! Lines(s to fitLen(e) map { numberLineFormCache }, LRange(s, e) )
    case AllCached => sender() ! logCacher
  }

  def fixIfLastCharIsNotNewLineCharater(lineString:String):String = if(lineString == null || lineString.isEmpty)
    lineString else lineString.last match {
    case '\n' | '\r' => lineString
    case other => s"$lineString\r\n"
  }

  def numberLineFormCache(n:Int):Line = logCacher.get(n) match {
    case Some(lineContext) => Line(Some(lineContext), Some(n))
    case _ => Line(None, Some(n))
  }

  def fitLen(end:Int):Int = if (end >= lnum) lnum else end

  override def postStop():Unit = {
    writer.flush()
    writer.close()
    lnum = -1
  }
}


object RunningStdLog{
  val logFileEncode = "UTF-8"

  case object MaxLineNum
  case object AllCached
  case class Line(content:Option[String], lineNum:Option[Int])
  case class LRange(startNo:Int = 1, endNo:Int)
  case class Lines(ls:Seq[Line], r:LRange)

  def props(logfile:Path, consoleHandler:ConsoleHandler) = Props(new RunningStdLog(logfile, consoleHandler))

  import scala.io.Source
  // Util method
  def getTotalLineCount(filePath:Path) = {
    val source = Source.fromFile(filePath.path, logFileEncode)
    val count = source.getLines().size
    source.close()
    count
  }

  def loadLinesToCaches(filePath:Path, range:Range, cacheMap:MMap[Int, String] = MMap()):MMap[Int, String] = {
    val source = Source.fromFile(filePath.path, logFileEncode)
    var counter = 0
    val it = source.getLines()
    while(counter < range.head) { it.next(); counter+=1}
    range.foreach { lno => cacheMap.put(lno, it.next())}
    source.close()
    cacheMap
  }

}
