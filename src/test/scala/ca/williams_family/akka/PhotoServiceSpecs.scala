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

import se.scalablesolutions.akka.actor.{Actor,ActorRegistry,Agent}
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
      (1 to 1000).foreach(i => genPhoto.sample.foreach(Photo.set))
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
      val counter = Agent(0)
      Prop.forAll{p: Photo =>
        counter(_ + 1)
        Photo.set(p)
        Photo.get(p.id) must beFull.which{_ must_== p}
        true
      } must pass (display(workers->4, wrkSize->20))
      Photo.count must beFull.which(_ must_== counter())
      counter.close
    }
  }

  "photo timeline" ->- full should {
    "return ids of inserted photos" in {
      Prop.forAll{p: Photo =>
        Photo.set(p)
        val date = p.createDate.toList.take(3)
        Photo.timeline(date) must beFull.which(_(p.id))
        Photo.timeline(List(date.head, date.tail.head)) must beFull.which(_(p.id))
        Photo.timeline(List(date.head)) must beFull.which(_(p.id))
        true
      } must pass (display(workers->4, wrkSize->20))
      Photo.count must_== Photo.timeline().map(_.size)
    }
  }
}

