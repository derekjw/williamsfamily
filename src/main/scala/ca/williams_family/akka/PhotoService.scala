package ca.williams_family
package akka

import collection.SortedSet

import net.liftweb.common._
import Box._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.dispatch._
import Futures._
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.config._

import net.liftweb.json._

abstract class PhotoService extends Actor {
  faultHandler = Some(OneForOneStrategy(5, 5000))
  trapExit = List(classOf[Exception])

  val storage: PhotoStorage

  var indexes: Set[PhotoIndex] = Set()

  def registerIndex(index: PhotoIndex) { registerIndex(List(index)) }

  def registerIndex(index: Iterable[PhotoIndex]) {
    index.foreach{i =>
      startLink(i)
      indexes += i
    }
    reIndex(index)
  }

  def reIndex(index: Iterable[PhotoIndex]) {
    ((this !! GetPhotoIds) ?~ "Timed out").asA[List[String]].getOrElse(Nil).map(i => this !!! GetPhoto(i)).foreach{f =>
      f.await
      for {
        photo <- Box(f.result).asA[Photo]
        idx <- index
      } {idx ! SetPhoto(photo)}
    }
  }

  def countPhotos = ((this !! CountPhotos) ?~ "Timed out").asA[java.lang.Integer].map(_.intValue)

  def setPhoto(photo: Photo): Future = this !!! SetPhoto(photo)

  def getPhoto(id: String) =
    for {
      photo <- ((this !! GetPhoto(id)) ?~ "Timed out" ~> 500).asA[Photo] ?~ "Photo Not Found" ~> 404
    } yield photo

  def getPhotosByDate(key: List[Int]): Box[SortedSet[String]] =
    for {
      res <- ((this !! GetPhotosByDate(key)) ?~ "Timed out").asA[SortedSet[String]] ?~ "Invalid Response"
    } yield res

  def receive = {
    case CountPhotos => storage forward CountPhotos
    case GetPhotoIds => storage forward GetPhotoIds
    case msg: SetPhoto => {
      storage forward msg
      indexes.foreach(_ ! msg)
    }
    case msg: GetPhoto => storage forward msg
    case msg: GetPhotos => storage forward msg
    case msg: GetPhotosByDate => {
      val idx = indexes.find{
        case i: PhotoDateIndex => true
        case _ => false
      }
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
