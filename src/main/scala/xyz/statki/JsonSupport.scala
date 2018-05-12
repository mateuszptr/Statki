//package xyz.statki
//
//import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
//import spray.json._
//import xyz.statki.Board.{Direction, Down, Field, HitField, MissField, Placement, Position, Right, Ship, ShipField, SunkField}
//import xyz.statki.Game._
//
//
//trait JsonSupport extends SprayJsonSupport {
//  import DefaultJsonProtocol._
//
//  implicit val positionFormat = jsonFormat2(Position)
//
//  implicit val directionFormat = new RootJsonFormat[Direction] {
//    override def write(obj: Direction): JsValue = JsObject("direction" -> JsString(obj.productPrefix))
//
//    override def read(json: JsValue): Direction = json.asJsObject.getFields("direction") match {
//      case Seq(JsString("Down")) => Down
//      case Seq(JsString("Right")) => Right
//    }
//  }
//
//  implicit val shipFormat = jsonFormat2(Ship)
//
//  implicit val placementFormat = jsonFormat3(Placement)
//
//  implicit val shipFieldFormat = jsonFormat1(ShipField)
//  implicit val hitFieldFormat = jsonFormat1(HitField)
//  implicit val sunkFieldFormat = jsonFormat1(SunkField)
//
//  implicit val fieldFormat = new RootJsonFormat[Field] {
//    override def write(obj: Field): JsValue = JsObject((obj match {
//      case s: ShipField => s.toJson
//      case h: HitField => h.toJson
//      case s: SunkField => s.toJson
//      case MissField => JsObject()
//    }).asJsObject.fields + ("type" -> JsString(obj.productPrefix)))
//
//    override def read(json: JsValue): Field = json.asJsObject.getFields("type") match {
//      case Seq(JsString("ShipField")) => json.convertTo[ShipField]
//      case Seq(JsString("HitField")) => json.convertTo[HitField]
//      case Seq(JsString("SunkField")) => json.convertTo[SunkField]
//      case Seq(JsString("MissField")) => MissField
//    }
//  }
//
//  implicit val turnFormat = jsonFormat1(Turn)
//  implicit val gameOverFormat = jsonFormat1(GameOver)
//
//  implicit val phaseFormat = new RootJsonFormat[Phase] {
//    override def write(obj: Phase): JsValue = JsObject((obj match {
//      case WaitingPhase => JsObject()
//      case PlacementPhase => JsObject()
//      case t: Turn => t.toJson
//      case g: GameOver => g.toJson
//    }).asJsObject.fields + ("phase" -> JsString(obj.productPrefix)))
//
//    override def read(json: JsValue): Phase = json.asJsObject.getFields("phase") match {
//      case Seq(JsString("WaitingPhase")) => WaitingPhase
//      case Seq(JsString("PlacementPhase")) => PlacementPhase
//      case Seq(JsString("Turn")) => json.convertTo[Turn]
//      case Seq(JsString("GameOver")) => json.convertTo[GameOver]
//    }
//  }
//
//  implicit val playerUpdateFormat = jsonFormat2(PlayerUpdate)
//  implicit val shootCommandFormat = jsonFormat3(ShootCommand)
//  implicit val placeCommandFormat = jsonFormat3(PlaceCommand)
//  implicit val stateCommandFormat = jsonFormat2(StateCommand)
//  implicit val shootReplyFormat = jsonFormat4(ShootReply)
//  implicit val placeReplyFormat = jsonFormat4(PlaceReply)
//  implicit val stateReplyFormat = jsonFormat5(StateReply)
//  implicit val phaseNotificationFormat = jsonFormat2(PhaseNotification)
//  implicit val playerDisconnectedFormat = jsonFormat2(PlayerDisconnected)
//
//  implicit val commandFormat = new RootJsonFormat[Command] {
//    override def write(obj: Command): JsValue = JsObject((obj match {
//      case pu: PlayerUpdate => pu.toJson
//      case sc: ShootCommand => sc.toJson
//      case pc: PlaceCommand => pc.toJson
//      case sc: StateCommand => sc.toJson
//      case sr: ShootReply => sr.toJson
//      case pr: PlaceReply => pr.toJson
//      case sr: StateReply => sr.toJson
//      case pn: PhaseNotification => pn.toJson
//      case pd: PlayerDisconnected => pd.toJson
//    }).asJsObject.fields + ("type" -> JsString(obj.productPrefix)))
//
//    override def read(json: JsValue): Command = json.asJsObject.getFields("type") match {
//      case Seq(JsString("PlayerUpdate")) => json.convertTo[PlayerUpdate]
//      case Seq(JsString("ShootCommand")) => json.convertTo[ShootCommand]
//      case Seq(JsString("PlaceCommand")) => json.convertTo[PlaceCommand]
//      case Seq(JsString("StateCommand")) => json.convertTo[StateCommand]
//      case Seq(JsString("ShootReply")) => json.convertTo[ShootReply]
//      case Seq(JsString("PlaceReply")) => json.convertTo[PlaceReply]
//      case Seq(JsString("StateReply")) => json.convertTo[StateReply]
//      case Seq(JsString("PhaseNotification")) => json.convertTo[PhaseNotification]
//      case Seq(JsString("PlayerDisconnected")) => json.convertTo[PlayerDisconnected]
//    }
//  }
//
//}
