package models.viewparam

import collection.mutable.{Seq=>MSeq}

/**
  * Created by marco on 2017/2/3.
  */
object EditorTemplateArg {
  def cssurls:MSeq[String] = MSeq(
    "/assets/javascripts/codemirro5-23-0/codemirror.css",
    "/assets/javascripts/codemirro5-23-0/theme/ambiance.css",
    "/assets/javascripts/codemirro5-23-0/theme/eclipse.css",
    "/assets/javascripts/codemirro5-23-0/theme/3024-day.css",
    "/assets/javascripts/codemirro5-23-0/theme/3024-night.css",
    "/assets/javascripts/codemirro5-23-0/theme/abcdef.css",
    "/assets/javascripts/codemirro5-23-0/theme/base16-dark.css",
    "/assets/javascripts/codemirro5-23-0/theme/base16-light.css",
    "/assets/javascripts/codemirro5-23-0/theme/bespin.css",
    "/assets/javascripts/codemirro5-23-0/theme/cobalt.css",
    "/assets/javascripts/codemirro5-23-0/theme/neo.css",
    "/assets/javascripts/codemirro5-23-0/theme/mdn-like.css",
    "/assets/javascripts/codemirro5-23-0/theme/midnight.css",
    "/assets/javascripts/codemirro5-23-0/theme/vibrant-ink.css",
    "/assets/javascripts/codemirro5-23-0/theme/xq-light.css",
    "/assets/javascripts/codemirro5-23-0/theme/zenburn.css",
    "/assets/lib/bootstrap/css/bootstrap.min.css",
    "/assets/stylesheets/jquery.contextMenu.min.css",
    "/assets/stylesheets/templatemo_main.css",
    "/assets/stylesheets/xterm.css"
  )

  def jsurls:MSeq[String] = MSeq(
    "/assets/javascripts/jquery-3.1.1.js",
    "/assets/javascripts/jquery.ui.position.min.js",
    "/assets/javascripts/jquery.contextMenu.min.js",
    "/assets/lib/bootstrap/js/modal.js",
    "/assets/javascripts/codemirro5-23-0/codemirror.js",
    "/assets/javascripts/codemirro5-23-0/mode/clike/clike.js",
    "/assets/javascripts/codemirro5-23-0/mode/htmlmixed/htmlmixed.js",
    "/assets/javascripts/codemirro5-23-0/mode/htmlembedded/htmlembedded.js",
    "/assets/javascripts/codemirro5-23-0/mode/javascript/javascript.js",
    "/assets/javascripts/codemirro5-23-0/mode/css/css.js",
    "/assets/javascripts/codemirro5-23-0/mode/properties/properties.js",
    "/assets/javascripts/xtermjs-2.3.2/xterm.js",
    "/assets/javascripts/xtermjs-2.3.2/addons/attach/attach.js",
    "/assets/javascripts/xtermjs-2.3.2/addons/fit/fit.js",
    // "/assets/javascripts/xtermjs-2.3.2/addons/fullscreen.js",
    "/assets/javascripts/editor/jqueryparttern.js"
  )

  def mainarg(title:String):MainTempateArguments = {
    val varg = new MainTempateArguments(title = title, cssUrl = Some(cssurls), jsUrl = Some(jsurls))
    varg.addMeta("charset"->"UTF-8").asInstanceOf[MainTempateArguments]
  }
}
