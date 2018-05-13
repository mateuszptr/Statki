package xyz.statki

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import xyz.statki.Protocol._

import scala.io.StdIn

object Server {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    implicit val dim = 10
    implicit val ships = Set(
      Ship(0,2),
      Ship(1,3),
      Ship(2,3),
      Ship(3,4),
      Ship(4,5)
    )

    val gameService = new GameService()
    val bindingFuture = Http().bindAndHandle(gameService.wsRoute, "localhost", 9999)
    println("Server running..")



    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
