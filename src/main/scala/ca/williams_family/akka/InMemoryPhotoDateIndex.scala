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

  type K = (Int, Int, Int)
  type V = TreeSet[String]
  type Col = TreeMap[K,V]

  private var index = TreeMap[K,V]()

  def receive = {
    case GetPhotosByDate(Nil) => reply(index)
    case GetPhotosByDate(year :: Nil) => reply(index.range((year,1,1),(year+1,1,1)))
    case GetPhotosByDate(year :: month :: Nil) => reply(index.range((year,month,1),(year,month+1,1)))
    case GetPhotosByDate(year :: month :: day :: Nil) => reply(index.range((year,month,day),(year,month,day+1)))
    case SetPhoto(p) => {
      p.createDate match {
        case year :: month :: day :: rest => {
          val d = (year, month, day)
          index += (d -> (index.getOrElse(d, TreeSet[String]()) + p.id))
        }
        case _ => error("Invalid Photo")
      }
    }
  }
}


