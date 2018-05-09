package xyz.statki

import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import spray.json._
import xyz.statki.Board._
import xyz.statki.Game.{GameOver, PlacementPhase, Turn}

class GameServiceTests extends WordSpec with Matchers with ScalatestRouteTest with BeforeAndAfterAll with JsonSupport {

  override def afterAll(): Unit = cleanUp()


  def msgFromClient(client: WSProbe) = client.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[Command]
  def clientSendMsg(client: WSProbe, msg: Command): Unit = client.sendMessage(msg.asInstanceOf[Command].toJson.toString)

  implicit val dim = 10
  implicit val ships = Set(Ship(0,2))

  "GameService" should {
    "be created" in {
      new GameService()
    }

    "reach placement phase" in {
      val gameService = new GameService()
      val player0Client = WSProbe()
      val player1Client = WSProbe()

      val ws0 = WS(s"/?pid=0&gid=Test", player0Client.flow) ~> gameService.wsRoute

      val ws1 = WS(s"/?pid=1&gid=Test", player1Client.flow) ~> gameService.wsRoute

      ws0 ~> check {
        val msg = player0Client.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[Command]
        msg shouldBe PhaseNotification("Test", PlacementPhase)
      }


      ws1 ~> check {
        val msg2 = player1Client.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[Command]
        msg2 shouldBe PhaseNotification("Test", PlacementPhase)

        player1Client.sendMessage(PlaceCommand(1, "Test", Placement(Ship(0, 2), Position(0, 0), Down)).asInstanceOf[Command].toJson.toString)
        val msg3 = player1Client.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[Command]
        msg3 shouldBe PlaceReply(1, "Test", Placement(Ship(0, 2), Position(0, 0), Down), true)
      }
    }

    "reach gameover" in {
      val gameService = new GameService()
      val player0Client = WSProbe()
      val player1Client = WSProbe()

      val ws0 = WS(s"/?pid=0&gid=Test", player0Client.flow) ~> gameService.wsRoute

      val ws1 = WS(s"/?pid=1&gid=Test", player1Client.flow) ~> gameService.wsRoute


      val msg = player0Client.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[Command]
      msg shouldBe PhaseNotification("Test", PlacementPhase)

      val msg2 = player1Client.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[Command]
      msg2 shouldBe PhaseNotification("Test", PlacementPhase)

      player1Client.sendMessage(PlaceCommand(1, "Test", Placement(Ship(0, 2), Position(0, 0), Right)).asInstanceOf[Command].toJson.toString)
      val msg3 = player1Client.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[Command]
      msg3 shouldBe PlaceReply(1, "Test", Placement(Ship(0, 2), Position(0, 0), Right), true)

      player0Client.sendMessage(PlaceCommand(0, "Test", Placement(Ship(0, 2), Position(0, 0), Down)).asInstanceOf[Command].toJson.toString)
      val msg4 = player0Client.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[Command]
      msg4 shouldBe PlaceReply(0, "Test", Placement(Ship(0, 2), Position(0, 0), Down), true)


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
