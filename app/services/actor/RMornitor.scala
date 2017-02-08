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
  private var processing:(Process, Integer) = null
  var counter = 0

  def receive = {
    case DevApp.Info =>
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
    processing = determinProcess(cmd.run(new ProcessIO( o=> Unit, stdin=>consoleP(stdin), ein=>consoleP(ein))))
    DevApp.RunningInfo(s"${processing._2}","run",s"$cmd")
  }

  // There has two method can terminte running process
  // Stop method A, this method can get last output information from excuting process.
  //    A.1 Make a variable to reference ProcessIO output stream
  //    A.2 Close this output stream that when you want terminate the process.
  // stop method B, for make sure the process been kill.
  private def stopSbtRun() = if( processing != null) {
    val stopInfo = s"process ${processing._2} was terminated"
    processing._1.destroy()
    processing = null
    DevApp.RunningInfo("test","test", stopInfo)
  }

  private def consoleP(in:InputStream):Unit = scala.io.Source.fromInputStream(in).getLines.foreach{ line =>
    println(line)
  }

  private def determinProcess(p:Process):(Process, Integer) = try{ (p, tryGetPid(p)) } catch {
    case e:Exception => println(s"[debug] determin process id error:$e"); e.printStackTrace(); (p, -1)
  }

  private def tryGetPid(process:Process):Integer = {
    val acc:(String, AnyRef) => AnyRef = (fn, obj) =>
    {
      val f = obj.getClass.getDeclaredField(fn)
      val originAccessible = f.isAccessible
      f.setAccessible(true)
      val r = f.get(obj)
      f.setAccessible(originAccessible)
      r
    }

    val maybeJProcess = acc("p", process)
    maybeJProcess.getClass.getName match {
      case "java.lang.UNIXProcess" => acc("pid", maybeJProcess).asInstanceOf[Integer]
      case other => -1
    }
  }
}
