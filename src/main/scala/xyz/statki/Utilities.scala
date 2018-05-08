package xyz.statki

import xyz.statki.Board.{Down, Placement, Position, Ship}
import spray.json._

object Utilities extends JsonSupport {
  def main(args: Array[String]): Unit = {
    val examplePlacement = Placement(Ship(0,2), Position(0,0), Down)
    println("Example placement:")
    println(examplePlacement.toJson.toString)
    val examplePlaceCommand = PlaceCommand(0, "Test", examplePlacement)
    println("Example place command:")
    println(examplePlaceCommand.asInstanceOf[Command].toJson.toString)
  }
}
