package ca.williams_family
package model
package specs

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
  "Ratios" should {
    "Be equal with the same values" in {
      genRatioPair must pass{ p: (Int, Int) =>
        Ratio(p._1, p._2) must_== Ratio(p._1, p._2)
      }
    }
    "Be equal after reducing" in {
      genTwoEqualRatioPairs must pass{ p: (Int, Int, Int, Int) =>
        Ratio(p._1, p._2) must_== Ratio(p._3, p._4)
      }
    }
    "Allow any input" in {
      genRatio must pass{ r: Ratio => { true } }(set(minTestsOk -> 10000, maxDiscarded -> 5000, workers -> 4))
    }
  }

  "Common ratios" should {
    "Not share the same object" in {
      Ratio(1,2) mustNotBe Ratio(1,2)
    }
    "Share the same object using R(n,d)" in {
      R(1,2) mustBe R(1,2)
    }
  }

  implicit val formats = Serialization.formats(NoTypeHints) + RatioSerializer

  "Serializing to JSON" should {
    "whole numbers" in {
      swrite(R(32)) must_==("\"32\"")
    }
    "rational numbers" in {
      swrite(R(27,12)) must_==("\"9 / 4\"")
    }
  }

  "Deserializing from JSON" should {
    "whole numbers" in {
      read[RatioWrapper]("""{"ratio": "32"}""").ratio must_==(R(32))
    }
    "rational numbers" in {
      read[RatioWrapper]("""{"ratio": "9 / 4"}""").ratio must_==(R(27,12))
      read[RatioWrapper]("""{"ratio": "9/4"}""").ratio must_==(R(27,12))
    }
  }
}

case class RatioWrapper(ratio: Ratio)
