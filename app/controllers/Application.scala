package controllers

import javax.inject.Inject

import play.api.db.Database
import play.api.libs.json.Json
import play.api.mvc._
import services.mpos.DBService


class Application @Inject() (db:Database)  extends Controller {
  def index = Action { Ok("hello") }

  val seq =
    "SELECT A.AGTORG,A.TMERCID,A.TTERMID,D.NOD_ID,D.AGTORGCPUS,D.TPDU,D.TTMKKEY" +
      "         FROM STPNODMERKEYINF A" +
      "         LEFT JOIN OMNG_NODPARA D ON TRIM(A.AGTORG)=TRIM(D.AGTORG) AND A.ORG_ID = D.NOD_ID" +
      "         WHERE ROWNUM < 2"

  def encapsulation = Action { Ok(Json.toJson(DBService.session(db).query(seq))) }
}