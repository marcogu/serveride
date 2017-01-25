package services.inspection


import play.api.{Logger, Environment}
import scala.reflect.io.Path

/**
  * Created by marco on 2017/1/24.
  */

class ServerEnv(env:Environment){
  val srcroot = Path(env.rootPath.getAbsolutePath) / "app"
  def rootSubnames = srcroot.toFile.jfile.listFiles().map( f => f.getName).toSeq

  def souceCodeContent(path:String) = {
    val source = scala.io.Source.fromFile(path)
    val lines = try source.mkString finally source.close()
    lines
  }

  def defaultTest():String = souceCodeContent((srcroot / "controllers" / "Application.scala").path)
}

object ServerEnv {

  var senv:Option[ServerEnv] = None

  def apply(env:Environment):ServerEnv = senv match {
    case Some(ins) => ins
    case None => senv = Some(new ServerEnv(env)); senv.get
  }
}
