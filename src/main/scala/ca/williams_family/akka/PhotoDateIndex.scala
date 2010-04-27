package ca.williams_family
package akka

import collection.SortedSet

import model._

import se.scalablesolutions.akka.actor.Transactor

trait PhotoDateIndex extends PhotoIndex {
  type idxSet = SortedSet[String]
  val set = SortedSet[String] _

  def receive = {
    case SetPhoto(photo,_) => setPhoto(photo)
    case GetPhotosByDate(date) => reply(getPhotosByDate(date))
  }

  def setPhoto(photo: Photo): Unit = withSet(photo.id.take(6).toInt)(_ + photo.id)

  def getPhotosByDate(date: Int): idxSet = getSet(date)

  def getSet(key: Int): idxSet

  def putSet(key: Int, newSet: idxSet): Unit
  
  def withSet(key: Int)(f: (idxSet) => idxSet): Unit = putSet(key, f(getSet(key)))

}
