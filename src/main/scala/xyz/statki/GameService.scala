package xyz.statki

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import spray.json._

class GameService(implicit val actorSystem: ActorSystem, implicit val actorMaterializer: ActorMaterializer) extends Directives with JsonSupport {

  val wsRoute = (get & parameters('pid.as[Int], 'gid)) { (pid, gid) =>
    handleWebSocketMessages(flow(pid, gid))
  }

  val gameControllerActor = actorSystem.actorOf(GameController.props())
  val playerActorSource = Source.actorRef[Command](5, OverflowStrategy.fail)

  def flow(pid: Int, gid: String): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(playerActorSource){ implicit builder => actorSrc =>
    import GraphDSL.Implicits._

    val materialization = builder.materializedValue.map(playerActorRef => PlayerConnected(pid, gid, playerActorRef))
    val merge = builder.add(Merge[Command](2))

    val messageToCommandFlow = builder.add(Flow[Message].collect {
      case TextMessage.Strict(text) => text.parseJson.convertTo[Command]
    })

    val commandToMessageFlow = builder.add(Flow[Command].map { command =>
      TextMessage(command.toJson.toString)
    })

    val gameControllerActorSink = Sink.actorRef[Command](gameControllerActor, PlayerDisconnected(pid, gid))

    materialization ~> merge ~> gameControllerActorSink
    messageToCommandFlow ~> merge

    actorSrc ~> commandToMessageFlow

    FlowShape(messageToCommandFlow.in, commandToMessageFlow.out)
  })

}
