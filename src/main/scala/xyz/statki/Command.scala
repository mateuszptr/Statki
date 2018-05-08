package xyz.statki

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


object Command {

  trait JsonSupport extends SprayJsonSupport {

    import DefaultJsonProtocol._

    implicit val playerUpdateFormat = jsonFormat2(PlayerUpdate)
    implicit val shootCommandFormat = jsonFormat3(ShootCommand)
    implicit val placeCommandFormat = jsonFormat3(PlaceCommand)
    implicit val stateCommandFormat = jsonFormat2(StateCommand)
    implicit val shootReplyFormat = jsonFormat4(ShootReply)
    implicit val placeReplyFormat = jsonFormat4(ShootReply)
    implicit val stateReplyFormat = jsonFormat5(StateReply)
    implicit val phaseNotificationFormat = jsonFormat2(PhaseNotification)

    implicit val commandFormat = new RootJsonFormat[Command] {
      override def write(obj: Command): JsValue = JsObject((obj match {
        case pu: PlayerUpdate => pu.toJson
        case sc: ShootCommand => sc.toJson
        case pc: PlaceCommand => pc.toJson
        case sc: StateCommand => sc.toJson
        case sr: ShootReply => sr.toJson
        case pr: PlaceReply => pr.toJson
        case sr: StateReply => sr.toJson
        case pn: PhaseNotification => pn.toJson
      }).asJsObject.fields + ("type" -> JsString(obj.productPrefix)))

      override def read(json: JsValue): Command = json.asJsObject.getFields("type") match {
        case Seq(JsString("PlayerUpdate")) => json.convertTo[PlayerUpdate]
        case Seq(JsString("ShootCommand")) => json.convertTo[ShootCommand]
        case Seq(JsString("PlaceCommand")) => json.convertTo[PlaceCommand]
        case Seq(JsString("StateCommand")) => json.convertTo[StateCommand]
        case Seq(JsString("ShootReply")) => json.convertTo[ShootReply]
        case Seq(JsString("PlaceReply")) => json.convertTo[PlaceReply]
        case Seq(JsString("StateReply")) => json.convertTo[StateReply]
        case Seq(JsString("PhaseNotification")) => json.convertTo[PhaseNotification]
      }
    }

  }

}

