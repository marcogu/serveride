package services.inspection


import play.api.{Logger, Environment}
import scala.reflect.io.Path

/**
  * Created by marco on 2017/1/24.
  *
  */
class ServerEnv(rootPath:Path){
  val srcroot = rootPath / "app"

  def souceCodeContent(path:String) = {
    val source = scala.io.Source.fromFile(path)
    try source.mkString finally source.close()
  }

  def rootSubnames = srcroot.toFile.jfile.listFiles().map( f => f.getName).toSeq
  def sourceWithExention(extension:String) = srcroot.walk.filter( p => p.extension.equals(extension) && p.isFile).toList
}


object ServerEnv {
  def apply(env:Environment):ServerEnv = new ServerEnv(env.rootPath.getAbsolutePath)
}
