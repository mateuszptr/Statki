package xyz.statki

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import xyz.statki.Protocol._

class GameStateQuery(pid: Int, gid: String, gameActor: ActorRef, boards: Seq[ActorRef]) extends Actor with ActorLogging {

  var playerBoard: Option[Map[Position, Field]] = None
  var enemyBoard: Option[Map[Position, Field]] = None

  override def preStart(): Unit = {
    for (i <- 0 to 1) boards(i) ! StateCommand(i, gid)
  }

  override def receive: Receive = {
    case StateReply(rPid, `gid`, board, _, _) =>
      val enemy = 1 - pid
      rPid match {
        case `pid` =>
          playerBoard = Some(board)
        case `enemy` =>
          enemyBoard = Some(board.filter { v =>
            v match {
              case (k, ShipField(_)) => false
              case _ => true
            }
          })
        case _ => log.warning("Unknown player")
      }
      if (playerBoard.isDefined && enemyBoard.isDefined) {
        gameActor ! StateReply(pid, gid, playerBoard.get, enemyBoard.get, WaitingPhase)
        context.stop(self)
      }
  }
}

object GameStateQuery {
  def props(pid: Int, gid: String, gameActor: ActorRef, boards: Seq[ActorRef]): Props = Props(new GameStateQuery(pid, gid, gameActor, boards))
}
