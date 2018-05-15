package xyz.statki

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import org.scalajs.dom
import org.scalajs.dom.WebSocket
import org.scalajs.dom.raw.{ Event, MessageEvent }
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import Protocol._

object InputOutputHandler {
  def props(pid: Int, gid: String, canvas: dom.html.Canvas, addr: String, clientActor: ActorRef) = Props(new InputOutputHandler(pid, gid, canvas, addr, clientActor))
}

class InputOutputHandler(pid: Int, gid: String, canvas: dom.html.Canvas, addr: String, clientActor: ActorRef) extends Actor with ActorLogging {

  var direction: Protocol.Direction = Protocol.Down

  def handleKeyboard(): Unit = {
    dom.document.onkeypress = { event =>
      event.keyCode match {
        case 39 => direction = Protocol.Right
        case 40 => direction = Protocol.Down
      }
    }
  }

  def handleClick(x: Int, y: Int): Unit = {
    val insideEnemyBoard = 0 <= x && x < ClientGame.tileDim * 10 && 0 <= y && y < ClientGame.tileDim * 10
    val insidePlayerBoard = 0 <= x && x < ClientGame.tileDim * 10 && ClientGame.playerBoardPos <= y && y < ClientGame.playerBoardPos + ClientGame.tileDim * 10

    if (insideEnemyBoard) {
      val position = Position(x / ClientGame.tileDim, (y - ClientGame.enemyBoardPos) / ClientGame.tileDim)
      clientActor ! ShootCommand(pid, gid, position)
    } else if (insidePlayerBoard) {
      val position = Position(x / ClientGame.tileDim, (y - ClientGame.playerBoardPos) / ClientGame.tileDim)
      clientActor ! PlaceCommand(pid, gid, Placement(Ship(0, 0), position, direction))
    }
  }

  def handleUserInput(): Unit = {
    canvas.onclick = { event =>
      val rect = canvas.getBoundingClientRect()
      val x = (event.clientX - rect.left) / (rect.right - rect.left) * canvas.width
      val y = (event.clientY - rect.top) / (rect.bottom - rect.top) * canvas.height
      handleClick(x.toInt, y.toInt)
      handleKeyboard()
    }
  }

  val webSocket = new WebSocket(addr)

  override def preStart(): Unit = {

    webSocket.onopen = { event =>
      handleUserInput()
      clientActor ! "SocketReady"
      context.become(recvCommands)
    }

    webSocket.onmessage = { event =>
      val msg = event.data.toString
      val command = decode[Command](msg).toOption.get
      clientActor ! command
    }
  }

  def recvCommands: Receive = {
    case c: Command => webSocket.send(c.asJson.noSpaces)
  }

  override def receive: Receive = Actor.emptyBehavior

}
