package controllers

import java.net.URLDecoder
import javax.inject.Inject

import models.viewparam.MainTempateArguments
import play.api.libs.json.Json
import play.api.Environment
import play.api.mvc.Controller
import services.inspection.ServerEnv
import play.api.mvc._
import scala.reflect.io.Path


/**
  * Created by marco on 2017/1/25.
  */
class EditorController @Inject() (env:Environment) extends Controller{

  val projser = ServerEnv(env)

  case class CodeMirrorModeInfo(filePath:Path){
    import collection.mutable.{Seq=>MSeq}
    private val cssurls:MSeq[String] = MSeq("/assets/javascripts/codemirro5-23-0/codemirror.css",
      "/assets/stylesheets/app-style.css")
    private var jsurls = MSeq(
      "/assets/javascripts/angular-1.4.4/angular.min.js",
      "/assets/javascripts/angular-1.4.4/angular-route.min.js",
      "/assets/javascripts/angular-1.4.4/angular-animate.min.js",
      "/assets/javascripts/angular-1.4.4/angular-sanitize.min.js",
      "/assets/javascripts/angular-1.4.4/angular-strap-2.3.7.js",
      "/assets/javascripts/angular-1.4.4/angular-strap-2.3.7.tpl.min.js",
      "/assets/javascripts/editor/devapp.js",
      "/assets/javascripts/codemirro5-23-0/codemirror.js"
    )

    private val jsUrlFormat = "/assets/javascripts/codemirro5-23-0/mode/%s/%s.js"
    private val lowExtension = filePath.extension.toLowerCase
    val cmtype = lowExtension match {
      case "scala" | "sbt" => jsurls ++= Seq(jsUrlFormat.format("clike", "clike"))
        "text/x-scala"
      case "java" => jsurls ++= Seq(jsUrlFormat.format("clike", "clike"))
        "text/x-java"
      case "html" => jsurls ++= Seq(jsUrlFormat.format("htmlmixed", "htmlmixed"))
        jsurls ++= Seq(jsUrlFormat.format("htmlembedded", "htmlembedded"))
        "application/x-ejs"
      case "js" => jsurls ++= Seq(jsUrlFormat.format("javascript", "javascript"))
        "text/javascript"
      case "css" | "sass" | "less" => jsurls ++= Seq(jsUrlFormat.format("css", "css"))
        lowExtension.equals("css") match {
          case true => "text/css"
          case false => s"text/x-$lowExtension"
        }
      case "properties" => jsurls ++= Seq(jsUrlFormat.format("properties", "properties"))
        "text/x-properties"
      case other => "text"
    }

    def mainarg:MainTempateArguments = {
      val varg = new MainTempateArguments(title=filePath.name, cssUrl = Some(cssurls), jsUrl = Some(jsurls))
      varg.addMeta("charset"->"UTF-8").asInstanceOf[MainTempateArguments]
    }

    lazy val souceCodeContent = projser.souceCodeContent(filePath.path)
  }

  def editorView(path:String = null) = {
    val pageInfo = path match {
      case null | "" =>  CodeMirrorModeInfo(Path(""))
      case other => CodeMirrorModeInfo(Path(URLDecoder.decode(path, "UTF-8")))
    }  
    Action(Ok(views.html.codereditorfull(pageInfo.mainarg, pageInfo.cmtype, souceCodeContent, path)))
  }

  def laodSourceCode(urlEncodedPath:String) =
    Action(Ok(projser.souceCodeContent(URLDecoder.decode(urlEncodedPath, "UTF-8"))))

  def sourceCodeWithExtension(ext:String) = {
    val mapResult = ext match {
      case null => Map(projser.srcroot.walk.map( p => p.name -> p.path).toList:_*)
      case extension => Map(projser.sourceWithExention(ext).map(p => p.name -> p.path):_*)
    }
    Action(Ok(Json.toJson(mapResult)))
  }
}
