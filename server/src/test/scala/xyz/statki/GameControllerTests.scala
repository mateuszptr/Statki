package xyz.statki

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{ Matchers, WordSpec }
import xyz.statki.Protocol._

class GameControllerTests extends WordSpec with Matchers {
  "GameController" should {
    "reach placement phase" in {
      implicit val system = ActorSystem()
      val player0probe = TestProbe()
      val player1probe = TestProbe()
      val senderProbe = TestProbe()
      val gameController = system.actorOf(GameController.props())

      gameController.tell(PlayerConnected(0, "Test", player0probe.ref), senderProbe.ref)
      gameController.tell(PlayerConnected(1, "Test", player1probe.ref), senderProbe.ref)

      player0probe.expectMsg(PhaseNotification("Test", PlacementPhase))
      player1probe.expectMsg(PhaseNotification("Test", PlacementPhase))

      val placement = Placement(Ship(0, 2), Position(0, 0), Down)
      gameController.tell(PlaceCommand(0, "Test", placement), senderProbe.ref)
      player0probe.expectMsg(PlaceReply(0, "Test", placement, true))

    }
    "reach turn of player 0" in {
      implicit val system = ActorSystem()
      val player0probe = TestProbe()
      val player1probe = TestProbe()
      val senderProbe = TestProbe()
      val gameController = system.actorOf(GameController.props(10, Set(Ship(0, 2))))

      gameController.tell(PlayerConnected(0, "Test", player0probe.ref), senderProbe.ref)
      gameController.tell(PlayerConnected(1, "Test", player1probe.ref), senderProbe.ref)

      player0probe.expectMsg(PhaseNotification("Test", PlacementPhase))
      player1probe.expectMsg(PhaseNotification("Test", PlacementPhase))

      var placement = Placement(Ship(0, 2), Position(0, 0), Down)
      gameController.tell(PlaceCommand(0, "Test", placement), senderProbe.ref)
      player0probe.expectMsg(PlaceReply(0, "Test", placement, true))

      placement = Placement(Ship(0, 2), Position(0, 0), Right)
      gameController.tell(PlaceCommand(1, "Test", placement), senderProbe.ref)
      player1probe.expectMsg(PlaceReply(1, "Test", placement, true))

      player0probe.expectMsg(PhaseNotification("Test", Turn(0)))
      player1probe.expectMsg(PhaseNotification("Test", Turn(0)))
    }
    "reach GameOver" in {
      implicit val system = ActorSystem()
      val player0probe = TestProbe()
      val player1probe = TestProbe()
      val senderProbe = TestProbe()
      val gameController = system.actorOf(GameController.props(10, Set(Ship(0, 2))))

      gameController.tell(PlayerConnected(0, "Test", player0probe.ref), senderProbe.ref)
      gameController.tell(PlayerConnected(1, "Test", player1probe.ref), senderProbe.ref)

      player0probe.expectMsg(PhaseNotification("Test", PlacementPhase))
      player1probe.expectMsg(PhaseNotification("Test", PlacementPhase))

      var placement = Placement(Ship(0, 2), Position(0, 0), Down)
      gameController.tell(PlaceCommand(0, "Test", placement), senderProbe.ref)
      player0probe.expectMsg(PlaceReply(0, "Test", placement, true))

      placement = Placement(Ship(0, 2), Position(0, 0), Right)
      gameController.tell(PlaceCommand(1, "Test", placement), senderProbe.ref)
      player1probe.expectMsg(PlaceReply(1, "Test", placement, true))

      player0probe.expectMsg(PhaseNotification("Test", Turn(0)))
      player1probe.expectMsg(PhaseNotification("Test", Turn(0)))

      gameController.tell(ShootCommand(0, "Test", Position(0, 0)), player0probe.ref)
      player0probe.expectMsg(ShootReply(0, "Test", Position(0, 0), Some(HitField(Ship(0, 2)))))
      player1probe.expectMsg(ShootReply(0, "Test", Position(0, 0), Some(HitField(Ship(0, 2)))))

      player0probe.expectMsg(PhaseNotification("Test", Turn(1)))
      player1probe.expectMsg(PhaseNotification("Test", Turn(1)))

      gameController.tell(ShootCommand(1, "Test", Position(0, 0)), player1probe.ref)
      player1probe.expectMsg(ShootReply(1, "Test", Position(0, 0), Some(HitField(Ship(0, 2)))))
      player0probe.expectMsg(ShootReply(1, "Test", Position(0, 0), Some(HitField(Ship(0, 2)))))

      player0probe.expectMsg(PhaseNotification("Test", Turn(0)))
      player1probe.expectMsg(PhaseNotification("Test", Turn(0)))

      gameController.tell(ShootCommand(0, "Test", Position(1, 0)), player0probe.ref)
      player0probe.expectMsg(ShootReply(0, "Test", Position(1, 0), Some(SunkField(Ship(0, 2)))))
      player1probe.expectMsg(ShootReply(0, "Test", Position(1, 0), Some(SunkField(Ship(0, 2)))))

      player0probe.expectMsg(PhaseNotification("Test", Turn(1)))
      player1probe.expectMsg(PhaseNotification("Test", Turn(1)))

      player0probe.expectMsg(PhaseNotification("Test", GameOver(1)))
      player1probe.expectMsg(PhaseNotification("Test", GameOver(1)))
    }
  }
}
