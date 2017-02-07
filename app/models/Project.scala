package models

import play.api.Environment
import services.mpos.{QuerySession, DBService, DbCfg}

case class Project(pname:String, path:String)

object Project {
  val selfName = "CrjqSwiperMockServer"

  def named(n:String)(implicit session:QuerySession):Project =
    session.query(s"SELECT * FROM PROJECT p WHERE pn='$n'").map{
      item => Project(item("PN"), item("PURL"))
    }.headOption.getOrElse(null.asInstanceOf[Project])

  def all(implicit session:QuerySession):Seq[Project] = session.query("SELECT * FROM PROJECT").map {
    item=> Project(item("PN"), item("PURL"))
  }

  def newProj(n:String, url:String)(implicit session:QuerySession):Project = {
    session.exc(s"INSERT INTO PROJECT VALUES('$n','$url')")
    Project(n,url)
  }

  object H2Checker{
    private val h2dbCfg = DbCfg("jdbc:h2:./public/toolappdef.db", "marco", "123456", "org.h2.Driver")
    private lazy val h2db = DBService(h2dbCfg)  
    private var isInstalled = false

    def apply():DBService = if(!isInstalled) {
      val session = h2db.genQuerySession
      val tbSchema = session.query("SELECT * FROM INFORMATION_SCHEMA.TABLES tb WHERE tb.TABLE_NAME='PROJECT'")
      if( tbSchema.isEmpty ) {
        session.exc("CREATE TABLE PROJECT ( pn VARCHAR(128), purl VARCHAR(1024) )")
        val selfRoot = Environment.simple().rootPath
        val insertSql = s"INSERT INTO PROJECT VALUES('$selfName', '${selfRoot.getAbsolutePath}')"
        session.exc(insertSql)
      }
      session.finish()
      isInstalled = true
      h2db
    } else h2db
  }
}

