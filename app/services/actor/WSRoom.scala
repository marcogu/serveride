package services.actor

import akka.actor._
import play.api.libs.json.JsValue
import collection.mutable.{Map => MMap}


/**
  * Created by marco on 2017/2/10.
  */
class RMMember(room:ActorRef, socketOut:ActorRef, roomId:String) extends Actor{
  import services.actor.WSRoom._

  override def preStart(): Unit = room ! MemberIn(self)
  override def postStop(): Unit = if(!isKillSelf) room ! MemberOut(self)

  var isKillSelf = false

  def receive = {
    case Publish(msg) => socketOut ! msg
    case RoomClose => isKillSelf = true
      self ! PoisonPill 
      println(s"try close websocket actor")

    case msg: JsValue => println(msg)
    case msg: String => println(self.path) //  socketOut ! "my name is marco"
    case msg: Array[Byte] => println(msg.length);
    case other => println(other)
  }
}

trait WebsocketRoom {

  /** Use it for generate Play socket Flow Actor Props Once a new socket connected.
    *
    * @param socketSender Socket response actor
    * @param meberIdGen Handler actor name
    * */
  def inRoom(socketSender:ActorRef, meberIdGen:()=>String=()=>""):Props

  /** Close connect by connect Id where from the inRoom return value ._2
    *
    * @param actorId Child name of Room Actor context
    * @return if handler actor did find then finish it and return true, else return false
    */
  def kick(actorId : String):Boolean = false

  def publishMessage(msg:AnyRef):Unit = roomActor ! msg

  /** This Actor is The Manager of 'Play socket Flow Actor', called Room
    * It contain all socket Actor reference.
    * Send Publish(JsValue) class instance when you want to publish a message
    * */
  def roomActor:ActorRef

  def subrooms:collection.Map[String, WebsocketRoom]

  /** This function be designed for the only way to build a sub room, Please use subroom
    * reference to generate an 'Websocket Flow Actor' to pay, and then the room will contain
    * socket actor reference.
    * */
  def createSubRoom(name:String):WebsocketRoom = {
    import akka.pattern.ask
    import scala.concurrent.Await
    import scala.concurrent.duration._
    import WSRoom.{CreateRoom, RoomWrapper}
    implicit val timeout: akka.util.Timeout = 2.seconds
    val newRoom =
      new RoomWrapper(Await.result((roomActor ? CreateRoom(name)).mapTo[CreateRoom], timeout.duration).roomActor)
    subrooms.asInstanceOf[MMap[String, WebsocketRoom]].put(name, newRoom)
    newRoom
  }

  /** Get sub room by name
    * */
  def subRoom(name:String):Option[WebsocketRoom] = subrooms.get(name)

  def removeSubroom(name:String) = subRoom(name) match {
    case None =>
    case Some(wrapper) => wrapper.destroy(); println(s"sub room did found")
  }

  /** Override this method for custom destroy, release resources.
    * */
  def destroy():Unit = {subrooms.foreach{ entry =>
    println(s"roomActor ${roomActor.path} destroy()")
    entry._2.destroy() 
  }; roomActor ! PoisonPill}
}


object WSRoom{

  private class WSRoom extends Actor{
    import collection.mutable.{Set=>MSet}

    override def postStop(): Unit = clients.foreach { client => client ! RoomClose }
    val clients:MSet[ActorRef] = MSet()

    def receive = {
      case MemberIn(actor) => clients.add(actor)
      case MemberOut(actor) => clients.remove(actor)
      case msg:JsValue => clients.foreach{ _ ! Publish(msg)}
      case str:String => clients.foreach{ _ ! Publish(str)}
      case CreateRoom(name, null) => sender() ! CreateRoom(name, context.actorOf(Props(new WSRoom()), name))
    }
  }


  class RoomWrapper(val roomActor: ActorRef) extends WebsocketRoom {

    private val subs:MMap[String, WebsocketRoom] = MMap()
    override def subrooms = subs

    def inRoom(socketSender:ActorRef, meberIdGen:()=>String=()=>""):Props =
      Props(new RMMember(roomActor, socketSender, meberIdGen()))
  }


  case class MemberIn(socket:ActorRef)
  case class MemberOut(socket:ActorRef)
  case object RoomClose
  case class Publish(obj:AnyRef) // [JsValue | String]
  case class CreateRoom(name:String, roomActor:ActorRef = null)


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