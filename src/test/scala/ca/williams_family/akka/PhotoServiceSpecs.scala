package ca.williams_family
package akka
package specs

import ca.williams_family.specs.matcher._

import org.specs._
import specification.Context

import org.scalacheck._

import net.liftweb.common._

import model._

class InMemoryPhotoService extends PhotoService with InMemoryPhotoStorageFactory

object PhotoGenerator {
  import Gen._
  import Arbitrary.arbitrary

  implicit def arbPhoto: Arbitrary[Photo] = {
    Arbitrary {
      def genPhoto: Gen[Photo] = for {
        id <- arbitrary[String] suchThat (_.length > 0)
        da <- arbitrary[String] suchThat (_.length > 0)
        ex <- arbitrary[String] suchThat (_.length > 0)
        ap <- arbitrary[String] suchThat (_.length > 0)
        is <- arbitrary[String] suchThat (_.length > 0)
        fo <- arbitrary[String] suchThat (_.length > 0)
        he <- arbitrary[Int] suchThat (_ > 0)
        wi <- arbitrary[Int] suchThat (_ > 0)
      } yield Photo(id, da, ex, ap, is, fo, he, wi, Map())

      genPhoto
    }
  }

}

class PhotoServiceSpec extends Specification with ScalaCheck with BoxMatchers {
  import PhotoGenerator.arbPhoto

  var ps: InMemoryPhotoService = _
  val empty = new Context {
    before {
      ps = new InMemoryPhotoService
      ps.start
    }
    after {
      ps.stop
      ps = null
    }
  }

  val testPhoto = Photo("testid", "2010-04-16T10:06:00.00", "1/60", "4.0", "200", "50",1600, 1200, Map("thumbnail" -> Image("thumbnail", "/thumburl.jpg", 180, 180), "preview" -> Image("preview", "/previewurl.jpg", 720, 540)))

  "photo storage" ->- empty should {
    "have no photos stored" in {
      ps.countPhotos must beFull.which(_ must be_==(0))
    }
    "insert photos" in {
      Prop.forAll{p: Photo => {
        ps.setPhoto(p)
        ps.getPhoto(p.id) == Full(p)
      }} must pass
      ps.countPhotos must beFull.which(_ must be_==(100))
    }
  }
}
