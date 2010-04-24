package ca.williams_family
package model
package specs

import org.scalacheck._

import Gen._
import Arbitrary.arbitrary

object Generators {

  def genRational: Gen[Rational] = for {
    n <- arbitrary[Int] suchThat (_ > java.lang.Integer.MIN_VALUE)
    d <- arbitrary[Int] suchThat (_ > java.lang.Integer.MIN_VALUE)
  } yield Rational(n,d)

  implicit def arbRational: Arbitrary[Rational] =
    Arbitrary { genRational }

  def genPhoto: Gen[Photo] = for {
    id <- arbitrary[String] suchThat (_.length > 0)
    da <- arbitrary[String] suchThat (_.length > 0)
    ex <- arbitrary[Rational]
    ap <- arbitrary[Rational]
    is <- Gen.choose(100,10000)
    fo <- arbitrary[Rational]
    he <- Gen.choose(1000,10000)
    wi <- Gen.choose(1000,10000)
  } yield Photo(id, da, ex, ap, is, fo, he, wi, Map())

  implicit def arbPhoto: Arbitrary[Photo] = {
    Arbitrary { genPhoto }
  }
}
