package ca.williams_family
package model
package specs

import net.fyrie.ratio._

import org.scalacheck._

import org.joda.time.{DateTime, LocalDateTime, DateTimeZone}

import Gen._
import Arbitrary.arbitrary

object Generators {

  def genLocalDateTime: Gen[LocalDateTime] = {
    val start = (new DateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC)).getMillis
    val end = (new DateTime(2010, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC)).getMillis
    Gen.choose(start,end).map{i => new LocalDateTime(i, DateTimeZone.UTC)}
  }

  def genIdHash: Gen[String] = {
    Gen.choose(java.lang.Integer.MIN_VALUE, java.lang.Integer.MAX_VALUE).map(java.lang.Integer.toHexString)
  }

  def genPhoto: Gen[Photo] = for {
    id <- genIdHash
    da <- genLocalDateTime
    //ex <- arbitrary[Ratio]
    //ap <- arbitrary[Ratio]
    is <- Gen.choose(0,7).map(x => (math.pow(2, x).toInt) * 100)
    //fo <- arbitrary[Ratio]
    he <- Gen.choose(1000,10000)
    wi <- Gen.choose(1000,10000)
  } yield Photo(Photo.mkId(da,id), da, Some(Ratio(1,3)), Some(Ratio(1,3)), Some(is), Some(Ratio(1,3)), he, wi, Map())

  implicit def arbPhoto: Arbitrary[Photo] = {
    Arbitrary { genPhoto }
  }
}
