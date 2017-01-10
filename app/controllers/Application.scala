package controllers

import business.WK
import play.api._
import play.api.mvc._

import scala.concurrent.Future

//import security

class Application extends Controller {

  def index = Action {
//    println(WK("9D37F2AF79A3B42F9D37F2AF79A3B42F","82570010", "8257001000487725", "51309FA0", "000188").key)
    Ok(views.html.index("Your new application is ready."))
  }

  def testAction = Action(parse.tolerantText) { req =>
    println(s"[${req.body}]")
    Ok("Hello play")
  }



}