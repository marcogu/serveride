package services.inspection


import play.api.Environment
import scala.io.BufferedSource
import scala.reflect.io.Path

/**
  * Created by marco on 2017/1/24.
  *
  */
class ServerEnv(rootPath:Path){
  val srcroot = rootPath / "app"

  def souceCodeContent(path:String):String = {
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

  def rootSubnames = srcroot.toFile.jfile.listFiles().map( f => f.getName).toSeq
  def sourceWithExention(extension:String) = srcroot.walk.filter( p => p.extension.equals(extension) && p.isFile).toList
}


object ServerEnv {
  def apply(env:Environment):ServerEnv = new ServerEnv(env.rootPath.getAbsolutePath)
}
