package ca.williams_family
package model

import net.liftweb.common._
import net.liftweb.record._
import field._

case class Photo(id: String, createDate: String, exposure: String, aperature: String, iso: String, focalLength: String, width: Int, height: Int, images: Map[String, Image])
case class Image(size: String, fileName: String, width: Int, height: Int)
