package xyz.statki

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{Matchers, WordSpec}

class GameControllerTests extends WordSpec with Matchers {
  "GameController" should {
    "?" in {
      implicit val system = ActorSystem()
      val probe = TestProbe()
      val gameController = system.actorOf(GameController.props)


    }
  }
}
