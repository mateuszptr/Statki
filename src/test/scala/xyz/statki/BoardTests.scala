package xyz.statki

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{Matchers, WordSpec}
import xyz.statki.Board._
import xyz.statki.Game.{GameOver, Turn, WaitingPhase}

import scala.concurrent.duration._

class BoardTests extends WordSpec with Matchers {
  "Board (Placement)" should {
    "drop wrong pid and/or gid" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2))))

      val placement = Placement(Ship(0, 2), Position(0, 0), Down)
      board.tell(PlaceCommand(1, "Test2", placement), probe.ref)
      probe.expectNoMessage(100.millis)
      board.tell(PlaceCommand(1, "Test", placement), probe.ref)
      probe.expectNoMessage(100.millis)
      board.tell(PlaceCommand(0, "Test2", placement), probe.ref)
      probe.expectNoMessage(100.millis)
    }
    "be able to place the ship" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2))))

      val placement = Placement(Ship(0, 2), Position(0, 0), Down)
      board.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))
    }
    "reject placing a ship twice" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2))))

      val placement1 = Placement(Ship(0, 2), Position(0, 0), Down)
      val placement2 = Placement(Ship(0, 2), Position(0, 0), Right)

      board.tell(PlaceCommand(0, "Test", placement1), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement1, true))
      probe.expectMsg(PhaseNotification("Test", Turn(0)))

      board.tell(PlaceCommand(0, "Test", placement2), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement2, false))
    }
    "reject placing a ship outside the board" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2), Ship(1, 2))))

      var placement = Placement(Ship(0, 2), Position(9, 9), Down)
      board.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, false))

      placement = Placement(Ship(1, 2), Position(-1, -1), Down)
      board.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, false))
    }
    "reject colliding ships" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2), Ship(1, 2))))

      var placement = Placement(Ship(0, 2), Position(0, 0), Down)
      board.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      placement = Placement(Ship(1, 2), Position(0, 0), Right)
      board.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, false))
    }
  }
  "Board (Shooting)" should {
    "be able to miss" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2), Ship(1, 2))))

      board.tell(ShootCommand(0, "Test", Position(0, 0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 0), Some(MissField)))

      board.tell(ShootCommand(0, "Test", Position(1, 1)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(1, 1), Some(MissField)))
    }
    "report missing same place twice" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2), Ship(1, 2))))

      board.tell(ShootCommand(0, "Test", Position(0, 0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 0), Some(MissField)))

      board.tell(ShootCommand(0, "Test", Position(0, 0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 0), None))
    }
    "report hitting place outside the board" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2), Ship(1, 2))))

      board.tell(ShootCommand(0, "Test", Position(-1, -1)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(-1, -1), None))

      board.tell(ShootCommand(0, "Test", Position(10, 10)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(10, 10), None))
    }
    "be able to hit a ship" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2), Ship(1, 2))))

      val placement = Placement(Ship(0, 2), Position(0, 0), Down)
      board.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      board.tell(ShootCommand(0, "Test", Position(0, 0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 0), Some(HitField(Ship(0, 2)))))
    }
    "be able to sink a ship" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2), Ship(1, 2))))

      val placement = Placement(Ship(0, 2), Position(0, 0), Down)
      board.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      board.tell(ShootCommand(0, "Test", Position(0, 0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 0), Some(HitField(Ship(0, 2)))))

      board.tell(ShootCommand(0, "Test", Position(0, 1)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 1), Some(SunkField(Ship(0, 2)))))
    }
    "report hitting same ship segment twice" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2), Ship(1, 2))))

      val placement = Placement(Ship(0, 2), Position(0, 0), Down)
      board.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      board.tell(ShootCommand(0, "Test", Position(0, 0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 0), Some(HitField(Ship(0, 2)))))

      board.tell(ShootCommand(0, "Test", Position(0, 0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 0), None))
    }
    "report correct states" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2), Ship(1, 2))))

      val placement = Placement(Ship(0, 2), Position(0, 0), Down)
      board.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))

      var expectedMap: Map[Position, Field] = Map(
        Position(0,0) -> ShipField(Ship(0,2)),
        Position(0,1) -> ShipField(Ship(0,2))
      )
      board.tell(StateCommand(0, "Test"), probe.ref)
      probe.expectMsg(StateReply(0, "Test", expectedMap, Map.empty, WaitingPhase))

      board.tell(ShootCommand(0, "Test", Position(0, 0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 0), Some(HitField(Ship(0, 2)))))

      expectedMap = Map(
        Position(0,0) -> HitField(Ship(0,2)),
        Position(0,1) -> ShipField(Ship(0,2))
      )
      board.tell(StateCommand(0, "Test"), probe.ref)
      probe.expectMsg(StateReply(0, "Test", expectedMap, Map.empty, WaitingPhase))

      board.tell(ShootCommand(0, "Test", Position(0, 1)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 1), Some(SunkField(Ship(0, 2)))))

      expectedMap = Map(
        Position(0,0) -> SunkField(Ship(0,2)),
        Position(0,1) -> SunkField(Ship(0,2))
      )
      board.tell(StateCommand(0, "Test"), probe.ref)
      probe.expectMsg(StateReply(0, "Test", expectedMap, Map.empty, WaitingPhase))
    }
    "report the game is over" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val board = system.actorOf(Board.props(0, "Test", 10, Set(Ship(0, 2))))

      val placement = Placement(Ship(0, 2), Position(0, 0), Down)
      board.tell(PlaceCommand(0, "Test", placement), probe.ref)
      probe.expectMsg(PlaceReply(0, "Test", placement, true))
      probe.expectMsg(PhaseNotification("Test", Turn(0)))

      board.tell(ShootCommand(0, "Test", Position(0, 0)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 0), Some(HitField(Ship(0, 2)))))

      board.tell(ShootCommand(0, "Test", Position(0, 1)), probe.ref)
      probe.expectMsg(ShootReply(0, "Test", Position(0, 1), Some(SunkField(Ship(0, 2)))))
      probe.expectMsg(PhaseNotification("Test", GameOver(0)))
    }
  }

}
