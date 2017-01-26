package models

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by marco on 2017/1/11.
  */
package object reqarg {

  case class SQL(sql:String)

  val xmlForm = Form(
    mapping(
      "requestXml" -> text
    )(XMLPack.apply)(XMLPack.unapply)
  )

  val sqlForm = Form(
    mapping(
      "sql" -> text
    )(SQL.apply)(SQL.unapply)
  )

  object MockMkQuery{
    def queryMainKey(psam:String)(implicit db:Map[String, String]) = "9D37F2AF79A3B42F9D37F2AF79A3B42F"
  }
}
