package xyz.statki

import akka.actor.{Actor, ActorLogging}
import xyz.statki.Protocol._
import org.scalajs.dom.{CanvasRenderingContext2D, html}
import org.scalajs.dom

import scala.collection.mutable

class ClientGame(state: StateReply, canvas: html.Canvas) extends Actor with ActorLogging {

  var phase: Phase = state.phase

  val playerBoard: mutable.Map[Position, Field] = mutable.Map(state.playerBoard.toSeq: _*)
  val enemyBoard: mutable.Map[Position, Field] = mutable.Map(state.enemyBoard.toSeq: _*)

  val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  override def preStart(): Unit = {

  }

  override def receive: Receive = ???
}
