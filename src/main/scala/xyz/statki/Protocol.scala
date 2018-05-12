package xyz.statki

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.JsonCodec
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe._
import io.circe.generic.semiauto._
import xyz.statki.Board._
import xyz.statki.Game._
import spray.json._



object Protocol {
  object AsInt {
    def unapply(s: String) = try{ Some(s.toInt) } catch {
      case e: NumberFormatException => None
    }
  }

  val PositionRegex = "Position\\((\\d+),(\\d+)\\)".r

  object PositionString {
    def unapply(str: String): Option[Position] = str match {
      case PositionRegex(AsInt(x), AsInt(y)) => Some(Position(x, y))
      case _ => None
    }
  }

  implicit val positionKeyEncoder = new KeyEncoder[Position] {
    override def apply(key: Position): String = key.toString
  }
  implicit val positionKeyDecoder = new KeyDecoder[Position] {
    override def apply(key: String): Option[Position] = {
      PositionString.unapply(key)
    }
  }

  implicit val actorRefDecoder = new Decoder[ActorRef] {
    override def apply(c: HCursor): Result[ActorRef] = Left(DecodingFailure("Not implemented", c.history))
  }

  implicit val actorRefEncoder = new Encoder[ActorRef] {
    override def apply(a: ActorRef): Json = ???
  }

  @JsonCodec sealed trait Command

  final case class PlayerUpdate(pid: Int, gid: String) extends Command

  final case class ShootCommand(pid: Int, gid: String, position: Position) extends Command

  final case class PlaceCommand(pid: Int, gid: String, placement: Placement) extends Command

  final case class StateCommand(pid: Int, gid: String) extends Command

  final case class ShootReply(pid: Int, gid: String, position: Position, result: Option[Field]) extends Command

  final case class PlaceReply(pid: Int, gid: String, placement: Placement, result: Boolean) extends Command

  final case class StateReply(pid: Int, gid: String, playerBoard: Map[Position, Field], enemyBoard: Map[Position, Field], phase: Phase)  extends Command

  final case class PhaseNotification(gid: String, phase: Phase) extends Command

  final case class PlayerConnected(pid: Int, gid: String, actorRef: ActorRef) extends Command

  final case class PlayerDisconnected(pid: Int, gid: String) extends Command
}
