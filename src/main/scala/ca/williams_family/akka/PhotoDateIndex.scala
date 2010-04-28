package ca.williams_family
package akka

import collection.SortedSet

import model._

import se.scalablesolutions.akka.actor.Transactor

trait PhotoDateIndex extends PhotoIndex {
  type V = SortedSet[String]
  type K = Int
  val nV = SortedSet[String] _

  def receive = {
    case SetPhoto(photo,_) => setPhoto(photo)
    case GetPhotosByDate(date) => reply(getPhotos(date))
  }

  def setPhoto(photo: Photo): Unit = modify(photo.id.take(6).toInt)(_ + photo.id)

  def getPhotos(k: K): V = get(k)

  def get(k: K): V

  def put(k: K, v: V): Unit
  
  def modify(k: K)(f: (V) => V): Unit = put(k, f(get(k)))

}
