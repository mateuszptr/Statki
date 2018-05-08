package xyz.statki

import akka.http.scaladsl.testkit.WSProbe
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import xyz.statki.Game.PlacementPhase

class GameServiceTests extends WordSpec with Matchers with ScalatestRouteTest with BeforeAndAfterAll {

  override def afterAll(): Unit = cleanUp()

  "GameService" should {
    "be created" in {
      new GameService()
    }

    "reach placement phase" in {
      val gameService = new GameService()
      val player0Client = WSProbe()
      val player1Client = WSProbe()

      WS(s"/?pid=0&gid=Test", player0Client.flow) ~> gameService.wsRoute ~> check {
        ()
      }

      WS(s"/?pid=1&gid=Test", player1Client.flow) ~> gameService.wsRoute ~> check {

      }
    }

  }

}
