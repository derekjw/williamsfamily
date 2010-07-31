package ca.williams_family
package model
package specs

import net.fyrie.ratio._

import org.scalacheck._

import java.util.Date
import java.util.{Calendar => C}
import java.text.SimpleDateFormat

import Gen._
import Arbitrary.arbitrary

object Generators {

  val isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
  val idDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss00")

  def genPhotoDate: Gen[ExifDate] = {
    val cal = C.getInstance
    cal.set(2000,1,1)
    val calstart = cal.getTimeInMillis
    cal.set(2010,1,1)
    val calend = cal.getTimeInMillis
    Gen.choose(calstart,calend).flatMap{i =>
      cal.setTimeInMillis(i)
      Gen(_ => ExifDate(cal.get(C.YEAR) :: (cal.get(C.MONTH) + 1) :: cal.get(C.DATE) :: cal.get(C.HOUR_OF_DAY) :: cal.get(C.MINUTE) :: cal.get(C.SECOND) :: (cal.get(C.MILLISECOND) / 10) :: Nil))
    }
  }

  def genIdHash: Gen[String] = {
    Gen.choose(java.lang.Integer.MIN_VALUE, java.lang.Integer.MAX_VALUE).map(java.lang.Integer.toHexString)
  }

  def genPhoto: Gen[Photo] = for {
    id <- genIdHash
    da <- genPhotoDate
    //ex <- arbitrary[Ratio]
    //ap <- arbitrary[Ratio]
    is <- Gen.choose(100,10000)
    //fo <- arbitrary[Ratio]
    he <- Gen.choose(1000,10000)
    wi <- Gen.choose(1000,10000)
  } yield Photo(Photo.mkId(da,id), da, Ratio(1,3), Ratio(1,3), is, Ratio(1,3), he, wi, Map())

  implicit def arbPhoto: Arbitrary[Photo] = {
    Arbitrary { genPhoto }
  }
}
