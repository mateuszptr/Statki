package xyz.statki

import akka.actor.{Actor, ActorLogging, ActorRef}
import xyz.statki.Protocol._
import org.scalajs.dom.{CanvasRenderingContext2D, html}
import org.scalajs.dom

import scala.collection.mutable

object ClientGame {
  val tileDim = 50
  val borderWidth = 1
  val enemyBoardPos = 0
  val msgBoardPos = 500
  val playerBoardPos = 700

}

class ClientGame(pid: Int, gid: String, canvas: html.Canvas, wsActor: ActorRef) extends Actor with ActorLogging {
  import ClientGame._
  var phase: Phase = WaitingPhase

  var playerBoard: mutable.Map[Position, Field] = mutable.Map.empty
  var enemyBoard: mutable.Map[Position, Field] = mutable.Map.empty

  val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  def drawTile(pos: Position, field: Option[Field], boardPos: Int): Unit = {
    var tilePos = pos*tileDim + Position(0,boardPos)
    var dim = tileDim

    ctx.fillStyle = "white"
    ctx.fillRect(tilePos.x, tilePos.y, dim, dim)

    tilePos += Position(borderWidth, borderWidth)
    dim -= 2*borderWidth

    ctx.fillStyle = field match {
      case Some(MissField) => "#8888FF"
      case Some(HitField(_)) => "#FF0000"
      case Some(SunkField(_)) => "#880000"
      case Some(ShipField(_)) => "#AAAAAA"
      case None => "#000088"
    }
    ctx.fillRect(tilePos.x, tilePos.y, dim, dim)
  }

  def displayWaitMsg(): Unit = displayMsg("Waiting for other player")

  def displayMsg(text: String, style: scalajs.js.Any = "white"): Unit = {
    ctx.fillStyle = "black"
    ctx.fillRect(0, msgBoardPos, tileDim*10, playerBoardPos-msgBoardPos)

    ctx.fillStyle = style
    ctx.font = "Arial 100px"
    ctx.fillText(text, 0, msgBoardPos, tileDim*10)
  }

  def redrawBoards(): Unit = {
    for(x <- 0 until 10; y <- 0 until 10) {
      val pos = Position(x,y)
      drawTile(pos, enemyBoard.get(pos), enemyBoardPos)
      drawTile(pos, playerBoard.get(pos), playerBoardPos)
    }
  }

  def displayFailure(text: String): Unit = displayMsg(text, "red")

  def displayWinMsg(): Unit = displayMsg("You won!!!", "gold")

  def displayLoseMsg(): Unit = displayMsg("You lost :C", "blue")

  def refreshState(): Unit = {
    displayFailure("Refreshing state...")
    wsActor ! StateCommand(pid, gid)
    context.become(recvState)
  }

  def recvState: Receive = {
    case StateReply(`pid`, `gid`, pB, eB, ph) =>
      playerBoard = mutable.Map((pB ++ playerBoard).toSeq: _*)
      enemyBoard = mutable.Map((eB ++ enemyBoard).toSeq: _*)
      phase = ph
      redrawBoards()
      displayMsg("State refreshed")
      context become (phase match {
        case WaitingPhase =>
          displayMsg("Waiting for an enemy")
          recvWaiting
        case PlacementPhase =>
          displayMsg("Place ships")
          recvPlacement
        case Turn(`pid`) =>
          displayMsg("Your turn")
          recvPlayerTurn
        case Turn(_) =>
          displayMsg("Enemy turn")
          recvEnemyTurn
        case GameOver(loserPid) =>
          if(loserPid == pid) displayLoseMsg() else displayWinMsg()
          recvGameOver
      })
    case ShootReply(spid, `gid`, position, result) if spid != pid =>
      result match {
        case None =>
          log.warning("Empty enemy shot")
        case Some(SunkField(ship)) =>
          val updatedPositions = playerBoard.collect{case Position(x,y) -> HitField(`ship`) => Position(x,y) -> SunkField(ship)}.toList
          playerBoard ++= updatedPositions
          playerBoard += position -> SunkField(ship)
        case Some(field) =>
          playerBoard += position -> field
      }
  }

  def recvWaiting: Receive = {
    case PhaseNotification(`gid`, PlacementPhase) =>
      displayMsg("Place ships")
      context.become(recvPlacement)
  }

  def recvPlacement: Receive = {
    case PhaseNotification(`gid`, Turn(`pid`)) =>
      displayMsg("Your turn")
      context.become(recvPlayerTurn)
    case PhaseNotification(`gid`, Turn(_)) =>
      displayMsg("Enemy turn")
      context.become(recvEnemyTurn)
    case PlaceReply(`pid`, `gid`, placement, result) =>
      if(result) {
        playerBoard ++= placement.positions.map {pos => pos -> ShipField(placement.ship)}
        redrawBoards()
      } else {
        refreshState()
      }
    case pc@PlaceCommand(`pid`, `gid`, placement) =>
      val canPlace = placement.position.valid(10) && placement.positions.last.valid(10) && placement.positions.map(playerBoard.get).forall(_.isEmpty)
      if(canPlace)
        wsActor forward pc
      else {
        displayFailure("Can't place a ship there")
      }
  }

  def recvPlayerTurn: Receive = {
    case PhaseNotification(`gid`, Turn(enemyPid)) if enemyPid != pid =>
      displayMsg("Enemy turn")
      context.become(recvEnemyTurn)
    case PhaseNotification(`gid`, GameOver(loserPid)) =>
      if(loserPid == pid) displayLoseMsg() else displayWinMsg()
      context.become(recvGameOver)
    case ShootReply(`pid`, `gid`, position, result) =>
      result match {
        case None =>
          refreshState()
        case Some(SunkField(ship)) =>
          val updatedPositions = enemyBoard.collect{case Position(x,y) -> HitField(`ship`) => Position(x,y) -> SunkField(ship)}.toList
          enemyBoard ++= updatedPositions
          enemyBoard += position -> SunkField(ship)
        case Some(field) =>
          enemyBoard += position -> field
      }
    case sc@ShootCommand(`pid`, `gid`, position) =>
      val canShoot = position.valid(10) && enemyBoard.get(position).isEmpty
      if(canShoot)
        wsActor forward sc
      else
        displayFailure("Can't shoot there")
  }

  def recvEnemyTurn: Receive = {
    case PhaseNotification(`gid`, Turn(`pid`)) =>
      displayMsg("Your turn")
      context.become(recvPlayerTurn)
    case PhaseNotification(`gid`, GameOver(loserPid)) =>
      if(loserPid == pid) displayLoseMsg() else displayWinMsg()
    case ShootReply(spid, `gid`, position, result) =>
      result match {
        case None =>
          log.warning("Empty enemy shot")
        case Some(SunkField(ship)) =>
          val updatedPositions = playerBoard.collect{case Position(x,y) -> HitField(`ship`) => Position(x,y) -> SunkField(ship)}.toList
          playerBoard ++= updatedPositions
          playerBoard += position -> SunkField(ship)
        case Some(field) =>
          playerBoard += position -> field
      }
  }

  def recvGameOver: Receive = Actor.emptyBehavior

  override def preStart(): Unit = {
    wsActor ! StateCommand(pid, gid)
  }

  override def receive: Receive = recvState
}
