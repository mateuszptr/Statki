package xyz.statki

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import xyz.statki.Board.{Field, Placement, Position}
import xyz.statki.Game.Phase
import spray.json._


sealed trait Command extends Product

final case class PlayerUpdate(pid: Int, gid: String) extends Command

final case class ShootCommand(pid: Int, gid: String, position: Position) extends Command

final case class PlaceCommand(pid: Int, gid: String, placement: Placement) extends Command

final case class StateCommand(pid: Int, gid: String) extends Command

final case class ShootReply(pid: Int, gid: String, position: Position, result: Option[Field]) extends Command

final case class PlaceReply(pid: Int, gid: String, placement: Placement, result: Boolean) extends Command

final case class StateReply(pid: Int, gid: String, playerBoard: Map[Position, Field], enemyBoard: Map[Position, Field], phase: Phase) extends Command

final case class PhaseNotification(gid: String, phase: Phase) extends Command

final case class PlayerConnected(pid: Int, gid: String, actorRef: ActorRef) extends Command

final case class PlayerDisconnected(pid: Int, gid: String) extends Command


object Command {

}

