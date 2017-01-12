package mobels

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by marco on 2017/1/11.
  */
package object reqarg {
  val xmlForm = Form(
    mapping(
      "requestXml" -> text
    )(XMLPack.apply)(XMLPack.unapply)
  )

  object MockMkQuery{
    def queryMainKey(psam:String)(implicit db:Map[String, String] = Map.empty[String, String]):String = db(psam) match {
      case null =>  "9D37F2AF79A3B42F9D37F2AF79A3B42F"
      case other => other
    }
  }
}
