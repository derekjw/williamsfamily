package ca.williams_family
package snippet

import scala.xml._
import ca.williams_family.model._
import net.liftweb.common._
import net.liftweb.util._
import Helpers._
import net.liftweb.http.S

import ca.williams_family.model._

object Timeline {
  val monthNames = Map( 1 -> "January", 2 -> "February", 3 -> "March",
                        4 -> "April", 5 -> "May", 6 -> "June", 7 -> "July",
                        8 -> "August", 9 -> "September", 10 -> "October",
                        11 -> "November", 12 -> "December" )
}

class Timeline {
  def yearNode(year: Int) =
    <h4 id={"year-%04d" format year}>{year}</h4>
  
  def monthNode(year: Int, month: Int) =
    <a id={"month-%04d-%02d" format (year,month)} href={"/timeline/"+year+"/"+month}>{Timeline.monthNames(month)}</a>

  def list =
    ".year *" #> Photo.timelineMonths.groupBy(_._1).toSeq.sortBy(_._1).reverse.map{
      case (year, months) =>
        ("#year" #> yearNode(year)) &
        (".month *" #> months.map(_._2).map(month => "#month" #> monthNode(year, month)))}
  
  def photos =
    ".photo" #> S.param("year").flatMap(asInt).toList.flatMap(year =>
      S.param("month").flatMap(asInt).toList.flatMap(month =>
        Photo.findAllByMonth(year, month).flatMap(photo =>
          photo.images.get("thumbnail").map(thumb =>
            ".photo [id]" #> photo.id &
            ".thumb [href]" #> uri(photo) &
            "src=thumb" #> <img src={uri(thumb)}/>))))

}
