package xyz.statki

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable

class GameController extends Actor with ActorLogging {

  val idToActor: mutable.Map[(String, Int), ActorRef] = mutable.Map.empty
  val actorToId: mutable.Map[ActorRef, (String, Int)] = mutable.Map.empty

  val gidToActor: mutable.Map[String, ActorRef] = mutable.Map.empty


  override def receive: Receive = ???
}

object GameController {
  def props: Props = Props(new GameController)
}
