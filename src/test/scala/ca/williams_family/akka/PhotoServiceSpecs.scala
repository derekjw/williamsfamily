package ca.williams_family
package specs

import ca.williams_family.specs.matcher._
import ca.williams_family.model.specs.Generators._

import org.specs._
import specification.Context
import org.specs.util.Duration

import org.scalacheck._

import scala.collection.SortedSet

import net.liftweb.common._
import net.liftweb.util.IoHelpers._
import net.liftweb.util.TimeHelpers._

import java.io.{File, FileFilter}

import net.fyrie.redis.RedisClient
import net.fyrie.redis.Commands._

import model._

class PhotoServiceSpec extends Specification with ScalaCheck with BoxMatchers {
    
  val empty = new Context {
    before {
      redisClient send flushdb
    }
    after {
      redisClient send flushdb
    }
  }

  val full = new Context {
    before {
      redisClient send flushdb
      (1 to 1000).foreach(i => genPhoto.sample.foreach(Photo.save))
      assert(Photo.count == 1000)
    }
    after {
      redisClient send flushdb
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
      Photo.count must_== 0
    }
    "insert photos" in {
      Prop.forAll{p: Photo =>
        Photo.save(p)
        val res = Photo.find(p.id)
        res must beSome.which{_ must_== p}
        true
      } must pass (display(workers->10, wrkSize->10))
      Photo.count must be_>=(100)
    }
  }

  "full photo storage" ->- full should {
    "handle photo not found" in {
      val res = Photo.find("this is a missing photo id")
      res must beEmpty
    }
  }

  "photo timeline" ->- full should {
    "return ids of inserted photos" in {
      Prop.forAll{p: Photo =>
        Photo.save(p)
        val year = p.createDate.getYear
        val month = p.createDate.getMonthOfYear
        Photo.findAllByMonth(year, month).contains(p)
      } must pass (display(workers->10, wrkSize->10))
    }
  }
}

