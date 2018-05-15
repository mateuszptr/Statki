package xyz.statki
import xyz.statki.Protocol._
import io.circe.syntax._
import akka.actor.{ Actor, ActorSystem }
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement
import java.net.URLDecoder

import scala.scalajs.js

object Main {
  def parseUrlParameters(url: String): Map[String, String] = {
    url.split("&").map(v => {
      val m = v.split("=", 2).map(s => URLDecoder.decode(s, "UTF-8"))
      m(0) -> m(1)
    }).toMap
  }

  def main(args: Array[String]): Unit = {

    val canvas = dom.document.getElementById("canv").asInstanceOf[HTMLCanvasElement]
    val url = dom.document.documentURI
    println(url)

    val rawParameters = dom.document.documentURI.dropWhile { c => c != '?' }.drop(1)
    val parameters = parseUrlParameters(rawParameters)
    val pid = parameters("pid").toInt
    val gid = parameters("gid")

    val system = ActorSystem()
    val gameActor = system.actorOf(ClientGame.props(pid, gid, canvas, "ws://localhost:9999?" ++ rawParameters))

  }
}
