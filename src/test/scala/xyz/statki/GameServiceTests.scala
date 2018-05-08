package xyz.statki

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.testkit.WSProbe
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import xyz.statki.Game.PlacementPhase
import spray.json._
import xyz.statki.Board.{Down, Placement, Position, Ship}

class GameServiceTests extends WordSpec with Matchers with ScalatestRouteTest with BeforeAndAfterAll with JsonSupport {

  override def afterAll(): Unit = cleanUp()

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

  }

}
