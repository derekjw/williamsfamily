package ca.williams_family
package akka

import collection.immutable.{TreeMap,TreeSet}

import net.liftweb.common._
import Box._
import net.liftweb.util.Helpers._

import model._

import se.scalablesolutions.akka.actor._
import Actor._
import se.scalablesolutions.akka.dispatch._
import Futures._
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.config._

import net.liftweb.json._

class RedisPhotoService extends PhotoService
with RedisPhotoStorageFactory
with RedisPhotoTimelineIndexFactory

abstract class PhotoService extends Transactor with Logger {
  self.faultHandler = Some(AllForOneStrategy(5, 5000))
  self.trapExit = List(classOf[Exception])

  val storage: ActorRef

  val timelineIndex: ActorRef

  def receive = {
    case CountPhotos => storage forward CountPhotos
    case GetPhotoIds => storage forward GetPhotoIds
    case msg: SetPhoto =>
      storage ! msg
      timelineIndex ! msg
    case msg: GetPhoto => storage forward msg
    case msg: GetPhotos => storage forward msg
    case msg: GetPhotoTimeline => timelineIndex forward msg
  }

  override def shutdown = self.shutdownLinkedActors

}
