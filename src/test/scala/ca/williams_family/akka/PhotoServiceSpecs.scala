package ca.williams_family
package akka
package specs

import ca.williams_family.specs.matcher._
import ca.williams_family.model.specs.Generators._

import org.specs._
import specification.Context

import org.scalacheck._

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
}
