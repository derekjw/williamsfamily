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

class RationalSpec extends Specification with ScalaCheck with BoxMatchers {
  "Rationals" should {
    "Be equal with the same values" in {
      genRationalPair must pass{ p: (Int, Int) =>
        Rational(p._1, p._2) must_== Rational(p._1, p._2)
      }
    }
    "Be equal after reducing" in {
      genTwoEqualRationalPairs must pass{ p: (Int, Int, Int, Int) =>
        Rational(p._1, p._2) must_== Rational(p._3, p._4)
      }
    }
    "Allow any input" in {
      genRational must pass{ r: Rational => { true } }(set(minTestsOk -> 10000, maxDiscarded -> 1000, workers -> 4))
    }
  }

  "Common rationals" should {
    "Not share the same object" in {
      Rational(1,2) mustNotBe Rational(1,2)
    }
    "Share the same object using R(n,d)" in {
      R(1,2) mustBe R(1,2)
    }
  }
}
