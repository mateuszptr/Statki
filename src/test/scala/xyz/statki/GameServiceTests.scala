package xyz.statki

import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
//import spray.json._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import xyz.statki.Board._
import xyz.statki.Game.{GameOver, PlacementPhase, Turn}
import xyz.statki.Protocol._

class GameServiceTests extends WordSpec with Matchers with ScalatestRouteTest with BeforeAndAfterAll {

  override def afterAll(): Unit = cleanUp()


  def msgFromClient(client: WSProbe): Command = decode[Command](client.expectMessage().asTextMessage.getStrictText).toOption.get
  def clientSendMsg(client: WSProbe, msg: Command): Unit = client.sendMessage(msg.asInstanceOf[Command].asJson.toString())

  implicit val dim = 10
  implicit val ships = Set(Ship(0,2))

  "GameService" should {
    "be created" in {
      new GameService()
    }
    "reach gameover" in {
      val gameService = new GameService()
      val player0Client = WSProbe()
      val player1Client = WSProbe()

      val ws0 = WS(s"/?pid=0&gid=Test", player0Client.flow) ~> gameService.wsRoute

      val ws1 = WS(s"/?pid=1&gid=Test", player1Client.flow) ~> gameService.wsRoute

      msgFromClient(player0Client) shouldBe PhaseNotification("Test", PlacementPhase)
      msgFromClient(player1Client) shouldBe PhaseNotification("Test", PlacementPhase)

      clientSendMsg(player1Client, PlaceCommand(1, "Test", Placement(Ship(0, 2), Position(0, 0), Right)))
      msgFromClient(player1Client) shouldBe PlaceReply(1, "Test", Placement(Ship(0, 2), Position(0, 0), Right), true)

      clientSendMsg(player0Client, PlaceCommand(0, "Test", Placement(Ship(0, 2), Position(0, 0), Down)))
      msgFromClient(player0Client) shouldBe PlaceReply(0, "Test", Placement(Ship(0, 2), Position(0, 0), Down), true)


      msgFromClient(player0Client) shouldBe PhaseNotification("Test", Turn(0))
      msgFromClient(player1Client) shouldBe PhaseNotification("Test", Turn(0))

      clientSendMsg(player0Client, ShootCommand(0, "Test", Position(0,0)))
      msgFromClient(player0Client) shouldBe ShootReply(0, "Test", Position(0,0), Some(HitField(Ship(0,2))))
      msgFromClient(player1Client) shouldBe ShootReply(0, "Test", Position(0,0), Some(HitField(Ship(0,2))))

      msgFromClient(player0Client) shouldBe PhaseNotification("Test", Turn(1))
      msgFromClient(player1Client) shouldBe PhaseNotification("Test", Turn(1))

      clientSendMsg(player1Client, ShootCommand(1, "Test", Position(0,0)))
      msgFromClient(player0Client) shouldBe ShootReply(1, "Test", Position(0,0), Some(HitField(Ship(0,2))))
      msgFromClient(player1Client) shouldBe ShootReply(1, "Test", Position(0,0), Some(HitField(Ship(0,2))))

      msgFromClient(player0Client) shouldBe PhaseNotification("Test", Turn(0))
      msgFromClient(player1Client) shouldBe PhaseNotification("Test", Turn(0))

      clientSendMsg(player0Client, ShootCommand(0, "Test", Position(1,0)))
      msgFromClient(player0Client) shouldBe ShootReply(0, "Test", Position(1,0), Some(SunkField(Ship(0,2))))
      msgFromClient(player1Client) shouldBe ShootReply(0, "Test", Position(1,0), Some(SunkField(Ship(0,2))))

      msgFromClient(player0Client) shouldBe PhaseNotification("Test", Turn(1))
      msgFromClient(player1Client) shouldBe PhaseNotification("Test", Turn(1))

      msgFromClient(player0Client) shouldBe PhaseNotification("Test", GameOver(1))
      msgFromClient(player1Client) shouldBe PhaseNotification("Test", GameOver(1))

    }

  }

}
