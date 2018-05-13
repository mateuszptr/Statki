package xyz.statki

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.circe._
import io.circe.generic.JsonCodec
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import xyz.statki.Game._
import xyz.statki.Protocol._


object Game {



  def props(gid: String, controller: ActorRef, dim: Int, ships: Set[Ship]): Props = Props(new Game(gid, controller, dim, ships))

}

class Game(gid: String, controller: ActorRef, dim: Int, ships: Set[Ship]) extends Actor with ActorLogging {

  var gamePhase: Phase = WaitingPhase

  var waitingForPlayers: Set[Int] = Set(0,1)
  var placingPlayers: Set[Int] = Set.empty

  val boards = Seq(
    context.actorOf(Board.props(0,gid,dim,ships)),
    context.actorOf(Board.props(1,gid,dim,ships))
  )

  override def receive: Receive = {
    case PlayerUpdate(pid, `gid`) if gamePhase == WaitingPhase =>
      waitingForPlayers -= pid
      placingPlayers += pid
      if(waitingForPlayers.isEmpty) {
        gamePhase = PlacementPhase
        controller ! PhaseNotification(gid, gamePhase)
      }

    case PlaceCommand(pid, `gid`, placement) => gamePhase match {
      case PlacementPhase =>
        boards(pid) ! PlaceCommand(pid, gid, placement)
      case _ =>
        log.warning("Trying to place ship out of phase")
    }

    case ShootCommand(pid, `gid`, position)=> gamePhase match {
      case Turn(`pid`) => boards(1-pid) ! ShootCommand(1-pid,gid,position)
      case Turn(_) => log.warning("Trying to shoot out of turn")
      case _ => log.warning("Trying to shoot out of phase")
    }

    case pr@PlaceReply(pid, `gid`, placement, result) => gamePhase match {
      case PlacementPhase =>
        controller ! pr
      case _ =>
        log.error(s"Got $pr out of phase")
    }

    case sr@ShootReply(pid, `gid`, position, result) =>
      val shooterPid = 1-pid
      gamePhase match {
      case Turn(`shooterPid`) =>
        controller ! ShootReply(shooterPid, gid, position, result)
        result match {
          case Some(_) =>
            gamePhase = Turn(pid)
            controller ! PhaseNotification(gid, gamePhase)
          case None => log.warning("Invalid shot")
        }
      case Turn(_) =>
        log.error(s"Got $sr out of turn")
      case _ =>
        log.error(s"Got $sr out of phase")
    }

    case pn@PhaseNotification(`gid`, Turn(pid)) => gamePhase match {
      case PlacementPhase =>
        placingPlayers -= pid
        if(placingPlayers.isEmpty) {
          gamePhase = Turn(0)
          controller ! PhaseNotification(gid, gamePhase)
        }
      case _ =>
        log.error(s"Got $pn out of phase")
    }

    case pn@ PhaseNotification(`gid`, GameOver(loserPid)) => gamePhase match {
      case Turn(`loserPid`) =>
        controller ! pn
        context.stop(self)

      case Turn(_) =>
        log.error(s"Got $pn out of turn")

      case _ =>
        log.error(s"Got $pn out of phase")
    }

    case StateCommand(pid, `gid`) =>
      val query = context.actorOf(GameStateQuery.props(pid, gid, self, boards))

    case StateReply(pid, `gid`, playerBoard, enemyBoard, _) =>
      controller ! StateReply(pid, gid, playerBoard, enemyBoard, gamePhase)

    case c: Command => log.warning(s"Invalid command $c. Dropping.")
  }
}
