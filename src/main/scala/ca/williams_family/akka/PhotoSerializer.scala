package ca.williams_family
package akka

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.dispatch._
import se.scalablesolutions.akka.patterns._
import net.liftweb.common._

class PhotoSerializer(supervisor: Actor) extends Actor {
  def receive = {
    case SerializedPhoto(json) => reply(Photo.deserialize(json))
    case photo: Photo => reply(Photo.serialize(photo))
    case SetPhoto(photo,None) => {
      supervisor forward SetPhoto(photo,Some(Photo.serialize(photo)))
    }
    //case (json: String, WithPhoto(f)) => f(Photo.deserialize(json))
  }
}
