package ca.williams_family
package model
package specs

import net.fyrie.ratio._

import Generators._

import ca.williams_family.specs.matcher._

import org.specs._
import specification.Context

import org.scalacheck._
import org.scalacheck.Prop._

import net.liftweb.common._
import net.liftweb.json._
import Serialization.{read, write => swrite}

class RatioSpec extends Specification with ScalaCheck with BoxMatchers {

  "Serializing to JSON" should {
    "whole numbers" in {
      swrite(Ratio(32)) must_==("\"32\"")
    }
    "rational numbers" in {
      swrite(Ratio(27,12)) must_==("\"9/4\"")
    }
  }

  "Deserializing from JSON" should {
    "whole numbers" in {
      read[RatioWrapper]("""{"ratio": "32"}""").ratio must_==(Ratio(32))
    }
    "rational numbers" in {
      read[RatioWrapper]("""{"ratio": "9 / 4"}""").ratio must_==(Ratio(27,12))
      read[RatioWrapper]("""{"ratio": "9/4"}""").ratio must_==(Ratio(27,12))
    }
  }
}

case class RatioWrapper(ratio: Ratio)
