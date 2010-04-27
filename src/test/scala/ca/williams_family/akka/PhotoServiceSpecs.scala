package ca.williams_family
package akka
package specs

import ca.williams_family.specs.matcher._
import ca.williams_family.model.specs.Generators._

import org.specs._
import specification.Context

import org.scalacheck._

import scala.collection.SortedSet

import se.scalablesolutions.akka.actor.ActorRegistry
import se.scalablesolutions.akka.dispatch.Futures._

import net.liftweb.common._

import model._

class InMemoryPhotoService extends PhotoService with InMemoryPhotoStorageFactory

class PhotoServiceSpec extends Specification with ScalaCheck with BoxMatchers {
  
  var ps: InMemoryPhotoService = _
  
  val empty = new Context {
    before {
      ps = new InMemoryPhotoService
      ps.start
      ps.registerIndex(new InMemoryPhotoDateIndex)
    }
    after {
      ps.stop
    }
  }

  val large = new Context {
    before {
      ps = new InMemoryPhotoService
      ps.start
      ps.registerIndex(new InMemoryPhotoDateIndex)
      (1 to 10000).foreach(i => genPhoto.sample.foreach(ps.setPhoto))
    }
    after {
      ps.stop
    }
  }
  
  "photo storage" ->- empty should {
    "have no photos stored" in {
      ps.countPhotos must beFull.which(_ must_== 0)
    }
    "insert photos" in {
      Prop.forAll{p: Photo => {
        ps.setPhoto(p)
        ps.getPhoto(p.id) == Full(p)
      }} must pass
      ps.countPhotos must beFull.which(_ must_== 100)
    }
  }

  "photo date index" ->- large should {
    "return ids of inserted photos" in {
      val idx = ActorRegistry.actorsFor[PhotoDateIndex].head
      Prop.forAll{p: Photo => {
        ps.setPhoto(p)
        val Full(r) = ps.getPhotosByDate(p.id.take(6).toInt)
        r(p.id)
      }} must pass
    }
  }
}
