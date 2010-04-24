package ca.williams_family
package model

import net.liftweb.common._
import net.liftweb.json._
import JsonAST._
import JsonDSL._
import JsonParser._
import Serialization.{read, write}
import org.apache.commons.math.util.MathUtils

case class Photo(id: String, createDate: String, exposure: Rational, aperature: Rational, iso: Int, focalLength: Rational, width: Int, height: Int, images: Map[String, Image])

object Photo {
  def serialize(in: Photo) = {
    implicit val formats = DefaultFormats
    write(in)
  }
  def deserialize(in: String) = {
    implicit val formats = DefaultFormats
    read[Photo](in)
  }
}

case class Image(size: String, fileName: String, width: Int, height: Int)

object Rational {
  def apply(n: Int, d: Int = 1): Rational = {
    val m = if (d < 0) (-1) else 1
    val g = MathUtils.gcd(n, d)
    if (g == 0) (new Rational(0,0)) else (new Rational(m * n / g, m * d / g))
  }
  def unapply(in: Any): Option[(Int,Int)] = in match {
    case r: Rational => Some((r.n, r.d))
    case _ => None
  }

}

class Rational private (val n: Int, val d: Int) {
  override def toString = if (d > 1) (n + " / " + d) else (n.toString)
  override def hashCode: Int = 37 * (37 * 17 * n) * d
  override def equals(in: Any): Boolean = in match {
    case Rational(a,b) if n == a && d == b => true
    case _ => false
  }
}

