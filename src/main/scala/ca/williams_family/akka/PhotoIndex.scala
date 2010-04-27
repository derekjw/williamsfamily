package ca.williams_family
package akka

import model._

import se.scalablesolutions.akka.actor.Transactor

trait PhotoIndex extends Transactor {
  def receive = {
    case SetPhoto(photo,_) => setPhoto(photo)
  }

  def setPhoto(photo: Photo): Unit
}
