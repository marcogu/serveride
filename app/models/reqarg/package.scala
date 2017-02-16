package models



/**
  * Created by marco on 2017/1/11.
  */
package object reqarg {

  case class SQL(sql:String)

  import play.api.data.Form
  import play.api.data.Forms._

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

  object JSFormatImplicit {
    import play.api.libs.functional.syntax._
    import play.api.libs.json.{Reads, Writes, _}
    import services.actor.DevApp.{AppInfo, RunningInfo}
    import services.actor.RunningStdLog.Line
    import services.actor.DevApp.SourcePath

    implicit val jsProj = Json.format[Project]
    implicit val jsRunInfo = Json.format[RunningInfo]
    implicit val jsAppInfo = Json.format[AppInfo]
    implicit val jsConsoleLine = Json.format[Line]
    implicit lazy val nodeFormat: Format[SourcePath] = (
      (__ \ "rpath").format[String] and (__ \ "isFile").format[Boolean] and
        (__ \ "subs").lazyFormatNullable(Reads.seq[SourcePath](nodeFormat), Writes.seq[SourcePath](nodeFormat))
      )(SourcePath.apply, unlift(SourcePath.unapply))
  }
}
