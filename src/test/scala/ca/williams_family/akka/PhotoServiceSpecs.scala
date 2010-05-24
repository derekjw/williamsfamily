package ca.williams_family
package akka
package specs

import ca.williams_family.specs.matcher._
import ca.williams_family.model.specs.Generators._

import org.specs._
import specification.Context
import org.specs.util.Duration

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

class InMemoryPhotoService extends PhotoService
  with InMemoryPhotoStorageFactory
  with InMemoryPhotoTimelineIndexFactory

class PhotoServiceSpec extends Specification with ScalaCheck with BoxMatchers {
    
  val empty = new Context {
    before {
      Photo.service = actorOf[InMemoryPhotoService]
    }
    after {
      Photo.stopService
    }
  }

  val full = new Context {
    before {
      Photo.service = actorOf[InMemoryPhotoService]
      (1 to 10000).foreach(i => genPhoto.sample.foreach(Photo.set))
    }
    after {
      Photo.stopService
    }
  }

/*  val production = new Context {
    before {
      Photo.service = actorOf[InMemoryPhotoService]
      val dir = new File("output")
      val filter = new FileFilter() { def accept(file: File): Boolean = { file.getName.endsWith(".json") } }
      logTime("Loading production photos")(awaitAll(dir.listFiles(filter).toList.flatMap(f => Photo.set(Photo.deserialize(new String(readWholeFile(f), "UTF-8"))))))
    }
    after {
      Photo.stopService
    }
  }*/
  
  "photo storage" ->- empty should {
    "have no photos stored" in {
      Photo.count must beFull.which(_ must_== 0)
    }
    "insert photos" in {
      Prop.forAll{p: Photo =>
        Photo.set(p)
        Photo.get(p.id) must beFull.which{_ must_== p}
        true
      } must pass
      Photo.count must beFull.which(_ must_== 100)
    }
  }

  "photo timeline" ->- full should {
    "return ids of inserted photos" in {
      Prop.forAll{p: Photo =>
        Photo.set(p)
        val date = p.createDate.take(3)
        Photo.timeline(date).exists(_(p.id)) &&
        Photo.timeline(List(date.head, date.tail.head)).exists(_(p.id)) &&
        Photo.timeline(List(date.head)).exists(_(p.id))
      } must pass
      Photo.count must_== Photo.timeline().map(_.size)
      var pIds = Set[String]()
      (1 to 1000).foreach(i => genPhoto.sample.foreach{p => pIds += p.id; Photo.set(p)})
      logTime("Get "+pIds.size+" photos")(pIds.map(pId => Photo.get(pId))).foreach(_ must beFull)
    }
  }
}

