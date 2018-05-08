package xyz.statki

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{JsObject, JsString, JsValue, RootJsonFormat, _}
import xyz.statki.Board._
import xyz.statki.Game.{GameOver, WaitingPhase}

import scala.collection.mutable

object Board {

  final case class Position(x: Int, y: Int) {
    def +(other: Position) = Position(x + other.x, y + other.y)

    def valid(dim: Int): Boolean = x >= 0 && x < dim && y >= 0 && y < dim
  }

  sealed trait Direction extends Product

  final case object Down extends Direction

  final case object Right extends Direction

  final case class Ship(id: Int, len: Int)

  final case class Placement(ship: Ship, position: Position, direction: Direction) {
    def positions: Seq[Position] = for (i <- 0 until ship.len) yield direction match {
      case Down => position + Position(0, i)
      case Right => position + Position(i, 0)
    }
  }

  sealed trait Field extends Product

  final case class ShipField(ship: Ship) extends Field

  final case class HitField(ship: Ship) extends Field

  final case class SunkField(ship: Ship) extends Field

  final case object MissField extends Field

  trait JsonSupport extends SprayJsonSupport {

    import DefaultJsonProtocol._

    implicit val positionFormat = jsonFormat2(Position)

    implicit val directionFormat = new RootJsonFormat[Direction] {
      override def write(obj: Direction): JsValue = JsObject("direction" -> JsString(obj.productPrefix))

      override def read(json: JsValue): Direction = json.asJsObject.getFields("direction") match {
        case Seq(JsString("Down")) => Down
        case Seq(JsString("Right")) => Right
      }
    }

    implicit val shipFormat = jsonFormat2(Ship)

    implicit val placementFormat = jsonFormat3(Placement)

    implicit val shipFieldFormat = jsonFormat1(ShipField)
    implicit val hitFieldFormat = jsonFormat1(HitField)
    implicit val sunkFieldFormat = jsonFormat1(SunkField)

    implicit val fieldFormat = new RootJsonFormat[Field] {
      override def write(obj: Field): JsValue = JsObject((obj match {
        case s: ShipField => s.toJson
        case h: HitField => h.toJson
        case s: SunkField => s.toJson
        case MissField => JsObject()
      }).asJsObject.fields + ("type" -> JsString(obj.productPrefix)))

      override def read(json: JsValue): Field = json.asJsObject.getFields("type") match {
        case Seq(JsString("ShipField")) => json.convertTo[ShipField]
        case Seq(JsString("HitField")) => json.convertTo[HitField]
        case Seq(JsString("SunkField")) => json.convertTo[SunkField]
        case Seq(JsString("MissField")) => MissField
      }
    }
  }

  def props(pid: Int, gid: String, dim: Int, initShips: Set[Ship]): Props = Props(new Board(pid, gid, dim, initShips))
}

class Board(pid: Int, gid: String, dim: Int, initShips: Set[Ship]) extends Actor with ActorLogging {

  var shipsToPlace: Set[Ship] = initShips
  var unsunkShips: Set[Ship] = Set.empty

  val positionToField: mutable.Map[Position, Field] = mutable.Map.empty
  val shipToHits: mutable.Map[Ship, Int] = mutable.Map.empty
  //val shipToPlacement: mutable.Map[Ship, Placement] = mutable.Map.empty

  def placeShip(placement: Placement): Boolean = {
    val canPlace = placement.position.valid(dim) && placement.positions.last.valid(dim) && shipsToPlace.contains(placement.ship) && placement.positions.map(positionToField.get).forall(_.isEmpty)
    if(canPlace) {
      shipsToPlace -= placement.ship
      unsunkShips += placement.ship

      for(pos <- placement.positions) positionToField += pos -> ShipField(placement.ship)
      shipToHits += placement.ship -> 0
      //shipToPlacement += placement.ship -> placement
      true
    } else false
  }

  def shoot(pos: Position): Option[Field]  = {
    if(!pos.valid(dim)) {
      log.warning("Shot outside the board")
      return None
    }
    val optField = positionToField.get(pos)
    optField match {
      case None =>
        positionToField += pos -> MissField
        Some(MissField)
      case Some(MissField) | Some(HitField(_)) | Some(SunkField(_)) =>
        log.warning("Shot same place again")
        None
      case Some(ShipField(ship)) =>
        shipToHits += ship -> (shipToHits(ship)+1)
        if(shipToHits(ship) == ship.len) {
          unsunkShips -= ship
          positionToField += pos -> SunkField(ship)

          for(pos <- positionToField.collect{
            case (pos: Position, HitField(`ship`)) => pos
          }) positionToField += pos -> SunkField(ship)

          Some(SunkField(ship))
        } else {
          positionToField += pos -> HitField(ship)
          Some(HitField(ship))
        }
    }
  }

  def state: Map[Position, Field] = positionToField.toMap

  override def receive: Receive = {
    case ShootCommand(`pid`, `gid`, position) =>
      val result = shoot(position)
      sender() ! ShootReply(pid, gid, position, result)
      if(shipsToPlace.isEmpty && unsunkShips.isEmpty)
        sender() ! PhaseNotification(gid, GameOver(pid))

    case PlaceCommand(`pid`, `gid`, placement) =>
      val result = placeShip(placement)
      sender() ! PlaceReply(pid, gid, placement, result)

    case StateCommand(`pid`, `gid`) =>
      val result = state
      sender() ! StateReply(pid, gid, state, Map.empty, WaitingPhase)

    case c: Command => log.warning(s"Received invalid command $c. Dropping.")
  }
}