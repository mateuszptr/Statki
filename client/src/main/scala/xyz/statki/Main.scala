package xyz.statki
import xyz.statki.Protocol._
import io.circe.syntax._
import akka.actor.Actor

object Main {
  def main(args: Array[String]): Unit = {
    println(Protocol.Placement(Ship(0, 2), Position(0, 0), Down).asJson.noSpaces)
  }
}
