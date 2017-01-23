package services.mpos

import play.api.db.Database


case class SQL(sql:String)

class QuerySession(db:Database) {
  lazy val conn = db.getConnection()
  def finish() = conn.close()

  def query(sql:String):Seq[Map[String,String]] = {
    assert(!conn.isClosed)
    val rs = conn.createStatement().executeQuery(sql)
    val meta = rs.getMetaData

    val head = 1 to meta.getColumnCount map { idx => meta.getColumnName(idx) }
    val out = collection.mutable.ArrayBuffer[Map[String,String]]()

    while(rs.next()) { out.append(Map(head.map { e => e -> rs.getString(e) }:_*)) }
    out
  }
}


object DBService  {
  def session(db:Database) = new QuerySession(db)
}


