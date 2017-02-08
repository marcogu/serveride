package services.inspection


import java.lang.management.ManagementFactory
import scala.io.BufferedSource

/**
  * Created by marco on 2017/1/24.
  *
  */
object AppEnv {

  def srcContent(path:String):String = {
    var source:BufferedSource = null
    try{
      source = scala.io.Source.fromFile(path)
      source.mkString
    }  catch {
      case e:Exception => ""
    } finally {
      if (source != null) try source.close()
    }
  }

  lazy val rtbean = ManagementFactory.getRuntimeMXBean
  lazy val hostInfo:collection.Map[String, String] = hostSys()
  private val pidParttern = """^([0-9]+)@.+$""".r
  private def hostSys() = scala.collection.JavaConversions.mapAsScalaMap(rtbean.getSystemProperties)

  lazy val processId = selfPid()
  private def selfPid():String = pidParttern.findAllMatchIn(rtbean.getName).collectFirst[String]{
    case m => m.group(1)
  }.getOrElse("")
}

