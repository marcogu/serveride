package services.actor

import java.io.InputStream
import java.util.Date

import akka.actor.{ActorRef, PoisonPill, Actor}
import models.Project
import services.actor.DevApp.{RunningInfo, AppInfo}
import services.actor.ProjOnH2Actor.{Run, Stop}
import services.actor.RunningStdLog.AllCached
import scala.reflect.io.Path
import scala.sys.process._

/**
  * Created by marco on 2017/2/6.
  */
class RMornitor(proj:Project) extends Actor{
  private var processing:(Process, Integer) = null
  private var logactorRef:Option[ActorRef] = None

  def receive = {
    case DevApp.GetInfo =>
      sender() ! AppInfo(proj, processing!=null, genLog("state"))

    case Run(strPath, consoleHandler) => sender() ! excSbtrun(scriptToCmd(Path(strPath)), consoleHandler)
      context.actorSelection(self.path.parent.parent) ! AppInfo(proj, processing!=null, genLog("state"))

    case Stop(_) => val stopmsg = stopSbtRun()
      sender() ! stopmsg.getOrElse(RunningInfo("", "", "not running"))
      context.actorSelection(self.path.parent.parent) ! AppInfo(proj, processing!=null, stopmsg)

    case AllCached => logactorRef.fold()(_.forward(AllCached))
  }

  private def scriptToCmd(script:Path):String = s"${script.path} ${proj.root.path}"

  private def genLog(logId:String) = processing match {
    case null => None
    case (process, pid) => Some(RunningInfo(s"${processing._2}", logId, ""))
  }

  private def excSbtrun(cmd:String, ch:ConsoleHandler):RunningInfo = {
    processing = determinProcess(cmd.run(new ProcessIO( o=> Unit, stdin=>consoleP(stdin), ein=>consoleP(ein))))
    stopLogMonitor()
    logactorRef = Some(context.actorOf(RunningStdLog.props(genLogerFile(proj.root, processing._2.toLong), ch), "loger"))
    RunningInfo(s"${processing._2}","run",s"$cmd")
  }

  // There has two method can terminte running process
  // Stop method A, this method can get last output information from excuting process.
  //    A.1 Make a variable to reference ProcessIO output stream
  //    A.2 Close this output stream that when you want terminate the process.
  // stop method B, for make sure the process been kill.
  private def stopSbtRun():Option[RunningInfo] = processing match {
    case null => None
    case (process, pid) => processing = null
      process.destroy()
      stopLogMonitor()
      Some(RunningInfo(pid.toString, "stop", s"process $pid was terminated"))
  }

  // Process Callback thread and Actor thread is probably not the same, So
  // Don't share any class sope data.
  private def consoleP(in:InputStream):Unit = scala.io.Source.fromInputStream(in).getLines.foreach{ line =>
    val lineInfo = RunningStdLog.Line(Some(line), None)
    logactorRef.fold()(_ ! lineInfo )
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

  def genLogerFile(projPath:Path, pid:Long):Path = {
    val logFileName = pid match {
      case -1 => new Date().getTime.toHexString
      case other => s"${other.longValue().toHexString}-" +
        s"${(new Date().getTime / 1000).toHexString}-" +
        s"${(new util.Random).nextInt(255).toHexString}"
    }
    val logContainer = projPath / "jzlog";
    if(!logContainer.exists){ logContainer.createDirectory() }

    val logf = logContainer / logFileName
    if(!logf.exists) logf.createFile()
    logf
  }

  private def stopLogMonitor() =  {
    logactorRef.fold()(_ ! PoisonPill)
    logactorRef = None
  }
}
