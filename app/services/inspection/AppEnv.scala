package services.inspection


import play.api.Environment
import scala.io.BufferedSource
import scala.reflect.io.Path

/**
  * Created by marco on 2017/1/24.
  *
  */
class AppEnv(rootPath:Path){
  val srcroot = rootPath / "app"
  def rootSubnames = srcroot.toFile.jfile.listFiles().map( f => f.getName).toSeq
  def sourceWithExention(extension:String) =
    srcroot.walk.filter( p => p.extension.equals(extension) && p.isFile).toList
}


object AppEnv {
  def apply(env:Environment):AppEnv = new AppEnv(env.rootPath.getAbsolutePath)
  def apply(pPath:String):AppEnv = new AppEnv(Path(pPath))

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


}
