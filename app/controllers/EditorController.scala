package controllers

import java.net.URLDecoder
import javax.inject.Inject

import models.viewparam.CodeMirrorModeInfo
import play.api.libs.json.Json
import play.api.{Logger, Environment}
import play.api.mvc.Controller
import services.inspection.ServerEnv
import play.api.mvc._
import scala.reflect.io.Path


/**
  * Created by marco on 2017/1/25.
  */
class EditorController @Inject() (env:Environment) extends Controller{

  val projser = ServerEnv(env)

  def editorView(path:String = null) = {
    val pageInfo = path match {
      case null | "" =>  CodeMirrorModeInfo(Path(""))
      case other => CodeMirrorModeInfo(Path(URLDecoder.decode(path, "UTF-8")))
    }
    val ctt = projser.souceCodeContent(pageInfo.filePath.path)
    Action(Ok(views.html.codereditorfull(pageInfo.mainarg, pageInfo.cmtype, ctt, path, pageInfo.filePath.name)))
  }

  def sourceCodeWithExtension(ext:String) = {
    val mapResult = ext match {
      case null => Map(projser.srcroot.walk.map( p => p.name -> p.path).toList:_*)
      case extension => Map(projser.sourceWithExention(ext).map(p => p.name -> p.path):_*)
    }
    Action(Ok(Json.toJson(mapResult)))
  }

  // TODO: finish it
  def save(path:String) = Action(parse.tolerantText){ implicit req =>
    Logger.debug(s"${req.body}")
    Ok("----")
  }
}
