package xyz.statki

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{JsObject, JsString, JsValue, RootJsonFormat}
import spray.json._
import xyz.statki.Board.Ship

object Board {

  final case class Position(x: Int, y: Int)

  sealed trait Direction extends Product

  final case object Down extends Direction

  final case object Right extends Direction

  final case class Ship(id: Int, len: Int)

  final case class Placement(ship: Ship, position: Position, direction: Direction)

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
  override def receive: Receive = ???
}