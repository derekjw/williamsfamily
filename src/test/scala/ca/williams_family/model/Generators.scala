package ca.williams_family
package model
package specs

import org.scalacheck._

import Gen._
import Arbitrary.arbitrary

object Generators {

  implicit def arbRational: Arbitrary[Rational] =
    Arbitrary {
      def genRational: Gen[Rational] = for {
        n <- Gen.choose(1,100)
        d <- Gen.choose(1,100)
      } yield Rational(n,d)

      genRational
    }

  implicit def arbPhoto: Arbitrary[Photo] = {
    Arbitrary {
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

      genPhoto
    }
  }
}
