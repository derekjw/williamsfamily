package ca.williams_family
package akka

import net.liftweb.common._
import Box._

import model._

import collection.immutable.{TreeMap,TreeSet}

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.config.ScalaConfig._

class InMemoryPhotoDateIndex extends PhotoDateIndex {
  lifeCycle = Some(LifeCycle(Permanent))

  private var index = TreeMap[Int, TreeSet[String]]()

  def receive = {
    case GetPhotosByDate(Nil) => reply( index.valuesIterator )
    case GetPhotosByDate(year :: Nil) =>
      val d = year * 10000
      reply(index.range(d,d+10000).valuesIterator)
    case GetPhotosByDate(year :: month :: Nil) =>
      val d = year * 10000 + month * 100
      reply(index.range(d,d+100).valuesIterator)
    case GetPhotosByDate(year :: month :: day :: Nil) =>
      val d = year * 10000 + month * 100 + day
      reply(index.range(d,d+1).valuesIterator)
    case SetPhoto(p) => {
      p.createDate match {
        case year :: month :: day :: rest => {
          val d = year * 10000 + month * 100 + day
          index += (d -> (index.getOrElse(d, TreeSet[String]()) + p.id))
        }
        case _ => error("Invalid Photo")
      }
    }
  }
}
