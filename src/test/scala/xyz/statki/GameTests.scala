package xyz.statki

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{Matchers, WordSpec}
import xyz.statki.Board._
import xyz.statki.Game.{GameOver, PlacementPhase, Turn}

import scala.concurrent.duration._

class GameTests extends WordSpec with Matchers {
  "Game" should {
    "be able to register joining players" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val game =  system.actorOf(Game.props("Test", probe.ref, 10, Set(Ship(0,2))))

      game.tell(PlayerUpdate(0, "Test"), probe.ref)
      game.tell(PlayerUpdate(1, "Test"), probe.ref)
      probe.expectMsg(PhaseNotification("Test", PlacementPhase))

    }
    "reject invalid players" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val game =  system.actorOf(Game.props("Test", probe.ref, 10, Set(Ship(0,2))))

      game.tell(PlayerUpdate(3, "Test"), probe.ref)
      game.tell(PlayerUpdate(4, "Test"), probe.ref)
      probe.expectNoMessage(100.millis)
    }
    "place ships" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val game =  system.actorOf(Game.props("Test", probe.ref, 10, Set(Ship(0,2),Ship(1,3))))

      game.tell(PlayerUpdate(0, "Test"), probe.ref)
      game.tell(PlayerUpdate(1, "Test"), probe.ref)
      probe.expectMsg(PhaseNotification("Test", PlacementPhase))

      var placement = Placement(Ship(0,2),Position(0,0),Down)
      game.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      placement = Placement(Ship(0,2),Position(0,0),Right)
      game.tell(PlaceCommand(1, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(1, "Test", placement, true))

    }
    "reach proper game" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val game =  system.actorOf(Game.props("Test", probe.ref, 10, Set(Ship(0,2))))

      game.tell(PlayerUpdate(0, "Test"), probe.ref)
      game.tell(PlayerUpdate(1, "Test"), probe.ref)
      probe.expectMsg(PhaseNotification("Test", PlacementPhase))

      var placement = Placement(Ship(0,2),Position(0,0),Down)
      game.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      placement = Placement(Ship(0,2),Position(0,0),Right)
      game.tell(PlaceCommand(1, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(1, "Test", placement, true))
      probe.expectMsg(PhaseNotification("Test", Turn(0)))

    }
    "hit and sink ships" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val game =  system.actorOf(Game.props("Test", probe.ref, 10, Set(Ship(0,2),Ship(1,3))))

      game.tell(PlayerUpdate(0, "Test"), probe.ref)
      game.tell(PlayerUpdate(1, "Test"), probe.ref)
      probe.expectMsg(PhaseNotification("Test", PlacementPhase))

      var placement = Placement(Ship(0,2),Position(0,0),Down)
      game.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      placement = Placement(Ship(1,3),Position(3,3),Down)
      game.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      placement = Placement(Ship(0,2),Position(0,0),Right)
      game.tell(PlaceCommand(1, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(1, "Test", placement, true))

      placement = Placement(Ship(1,3),Position(3,3),Right)
      game.tell(PlaceCommand(1, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(1, "Test", placement, true))
      probe.expectMsg(PhaseNotification("Test", Turn(0)))

      game.tell(ShootCommand(0, "Test", Position(0,0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0,0), Some(HitField(Ship(0,2)))))
      probe.expectMsg(PhaseNotification("Test", Turn(1)))

      game.tell(ShootCommand(1, "Test", Position(9,9)), probe.ref)
      probe.expectMsg(ShootReply(1, "Test", Position(9,9), Some(MissField)))
      probe.expectMsg(PhaseNotification("Test", Turn(0)))

      game.tell(ShootCommand(0, "Test", Position(1,0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(1,0), Some(SunkField(Ship(0,2)))))
      probe.expectMsg(PhaseNotification("Test", Turn(1)))

    }
    "reach GameOver" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val game =  system.actorOf(Game.props("Test", probe.ref, 10, Set(Ship(0,2))))

      game.tell(PlayerUpdate(0, "Test"), probe.ref)
      game.tell(PlayerUpdate(1, "Test"), probe.ref)
      probe.expectMsg(PhaseNotification("Test", PlacementPhase))

      var placement = Placement(Ship(0,2),Position(0,0),Down)
      game.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      placement = Placement(Ship(0,2),Position(0,0),Right)
      game.tell(PlaceCommand(1, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(1, "Test", placement, true))
      probe.expectMsg(PhaseNotification("Test", Turn(0)))

      game.tell(ShootCommand(0, "Test", Position(0,0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0,0), Some(HitField(Ship(0,2)))))
      probe.expectMsg(PhaseNotification("Test", Turn(1)))

      game.tell(ShootCommand(1, "Test", Position(9,9)), probe.ref)
      probe.expectMsg(ShootReply(1, "Test", Position(9,9), Some(MissField)))
      probe.expectMsg(PhaseNotification("Test", Turn(0)))

      game.tell(ShootCommand(0, "Test", Position(1,0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(1,0), Some(SunkField(Ship(0,2)))))
      probe.expectMsg(PhaseNotification("Test", Turn(1)))
      probe.expectMsg(PhaseNotification("Test", GameOver(1)))
    }
    "report state of boards" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val game =  system.actorOf(Game.props("Test", probe.ref, 10, Set(Ship(0,2))))

      game.tell(PlayerUpdate(0, "Test"), probe.ref)
      game.tell(PlayerUpdate(1, "Test"), probe.ref)
      probe.expectMsg(PhaseNotification("Test", PlacementPhase))

      var placement = Placement(Ship(0,2),Position(0,0),Down)
      game.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      placement = Placement(Ship(0,2),Position(0,0),Right)
      game.tell(PlaceCommand(1, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(1, "Test", placement, true))
      probe.expectMsg(PhaseNotification("Test", Turn(0)))

      game.tell(ShootCommand(0, "Test", Position(0,0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0,0), Some(HitField(Ship(0,2)))))
      probe.expectMsg(PhaseNotification("Test", Turn(1)))

      game.tell(ShootCommand(1, "Test", Position(9,9)), probe.ref)
      probe.expectMsg(ShootReply(1, "Test", Position(9,9), Some(MissField)))
      probe.expectMsg(PhaseNotification("Test", Turn(0)))

      val expectedPlayerBoard: Map[Position, Field] = Map(
        Position(0,0) -> ShipField(Ship(0,2)),
        Position(0,1) -> ShipField(Ship(0,2)),
        Position(9,9) -> MissField
      )
      val expectedEnemyBoard: Map[Position, Field] = Map(
        Position(0,0) -> HitField(Ship(0,2))
      )

      game.tell(StateCommand(0, "Test"), probe.ref)
      probe.expectMsg(StateReply(0, "Test", expectedPlayerBoard, expectedEnemyBoard, Turn(0)))
    }
  }


}
