package controllers

import models.viewparam.MainTempateArguments
import play.api.mvc._

/**
  * Created by marco on 2017/1/17.
  */
class MainController extends Controller {

  def appIdx = Action(Ok(views.html.dashboardidx(MainTempateArguments())))
}
