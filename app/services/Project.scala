package services

import play.api.Environment
import scala.reflect.io.Path
import services.mpos.{DBService, DbCfg}

case class Project(pname:String, path:String)

object Project {
  private def qs = H2Checker().genQuerySession
  val selfName = "mobildevmanager"

  def named(n:String):Project = {
    val p = qs.query(s"SELECT * FROM PROJECT p WHERE pn='${n}'").map{ item =>
      Project(item("PN"), item("PURL")) 
    }.headOption.getOrElse(null)         
    qs.finish()
    p
  }

  def newProj(n:String, url:String):Project = {
    qs.exc(s"INSERT INTO PROJECT VALUES('$n','$url')")
    qs.finish()
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
        val insertSql = s"INSERT INTO PROJECT VALUES('$selfName', '${selfRoot.getAbsolutePath()}')"
        session.exc(insertSql)
      }
      session.finish()
      isInstalled = true
      h2db
    } else h2db
  }
}

