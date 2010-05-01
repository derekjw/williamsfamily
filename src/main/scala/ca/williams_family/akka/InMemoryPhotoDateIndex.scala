package ca.williams_family
package akka

import net.liftweb.common._
import Box._

import model._

import collection.{SortedSet,SortedMap}

import collection.immutable.TreeSet

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.config.ScalaConfig._

class InMemoryPhotoDateIndex extends PhotoDateIndex {
  lifeCycle = Some(LifeCycle(Permanent))

  val empty = SortedSet[String]()

  private var years = SortedMap[Int, Actor]()

  def receive = {
    case GetPhotosByDate(year :: rest) => years.get(year) match {
      case Some(a) => a forward GetPhotosByDate(rest)
      case _ => reply(empty)
    }
    case GetPhotosByDate(Nil) =>
      Actor.spawn{
        reply(years.valuesIterator.map(a => a !!! GetPhotosByDate(Nil)).foldLeft(empty){
          case (s, f) => {
            f.awaitBlocking
            f.result.asA[SortedSet[String]].map(s ++ _).getOrElse(s)
          }
        })
      }
    case SetPhoto(p,_) =>
      p.createDate.take(10).split('-').toList.map(_.toInt) match {
        case year :: rest => {
          years.get(year).getOrElse{
            val a = new YearIndex(year)
            startLink(a)
            years += (year -> a)
            a
          } forward SetPhotoDateIndex(p, rest)
        }
        case _ => error("Invalid Photo")
      }
  }

  override def shutdown = {
    years.valuesIterator.foreach {i =>
      unlink(i)
      i.stop
    }
  }

  class YearIndex(year: Int) extends Actor {
    id = "PhotoDateIndex:"+year
    
    lifeCycle = Some(LifeCycle(Permanent))

    private var months = SortedMap[Int, Actor]()

    def receive = {
      case GetPhotosByDate(month :: rest) => months.get(month) match {
        case Some(a) => a forward GetPhotosByDate(rest)
        case _ => reply(empty)
      }
      case GetPhotosByDate(Nil) =>
        Actor.spawn{
          reply(months.valuesIterator.map(a => a !!! GetPhotosByDate(Nil)).foldLeft(empty){
            case (s, f) => {
              f.awaitBlocking
              f.result.asA[SortedSet[String]].map(s ++ _).getOrElse(s)
            }
          })
        }
      case SetPhotoDateIndex(p, month :: rest) => months.get(month).getOrElse{
        val a = new MonthIndex(year, month)
        startLink(a)
        months += (month -> a)
        a
      } forward SetPhotoDateIndex(p, rest)
    }

    override def shutdown = {
      months.valuesIterator.foreach {i =>
        unlink(i)
        i.stop
      }
    }

    class MonthIndex(year: Int, month: Int) extends Actor {
      id = "PhotoDateIndex:"+year+"-"+month

      lifeCycle = Some(LifeCycle(Permanent))

      private var days = SortedMap[Int, Actor]()

      def receive = {
        case GetPhotosByDate(day :: rest) => days.get(day) match {
          case Some(a) => a forward GetPhotosByDate(rest)
          case _ => reply(empty)
        }
        case GetPhotosByDate(Nil) =>
          Actor.spawn{
            reply(days.valuesIterator.map(a => a !!! GetPhotosByDate(Nil)).foldLeft(empty){
              case (s, f) => {
                f.awaitBlocking
                f.result.asA[SortedSet[String]].map(s ++ _).getOrElse(s)
              }
            })
          }
        case SetPhotoDateIndex(p, day :: rest)  => days.get(day).getOrElse{
          val a = new DayIndex(year, month, day)
          startLink(a)
          days += (day -> a)
          a
        } forward SetPhotoDateIndex(p, rest)

      }

      override def shutdown = {
        days.valuesIterator.foreach {i =>
          unlink(i)
          i.stop
        }
      }

      class DayIndex(year: Int, month: Int, day: Int) extends Actor {
        id = "PhotoDateIndex "+year+"-"+month+"-"+day

        lifeCycle = Some(LifeCycle(Permanent))

        private var index = empty

        def receive = {
          case GetPhotosByDate(Nil) => reply(index)
          case SetPhotoDateIndex(p,_) => index += p.id
        }
      }
    }
  }
}
