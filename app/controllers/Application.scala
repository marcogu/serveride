package controllers

import javax.inject.Inject

import play.api.db.Database
import play.api.mvc._


class Application @Inject() (db:Database)  extends Controller {
  def index = Action { Ok("hello")}

  def mpostest = Action {
    val query =
      "SELECT A.AGTORG,A.TMERCID,A.TTERMID,D.NOD_ID,D.AGTORGCPUS,D.TPDU,D.TTMKKEY" +
        "         FROM STPNODMERKEYINF A" +
        "         LEFT JOIN OMNG_NODPARA D ON TRIM(A.AGTORG)=TRIM(D.AGTORG) AND A.ORG_ID = D.NOD_ID" +
      "         WHERE ROWNUM < 2"
    val conn = db.getConnection()
    var resultString:String = ""
    try{
      val stm = conn.createStatement()
      val rs = stm.executeQuery(query)

      while (rs.next()) {
        resultString = rs.getString("TMERCID")
      }
    } finally {
      conn.close()
    }
    Ok(resultString)
  }


}