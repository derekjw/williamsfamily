package ca.williams_family
package model
package specs

import Generators._

import ca.williams_family.specs.matcher._

import org.specs._
import specification.Context

import org.scalacheck._

import net.liftweb.common._

class RationalSpec extends Specification with ScalaCheck with BoxMatchers {
  "Rationals" should {
    "Be equal with the same values" in {
      Rational(9,10) must be_==(Rational(9,10))
    }
    "Be equal after reducing" in {
      Rational(100,5) must be_==(Rational(20,1))
    }
  }
}
