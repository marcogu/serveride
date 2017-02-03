package controllers

import java.net.URLDecoder
import javax.inject.Inject

import models.viewparam.CodeMirrorModeInfo
import play.api.libs.json.Json
import play.api.{Logger, Environment}
import play.api.mvc.Controller
import services.inspection.ServerEnv
import services.Project
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
    val ctt = ServerEnv.srcContent(pageInfo.filePath.path)
    Action(Ok(views.html.codereditorfull(pageInfo.mainarg, pageInfo.cmtype, ctt, path, pageInfo.filePath.name)))
  }

  def projectFromName(n:String):Project = n match {
    case null | "self" | "SELF" => Project.named(Project.selfName)
    case other => Project.named(n)
  }

  def srcFiles(projName:String, ext:String) = Action {
    val cp = ServerEnv(projectFromName(projName).path)
    val r = ext match {
      case null => Map(cp.srcroot.walk.map( p => p.name -> p.path).toList:_*)
      case extension => Map(cp.sourceWithExention(ext).map(p => p.name -> p.path):_*)
    }
    Ok(Json.toJson(r))
  }

  def registerProj(name:String, url:String) = Action {
    val projLocation = URLDecoder.decode(url, "UTF-8")
    val newproj = Project.newProj(name, url)
    Ok("succ")
  }

  // TODO: finish it
  def save(path:String) = Action(parse.tolerantText){ implicit req =>
    Logger.debug(s"${req.body}")
    Ok("----")
  }
}
