package models.viewparam

/**
  * Created by marco on 16/2/5.
  */

import language.postfixOps
import scala.collection.mutable._

trait HtmlMetaArgs {
  val metas = ArrayBuffer.empty[scala.collection.mutable.Map[String, String]]

  def addMeta(property:(String, String)*):HtmlMetaArgs  = {
    var meta = scala.collection.mutable.Map.empty[String, String]
    for( p:(String, String) <- property) { meta += p }
    metas += meta
    this
  }

  def toXmlPropertyString(properties:scala.collection.Map[String, String]):String = {
    val strBuf:StringBuffer = new StringBuffer()
    for(kv:(String, String) <- properties){
      strBuf.append(s"""${kv._1}="${kv._2}" """)
    }
    strBuf.toString
  }

  var baseHref:Option[String] = None
}

case class MainTempateArguments(title:String,
                           cssUrl:Option[scala.collection.Seq[String]],
                           var jsUrl:Option[scala.collection.Seq[String]]) extends HtmlMetaArgs
{
  val jsMap = if (jsUrl.nonEmpty) jsUrl.get map { s => (s.split("/").last, s) } toMap else collection.mutable.Map.empty[String, String]
//  println(jsMap)  // for debug
  //TODO: client can add delete or exchange java script url
}

object MainTempateArguments {
  def apply():MainTempateArguments = {

    val defaultArg = new MainTempateArguments(title="Oem Pkg Manager", cssUrl=
      Some(scala.collection.Seq("/assets/stylesheets/app-style.css", "/assets/stylesheets/angular-motion-0.4.4.css")
      ),
      jsUrl =
      Some(scala.collection.Seq(
        "/assets/javascripts/angular-1.4.4/angular.min.js",
        "/assets/javascripts/angular-1.4.4/angular-route.min.js",
        "/assets/javascripts/angular-1.4.4/angular-animate.min.js",
        "/assets/javascripts/angular-1.4.4/angular-sanitize.min.js",
      // the angular js module :
        "/assets/javascripts/angular-1.4.4/angular-strap-2.3.7.js",
        "/assets/javascripts/angular-1.4.4/angular-strap-2.3.7.tpl.min.js"
      )))

    defaultArg.addMeta(("charset", "UTF-8"))
      // .addMeta(("name", "viewport"), ("content", "width=device-width, initial-scale=1"))
      // .addMeta(("http-equiv", "X-UA-Compatible"), ("content", "IE=edge"))
      .addMeta(("name", "description"), ("content", "合利宝"))
      .addMeta(("name", "author"), ("content", "顾颖炯"))
//    defaultArg.baseHref = Some("/")
    defaultArg
  }
}