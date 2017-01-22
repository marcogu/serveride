package controllers

import models.viewparam.MainTempateArguments
import play.Logger
import play.api.mvc._


/**
  * Created by marco on 2017/1/17.
  */
class MainController extends Controller {

  def appIdx = Action(Ok(views.html.dashboardidx(MainTempateArguments())))

  def viewcmp(pkg:String, cmpName:String) = Action {
    Logger.debug(s"$pkg, $cmpName")
    val tclz = (pkg, cmpName) match {
      case (null, _) => Class.forName(s"views.html.$cmpName")
      case other =>  Class.forName(s"views.html.$pkg.$cmpName")
    }
    Ok(tclz.getDeclaredMethod("apply").invoke(tclz).asInstanceOf[play.twirl.api.HtmlFormat.Appendable])
  }
}
