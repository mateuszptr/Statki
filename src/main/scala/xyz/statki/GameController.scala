package xyz.statki

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import xyz.statki.Board.Ship

import scala.collection.mutable

class GameController(dim: Int = 10, ships: Set[Ship] = Set(
  Ship(0,2),
  Ship(1,3),
  Ship(2,3),
  Ship(3,4),
  Ship(4,5)
)) extends Actor with ActorLogging {

  val idToActor: mutable.Map[(String, Int), ActorRef] = mutable.Map.empty
  val actorToId: mutable.Map[ActorRef, (String, Int)] = mutable.Map.empty

  val gidToActor: mutable.Map[String, ActorRef] = mutable.Map.empty
  val actorToGid: mutable.Map[ActorRef, String] = mutable.Map.empty

  def handleInputCommand(command: Command, gid: String): Unit = {
    if(gidToActor.get(gid).isDefined) {
      gidToActor(gid) ! command
    } else
      log.warning("Got request for invalid game")
  }

  def handleOutputCommand(command: Command, pid: Int, gid: String): Unit = {
    if(idToActor.get(gid -> pid).isDefined) {
      idToActor(gid -> pid) ! command
    } else
      log.warning("Got reply to (probably) disconnected player")
  }

  override def receive: Receive = {
    case PlayerConnected(pid, gid, actorRef) =>
      idToActor += (gid -> pid) -> actorRef
      actorToId += actorRef -> (gid, pid)
      val gameActor = gidToActor.getOrElseUpdate(gid, {
        val actor = context.actorOf(Game.props(gid, self, dim, ships))
        context.watch(actor)
        actorToGid += actor -> gid
        actor
      })

      gameActor ! PlayerUpdate(pid, gid)

    case PlayerDisconnected(pid, gid) =>
      val actor = idToActor(gid -> pid)
      idToActor -= gid -> pid
      actorToId -= actor
      log.warning(s"Player ($gid,$pid) Disconnected")

    case Terminated(groupActor) =>
      val gid = actorToGid(groupActor)
      actorToGid -= groupActor
      gidToActor -= gid

    case pc@PlaceCommand(pid, gid, placement) =>
      handleInputCommand(pc, gid)

    case sc @ ShootCommand(pid, gid, position) =>
      handleInputCommand(sc, gid)

    case sc @ StateCommand(pid, gid) =>
      handleInputCommand(sc, gid)

    case pr @ PlaceReply(pid, gid, placement, result) =>
      handleOutputCommand(pr, pid, gid)

    case sr @ ShootReply(pid, gid, position, result) =>
      handleOutputCommand(sr, pid, gid)
      handleOutputCommand(sr, 1-pid, gid)

    case sr @ StateReply(pid, gid, _, _, _) =>
      handleOutputCommand(sr, pid, gid)
    case pn @ PhaseNotification(gid, _) =>
      handleOutputCommand(pn, 0, gid)
      handleOutputCommand(pn, 1, gid)

  }

}

object GameController {
  def props(dim: Int = 10, ships: Set[Ship] = Set(
    Ship(0,2),
    Ship(1,3),
    Ship(2,3),
    Ship(3,4),
    Ship(4,5)
  )): Props = Props(new GameController(dim, ships))
}
