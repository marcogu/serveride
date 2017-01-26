package controllers

import java.net.URLDecoder
import javax.inject.Inject

import models.viewparam.MainTempateArguments
import play.api.libs.json.Json
import play.api.Environment
import play.api.mvc.Controller
import services.inspection.ServerEnv
import play.api.mvc._


/**
  * Created by marco on 2017/1/25.
  */
class EditorController @Inject() (env:Environment) extends Controller{

  val projser = ServerEnv(env)

  def editorView = {
    val tempateArg:MainTempateArguments = new MainTempateArguments(title="Oem Pkg Manager",
      cssUrl= Some(Seq("/assets/javascripts/codemirro5-23-0/codemirror.css" ,
                       "/assets/stylesheets/app-style.css")),
      jsUrl = Some(Seq(
//                      "/assets/javascripts/jquery-3.1.1.js",
                      "/assets/javascripts/angular-1.4.4/angular.min.js",
                      "/assets/javascripts/angular-1.4.4/angular-route.min.js",
                      "/assets/javascripts/angular-1.4.4/angular-animate.min.js",
                      "/assets/javascripts/angular-1.4.4/angular-sanitize.min.js",
                      "/assets/javascripts/angular-1.4.4/angular-strap-2.3.7.js",
                      "/assets/javascripts/angular-1.4.4/angular-strap-2.3.7.tpl.min.js",
                      "/assets/javascripts/editor/devapp.js",
                      "/assets/javascripts/codemirro5-23-0/codemirror.js"))
    )
    tempateArg.addMeta(("charset", "UTF-8"))
    Action(Ok(views.html.codereditorfull(tempateArg)))
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
