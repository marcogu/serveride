package models

import play.api.Environment
import services.mpos.{QuerySession, DBService, DbCfg}

import scala.reflect.io.Path


/** The project root url is Path(Project.path) / Project.pname
  * ex: Path("FirstProj", "/Users/Person/Documents") the root path
  * is '/Users/Person/Documents/FirstProj"
  *
  * @param pname Project name, root folder name
  * @param path Project container path.
  * */
case class Project(pname:String, path:String){
  import scala.reflect.io.Path
  lazy val root = Path(path) / pname
}


/**
  * @param name Workspace description name
  * @param url Workspace Location
  * */
case class WorkSpace(name:String, url:String)

object Project {
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

  object DDL{
    private val h2dbCfg = DbCfg("jdbc:h2:./public/toolappdef.db", "marco", "123456", "org.h2.Driver")
    private lazy val h2db = DBService(h2dbCfg)  
    private var isInstalled = false

    /**
      * This function is not a thread safe function.
      * Make sure it will not be executed concurrently is the caller's responsibility
      * */
    def apply():DBService = if(!isInstalled) {
      isInstalled = true
      val session = h2db.genQuerySession
      if( isEmptyDb(session)) {
        excDDL(session)
        insertDefaultData(session)
        insertDevelopData(session)
      }
      session.finish()
      h2db
    } else h2db

    private def isEmptyDb(s:QuerySession):Boolean = {
      s.query("SELECT * FROM INFORMATION_SCHEMA.TABLES tb WHERE tb.TABLE_NAME='PROJECT'").isEmpty
    }

    private def excDDL(s:QuerySession) = {
      val defaultSchema =
        "create TABLE PROJECT (" +
          "    PN VARCHAR(128) not null," +
          "    PURL VARCHAR(1024) not null," +
          "    PRIMARY KEY (PN)" +
          ");" +
          "CREATE UNIQUE INDEX PROJECT_PN_UINDEX ON PROJECT (PN);" +
          "create TABLE WORKSAPCE (" +
          "    URL VARCHAR(1024) not null," +
          "    NAME VARCHAR(256)," +
          "    PRIMARY KEY (URL)" +
          ");"
      s.exc(defaultSchema)
    }

    private def insertDefaultData(s:QuerySession) = {
      val p = Path(Environment.simple().rootPath.getCanonicalPath)
      s.exc(s"INSERT INTO PROJECT VALUES('${p.name}', '${p.parent.path}')")

      val defaultWorkspacePath = p.parent / "jazydefwps"
      defaultWorkspacePath.exists match {
        case true if !defaultWorkspacePath.isDirectory =>
          throw new Exception(s"can not create default workspace on $defaultWorkspacePath, contain the same name file")
        case false => defaultWorkspacePath.createDirectory()
        case true =>
      }

      if(defaultWorkspacePath.isDirectory)
        s.exc(s"INSERT INTO WORKSAPCE VALUES('${defaultWorkspacePath.path}', 'default workspace')")
    }

    def insertDevelopData(s:QuerySession) = {
      s.exc(s"INSERT INTO PROJECT VALUES ('autotoolt6', '${WorkSpace.default(s).url}')")
    }
  }
}

object WorkSpace{
  def default(implicit session:QuerySession):WorkSpace = { session.query("SELECT * FROM WORKSAPCE LIMIT 1").map { item =>
    WorkSpace(item.get("NAME").get, item.get("URL").get);
  }.head }
}
