package ca.williams_family
package model
package specs

import org.scalacheck._

import java.util.Date
import java.util.{Calendar => C}
import java.text.SimpleDateFormat

import Gen._
import Arbitrary.arbitrary

object Generators {

  def genRationalPair: Gen[(Int, Int)] = for {
    n <- arbitrary[Int] suchThat (_ > java.lang.Integer.MIN_VALUE)
    d <- arbitrary[Int] suchThat (_ > java.lang.Integer.MIN_VALUE)
  } yield (n,d)

  def genTwoEqualRationalPairs: Gen[(Int, Int, Int, Int)] = for {
    n <- Gen.choose(-1000,1000)
    d <- Gen.choose(-1000,1000)
    m <- Gen.choose(1,10000)
  } yield (n,d,n*m,d*m)

  def genRational: Gen[Rational] = for {
    (n, d) <- genRationalPair
  } yield Rational(n,d)

  implicit def arbRational: Arbitrary[Rational] =
    Arbitrary { genRational }

  val isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
  val idDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss00")

  def genPhotoDate: Gen[List[Int]] = {
    val cal = C.getInstance
    cal.set(2000,1,1)
    val calstart = cal.getTimeInMillis
    cal.set(2010,1,1)
    val calend = cal.getTimeInMillis
    Gen.choose(calstart,calend).map{i =>
      cal.setTimeInMillis(i)
      cal.get(C.YEAR) :: (cal.get(C.MONTH) + 1) :: List(C.DATE, C.HOUR_OF_DAY, C.MINUTE, C.SECOND, C.MILLISECOND).map(cal.get)
    }
  }

  def genIdHash: Gen[String] = {
    Gen.choose(java.lang.Integer.MIN_VALUE, java.lang.Integer.MAX_VALUE).map(java.lang.Integer.toHexString)
  }

  def genPhoto: Gen[Photo] = for {
    id <- genIdHash
    da <- genPhotoDate
    ex <- arbitrary[Rational]
    ap <- arbitrary[Rational]
    is <- Gen.choose(100,10000)
    fo <- arbitrary[Rational]
    he <- Gen.choose(1000,10000)
    wi <- Gen.choose(1000,10000)
  } yield Photo(Photo.mkId(da,id), da, ex, ap, is, fo, he, wi, Map())

  implicit def arbPhoto: Arbitrary[Photo] = {
    Arbitrary { genPhoto }
  }
}
