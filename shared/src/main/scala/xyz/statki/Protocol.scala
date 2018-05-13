package xyz.statki

import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.JsonCodec

object Protocol {

  @JsonCodec final case class Position(x: Int, y: Int) {
    def +(other: Position) = Position(x + other.x, y + other.y)

    def valid(dim: Int): Boolean = x >= 0 && x < dim && y >= 0 && y < dim
  }

  @JsonCodec sealed trait Direction extends Product

  final case object Down extends Direction

  final case object Right extends Direction

  @JsonCodec final case class Ship(id: Int, len: Int)

  @JsonCodec final case class Placement(ship: Ship, position: Position, direction: Direction) {
    def positions: Seq[Position] = for (i <- 0 until ship.len) yield direction match {
      case Down => position + Position(0, i)
      case Right => position + Position(i, 0)
    }
  }

  @JsonCodec sealed trait Field extends Product

  final case class ShipField(ship: Ship) extends Field

  final case class HitField(ship: Ship) extends Field

  final case class SunkField(ship: Ship) extends Field

  final case object MissField extends Field

  @JsonCodec sealed trait Phase extends Product

  final case object WaitingPhase extends Phase

  final case object PlacementPhase extends Phase

  final case class Turn(pid: Int) extends Phase

  final case class GameOver(loserPid: Int) extends Phase

  object AsInt {
    def unapply(s: String) = try { Some(s.toInt) } catch {
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

  implicit val actorRefDecoder = new Decoder[Any] {
    override def apply(c: HCursor): Result[Any] = Left(DecodingFailure("Not implemented", c.history))
  }

  implicit val actorRefEncoder = new Encoder[Any] {
    override def apply(a: Any): Json = ???
  }

  @JsonCodec sealed trait Command

  final case class PlayerUpdate(pid: Int, gid: String) extends Command

  final case class ShootCommand(pid: Int, gid: String, position: Position) extends Command

  final case class PlaceCommand(pid: Int, gid: String, placement: Placement) extends Command

  final case class StateCommand(pid: Int, gid: String) extends Command

  final case class ShootReply(pid: Int, gid: String, position: Position, result: Option[Field]) extends Command

  final case class PlaceReply(pid: Int, gid: String, placement: Placement, result: Boolean) extends Command

  final case class StateReply(pid: Int, gid: String, playerBoard: Map[Position, Field], enemyBoard: Map[Position, Field], phase: Phase) extends Command

  final case class PhaseNotification(gid: String, phase: Phase) extends Command

  final case class PlayerConnected(pid: Int, gid: String, actorRef: Any) extends Command

  final case class PlayerDisconnected(pid: Int, gid: String) extends Command
}
