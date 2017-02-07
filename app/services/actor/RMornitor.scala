package services.actor

import java.io.InputStream

import akka.actor.Actor
import models.Project
import scala.reflect.io.Path
import scala.sys.process._

/**
  * Created by marco on 2017/2/6.
  */
class RMornitor(path:String) extends Actor{
  private val p = Path(path)
  private var processing:(Process, Long) = null
  var counter = 0

  def receive = {
    case DevApp.Info => counter += 1; println(counter)
      sender() ! DevApp.AppInfo(Project(p.name, p.parent.path), processing!=null, genLog("state"))
    case DevApp.Run(scriptp) => sender() ! excSbtrun(scriptToCmd(scriptp))
    case DevApp.Stop => sender() ! stopSbtRun()
  }

  private def scriptToCmd(script:Path):String = s"${script.path} $path"

  private def genLog(logId:String) = processing match {
    case null => None
    case (process, pid) => Some(DevApp.RunningInfo(s"${processing._2}", logId, ""))
  }

  private def excSbtrun(cmd:String):DevApp.RunningInfo = {
    processing = determinProcess(cmd.run(new ProcessIO( o=> op=o, stdin=>processorPrinter(stdin),
      errin=>processorPrinter(errin))))
    DevApp.RunningInfo(s"${processing._2}","run",s"$cmd")
  }

  // stop method 1, this method can get last output information from excuting process.
  //    op.close()
  // stop method 2, for make sure the process been kill.
  private def stopSbtRun() = if( processing != null) {
    processing._1.destroy()
    processing = null
    DevApp.RunningInfo("test","test",s"stop")
  }

  private var op:java.io.OutputStream = null
  private def processorPrinter(in:InputStream):Unit = scala.io.Source.fromInputStream(in).getLines.foreach{ line =>
    println(line)
  }

  def determinProcess(p:Process):(Process, Long) = {
    try{
      val field = p.getClass.getDeclaredField("pid")
      field.setAccessible(true)
      val pid = field.getLong(p)
      field.setAccessible(false)
      (p, pid)
    }catch {
      case e:Exception => (p, -1)
    }
  }
}
