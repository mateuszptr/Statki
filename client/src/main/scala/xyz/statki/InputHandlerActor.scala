package xyz.statki

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.scalajs.dom
import org.scalajs.dom.WebSocket
import org.scalajs.dom.raw.{Event, MessageEvent}

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import Protocol._

class InputHandlerActor(canvas: dom.html.Canvas, webSocket: WebSocket, clientActor: ActorRef) extends Actor with ActorLogging {

  def handleClick(x: Int, y: Int): Unit = {

  }

  def handleUserInput(): Unit = {
    canvas.onclick = { event =>
      val rect = canvas.getBoundingClientRect()
      val x = (event.clientX - rect.left) / (rect.right - rect.left) * canvas.width
      val y = (event.clientY - rect.top) / (rect.bottom - rect.top) * canvas.height
      handleClick(x.toInt,y.toInt)
    }
  }

  override def preStart(): Unit = {
    webSocket.onopen = { event =>

    }

    webSocket.onmessage = {event =>
      val msg = event.data.toString
      val command = decode[Command](msg).toOption.get
      clientActor ! command
    }
  }

  override def receive: Receive = ???
}
