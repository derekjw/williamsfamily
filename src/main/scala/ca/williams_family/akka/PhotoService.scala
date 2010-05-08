package ca.williams_family
package akka

import collection.immutable.{TreeMap,TreeSet}

import net.liftweb.common._
import Box._
import net.liftweb.util.Helpers._

import model._

import se.scalablesolutions.akka.stm.Transaction.Global._
import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.dispatch._
import Futures._
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.config._

import net.liftweb.json._

class RedisPhotoService extends PhotoService with RedisPhotoStorageFactory

abstract class PhotoService extends Actor with Logger {
  faultHandler = Some(OneForOneStrategy(5, 5000))
  trapExit = List(classOf[Exception])

  val storage: ActorID

  private var indexes: Set[ActorID] = Set()

  def registerIndex(index: ActorID) { registerIndex(List(index)) }

  def registerIndex(index: Iterable[ActorID]) {
    index.foreach{i =>
      startLink(i)
      indexes += i
    }
    reIndex(index)
  }

  def reIndex(index: Iterable[ActorID]) {
    logTime("ReIndexing photos")(((self !! GetPhotoIds) ?~ "Timed out").asA[List[String]].getOrElse(Nil).foreach{pId =>
      for {
        photo <- getPhoto(pId)
        idx <- index
      } {idx !!! SetPhoto(photo)}
    })
  }

  def countPhotos = ((self !! CountPhotos) ?~ "Timed out").asA[java.lang.Integer].map(_.intValue)

  def setPhoto(photo: Photo): Future[Boolean] = self !!! SetPhoto(photo)

  def getPhoto(id: String) =
    for {
      res <- ((self !! GetPhoto(id)) ?~ "Timed out" ~> 500).asA[Option[Photo]]
      photo <-res ?~ "Photo Not Found" ~> 404
    } yield photo

  def getPhotoTimeline(key: List[Int]): Box[PhotoTimeline] =
    for (res <- ((self !! GetPhotoTimeline(key)) ?~ "Timed out").asA[PhotoTimeline] ?~ "Invalid Response") yield res

  def getPhotoTimeline(year: Int = 0, month: Int = 0, day: Int = 0): Box[PhotoTimeline] = getPhotoTimeline(List(year,month,day).filterNot(_ == 0))

  def receive = {
    case CountPhotos => storage forward CountPhotos
    case msg: ForEachPhoto => storage forward msg
    case GetPhotoIds => storage forward GetPhotoIds
    case msg: SetPhoto =>
      storage forward msg
      indexes.foreach(_ !!! msg)
    case msg: GetPhoto => storage forward msg
    case msg: GetPhotos => storage forward msg
    case msg: GetPhotoTimeline => {
      val idx = indexes.find(i => classOf[PhotoTimelineIndex].isAssignableFrom(i.actorClass))
      Box(idx).foreach(_ forward msg)
      if (idx.isEmpty) reply(Failure("Index not found"))
    }
  }

  override def shutdown = {
    unlink(storage)
    storage.stop
    indexes.foreach {i =>
      unlink(i)
      i.stop
    }
  }

}
