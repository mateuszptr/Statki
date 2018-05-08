package xyz.statki

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Source}

class GameService(implicit val actorSystem: ActorSystem, implicit val actorMaterializer: ActorMaterializer) extends Directives {

  val wsRoute = (get & parameters('pid.as[Int], 'gid)) { (pid, gid) =>
    handleWebSocketMessages(???)
  }

  val gameControllerActor = actorSystem.actorOf(GameController.props)
  val playerActorSource = Source.actorRef[Command](5, OverflowStrategy.fail)

  def flow(pid: Int, gid: String): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(playerActorSource){ implicit builder => actorSrc =>

    ???
  })

}
