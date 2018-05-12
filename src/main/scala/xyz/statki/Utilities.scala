package xyz.statki

import xyz.statki.Board.{Down, Placement, Position, Ship}
import xyz.statki.Protocol._
import io.circe._, io.circe.parser._, io.circe.syntax._

object Utilities {
  def main(args: Array[String]): Unit = {
    val examplePlacement = Placement(Ship(0,2), Position(0,0), Down)
    println("Example placement:")
    println(examplePlacement.asJson.noSpaces)
    val examplePlaceCommand = PlaceCommand(0, "Test", examplePlacement)
    println("Example place command:")
    println(examplePlaceCommand.asInstanceOf[Command].asJson.noSpaces)
  }
}
