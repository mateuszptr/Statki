package xyz.statki

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{Matchers, WordSpec}
import xyz.statki.Board.{Down, Placement, Position, Ship}
import xyz.statki.Game.PlacementPhase

class GameControllerTests extends WordSpec with Matchers {
  "GameController" should {
    "?" in {
      implicit val system = ActorSystem()
      val player0probe = TestProbe()
      val player1probe = TestProbe()
      val senderProbe = TestProbe()
      val gameController = system.actorOf(GameController.props())

      gameController.tell(PlayerConnected(0, "Test", player0probe.ref), senderProbe.ref)
      gameController.tell(PlayerConnected(1, "Test", player1probe.ref), senderProbe.ref)

      player0probe.expectMsg(PhaseNotification("Test", PlacementPhase))
      player1probe.expectMsg(PhaseNotification("Test", PlacementPhase))

      val placement = Placement(Ship(0,2), Position(0,0), Down)
      gameController.tell(PlaceCommand(0, "Test", placement), senderProbe.ref)
      player0probe.expectMsg(PlaceReply(0, "Test", placement, true))

    }
  }
}
