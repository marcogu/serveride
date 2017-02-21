package models.viewparam


object ManageProjTemplArg {
  def cssurls:Seq[String] = Seq( "/assets/stylesheets/mgproj.css")
  def mainarg(title:String)= {
  	val varg = new MainTempateArguments(title = title, cssUrl = Some(cssurls), jsUrl = None)
    varg.addMeta("charset"->"UTF-8").asInstanceOf[MainTempateArguments]
  }
}