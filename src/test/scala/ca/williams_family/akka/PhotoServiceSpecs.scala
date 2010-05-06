package ca.williams_family
package akka
package specs

import ca.williams_family.specs.matcher._
import ca.williams_family.model.specs.Generators._

import org.specs._
import specification.Context

import org.scalacheck._

import scala.collection.SortedSet

import se.scalablesolutions.akka.actor.{Actor,ActorRegistry}
import Actor._
import se.scalablesolutions.akka.dispatch.Futures._

import net.liftweb.common._
import net.liftweb.util.IoHelpers._
import net.liftweb.util.TimeHelpers._

import java.io.{File, FileFilter}

import model._

class InMemoryPhotoService extends PhotoService with InMemoryPhotoStorageFactory

class PhotoServiceSpec extends Specification with ScalaCheck with BoxMatchers {
    
  val empty = new Context {
    before {
      Photo.service = new InMemoryPhotoService
      Photo.withService{ps =>
        ps.start
        ps.registerIndex(newActor[InMemoryPhotoTimelineIndex])
      }
    }
    after {
      Photo.withService(_.stop)
    }
  }

  val full = new Context {
    before {
      Photo.service = new InMemoryPhotoService
      Photo.withService{ps =>
        ps.start
        ps.registerIndex(newActor[InMemoryPhotoTimelineIndex])
        awaitAll((1 to 10000).flatMap(i => genPhoto.sample.map(ps.setPhoto)).toList)
      }
    }
    after {
      Photo.withService(_.stop)
    }
  }

  val fullNonBlocking = new Context {
    before {
      Photo.service = new InMemoryPhotoService
      Photo.withService{ps =>
        ps.start
        ps.registerIndex(newActor[InMemoryPhotoTimelineIndex])
        (1 to 10000).foreach(i => genPhoto.sample.foreach(ps.setPhoto))
      }
    }
    after {
      Photo.withService(_.stop)
    }
  }

  val fullNonBlockingNoIndexes = new Context {
    before {
      Photo.service = new InMemoryPhotoService
      Photo.withService{ps =>
        ps.start
        (1 to 10000).flatMap(i => genPhoto.sample.map(ps.setPhoto))
      }
    }
    after {
      Photo.withService(_.stop)
    }
  }

  val production = new Context {
    before {
      Photo.service = new InMemoryPhotoService
      Photo.withService{ps =>
        ps.start
        val dir = new File("output")
        val filter = new FileFilter() { def accept(file: File): Boolean = { file.getName.endsWith(".json") } }
        logTime("Loading production photos")(awaitAll(dir.listFiles(filter).toList.map(f => ps.setPhoto(Photo.deserialize(new String(readWholeFile(f), "UTF-8"))))))
      }
    }
    after {
      Photo.withService(_.stop)
    }
  }
  
  "photo storage" ->- empty should {
    "have no photos stored" in {
      Photo.withService{ps =>
        ps.countPhotos must beFull.which(_ must_== 0)
      }
    }
    "insert photos" in {
      Photo.withService{ps =>
        Prop.forAll{p: Photo => {
          ps.setPhoto(p).await
          ps.getPhoto(p.id) == Full(p)
        }} must pass
        ps.countPhotos must beFull.which(_ must_== 100)
      }
    }
  }

  "photo timeline" ->- fullNonBlocking should {
    "return ids of inserted photos" in {
      Photo.withService{ps =>
        Prop.forAll{p: Photo => {
          ps.setPhoto(p).awaitBlocking
          val date = p.createDate.take(3)
          ps.getPhotoTimeline(date).exists(_(p.id)) && ps.getPhotoTimeline(List(date.head, date.tail.head)).exists(_(p.id)) && ps.getPhotoTimeline(List(date.head)).exists(_(p.id))          
        }} must pass
        ps.countPhotos must_== ps.getPhotoTimeline().map(_.size)
        var pIds = Set[String]()
        awaitAll((1 to 1000).flatMap(i => genPhoto.sample.map{p => pIds += p.id; ps.setPhoto(p)}).toList)
        logTime("Get "+pIds.size+" photos")(pIds.map(pId => ps.getPhoto(pId))).foreach(_ must beFull)
      }
    }
  }

  "reindexing" ->- fullNonBlockingNoIndexes should {
    "return indexed values" in {
      Photo.withService{ps =>
        ps.registerIndex(newActor[InMemoryPhotoTimelineIndex])
        Prop.forAll{p: Photo =>
          ps.setPhoto(p)
          val date = p.createDate.take(3)
          ps.getPhotoTimeline(date).exists(_(p.id)) && ps.getPhotoTimeline(List(date.head, date.tail.head)).exists(_(p.id)) && ps.getPhotoTimeline(List(date.head)).exists(_(p.id))
        } must pass
        ps.countPhotos must_== ps.getPhotoTimeline(Nil).map(_.size)
      }
    }
  }

/*  "production photos" ->- production should {
    "have proper count" in {
      ps.registerIndex(new InMemoryPhotoDateIndex)
      ps.countPhotos must beFull.which(_ must_== 17454)
      logTime("Getting index for all photos")(ps.getPhotosByDate(Nil)) must beFull.which(_.size must_== 17454)
      logTime("Getting index for year")(ps.getPhotosByDate(List(2009))) must beFull.which(_.size must_== 3917)
      logTime("Getting index for month")(ps.getPhotosByDate(List(2009,12))) must beFull.which(_.size must_== 247)
      logTime("Getting index for day")(ps.getPhotosByDate(List(2009,12,25))) must beFull.which(_.size must_== 157)
    }
  }*/
}

