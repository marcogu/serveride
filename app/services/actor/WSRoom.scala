package services.actor

import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import play.api.libs.json.JsValue
//import akka.pattern.ask
//import scala.concurrent.Future
//import scala.concurrent.duration._


/**
  * Created by marco on 2017/2/10.
  */
class RMMember(room:ActorRef, socketOut:ActorRef, roomId:String) extends Actor{
  import services.actor.WSRoom._

  override def preStart(): Unit = room ! MemberIn(self)
  override def postStop(): Unit = room ! MemberOut(self)

  def receive = {
    case Publish(msg) => socketOut ! msg
    case msg: JsValue => println(msg)
    case msg: String => println(self.path) //  socketOut ! "my name is marco"
    case msg: Array[Byte] => println(msg.length);
    case other => println(other)
  }
}

trait WebsocketRoom {
  /** Once a new socket connected, use this function register a scoket event handler Actor
    *
    * @param socketSender The instance will handler Actor need to sent response message
    * */
  def inRoom(socketSender:ActorRef, meberIdGen:()=>String=()=>""):Props

  /** Close connect by connect Id where from the inRoom return value ._2
    *
    * @param actorId Child name of Room Actor context
    * @return if handler actor did find then finish it and return true, else return false
    */
  def kick(actorId : String):Boolean

  def publishQuestion(msg:JsValue):Unit

  def roomActor:ActorRef
}


object WSRoom{

  private class WSRoom extends Actor{
    import collection.mutable.{Set=>MSet}
    override def preStart(): Unit = {
      println(self.path)
    }

    val setIdx:MSet[ActorRef] = MSet()

    // ---> remote it, client actor only support WSRoom defined message type, use JsValue instead
    import services.actor.DevApp.ConsoleInfo
    def receive = {
      case MemberIn(actor) => setIdx.add(actor)
      case MemberOut(actor) => setIdx.remove(actor)
      case msg:JsValue => setIdx.foreach{ _ ! Publish(msg)}
      // ---> remote it, client actor only support WSRoom defined message type, use JsValue instead
      case ConsoleInfo(proj, info) =>
        println(s"websocket room get running application console dispatch message:\n$info")
    }
  }


  class RoomWrapper(val roomActor: ActorRef) extends WebsocketRoom {
    def inRoom(socketSender:ActorRef, meberIdGen:()=>String=()=>""):Props =
      Props(new RMMember(roomActor, socketSender, meberIdGen()))

    def kick(actorId : String):Boolean = false

    def publishQuestion(msg:JsValue) = roomActor ! msg
  }


  case class MemberIn(socket:ActorRef)
  case class MemberOut(socket:ActorRef)
  case class Publish(msg:JsValue)


  private var _defroom:Option[WebsocketRoom] = None
  private val defaultRoomName:String = "defaultWSRoom"

  @throws[IllegalArgumentException]
  def apply(actsys:ActorSystem):WebsocketRoom = _defroom match {
    case Some(defaultRoom) => throw new IllegalArgumentException(
      "apply for default room instance can only be executed once, use 'default' method instead")
    case None => _defroom = Some( new RoomWrapper(actsys.actorOf(Props(new WSRoom), defaultRoomName)))
      _defroom.get
  }

  def default:WebsocketRoom = _defroom.fold(
    throw new IllegalArgumentException("Need to perform 'apply()' once")
  )( df => df )
}



