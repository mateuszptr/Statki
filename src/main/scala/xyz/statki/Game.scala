package xyz.statki

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object Game {

  sealed trait Phase extends Product

  final case object WaitingPhase extends Phase

  final case object PlacementPhase extends Phase

  final case class Turn(pid: Int) extends Phase

  final case class GameOver(loserPid: Int) extends Phase

  trait JsonSupport extends SprayJsonSupport {

    import DefaultJsonProtocol._

    implicit val turnFormat = jsonFormat1(Turn)
    implicit val gameOverFormat = jsonFormat1(GameOver)

    implicit val phaseFormat = new RootJsonFormat[Phase] {
      override def write(obj: Phase): JsValue = JsObject((obj match {
        case WaitingPhase => JsObject()
        case PlacementPhase => JsObject()
        case t: Turn => t.toJson
        case g: GameOver => g.toJson
      }).asJsObject.fields + ("phase" -> JsString(obj.productPrefix)))

      override def read(json: JsValue): Phase = json.asJsObject.getFields("phase") match {
        case Seq(JsString("WaitingPhase")) => WaitingPhase
        case Seq(JsString("PlacementPhase")) => PlacementPhase
        case Seq(JsString("Turn")) => json.convertTo[Turn]
        case Seq(JsString("GameOver")) => json.convertTo[GameOver]
      }
    }
  }

}
