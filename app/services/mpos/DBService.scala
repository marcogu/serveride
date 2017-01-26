package services.mpos

import java.sql.Connection

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import play.api.db.Database


class QuerySession(conn:Connection) {
  assert(!conn.isClosed)
  def finish() = conn.close()

  def query(sql:String):Seq[Map[String,String]] = {
    val rs = conn.createStatement().executeQuery(sql)
    val meta = rs.getMetaData

    val head = 1 to meta.getColumnCount map { idx => meta.getColumnName(idx) }
    val out = collection.mutable.ArrayBuffer[Map[String,String]]()

    while(rs.next()) { out.append(Map(head.map { e => e -> rs.getString(e) }:_*)) }
    out
  }
}

case class DbCfg(url:String, act:String, pwd:String, driver:String){
  val hkldsProperites = {
    val config = new HikariConfig()
    config.setJdbcUrl(url)
    config.setUsername(act)
    config.setPassword(pwd)
    config.setDriverClassName(driver)
    config
  }
}

object DbCfg{
  def default():DbCfg = DbCfg(url="jdbc:oracle:thin:@172.16.16.13:1521:ORCL",
  driver = "oracle.jdbc.driver.OracleDriver", act="BPMPOS", pwd = "nK17kLnd")
}

class DBService(val cfg:DbCfg){
  lazy val hikariDs:HikariDataSource = new HikariDataSource(cfg.hkldsProperites)
  def genQuerySession = new QuerySession(hikariDs.getConnection)

  def query(sql:String):Seq[Map[String,String]] = {
    val s = genQuerySession
    val r = s.query(sql)
    s.finish()
    r
  }
}

object DBService  {
  // this is a utils method
  def sessionFromDB(db:Database) = new QuerySession(db.getConnection())
  // dynamic database
  import collection.mutable.{HashMap=>MHMap}
  private lazy val dbsPool:MHMap[String, DBService] = MHMap()

  def apply(cfg:DbCfg) = dbsPool.get(cfg.driver) match {
    case Some(service) => service
    case None => dbsPool.put(cfg.driver, new DBService(cfg))
      dbsPool(cfg.driver)
  }

  def cleanall() = {
    dbsPool.foreach{ enity => if(!enity._2.hikariDs.isClosed) enity._2.hikariDs.close()}
    dbsPool.clear()
  }

  def alldsCfg[T](format:(DbCfg)=>T):Iterator[T] = dbsPool.iterator.map( entry => format(entry._2.cfg))
}