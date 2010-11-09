package ca.williams_family
package lib

import net.liftweb._
import common._
import util._
import Helpers._
import http._
import sitemap._
import Loc._

import model._

import scala.xml.{Text, NodeSeq}

sealed trait TimelineLoc

case object TimelineIndex extends TimelineLoc

//case class TimelineYear(year: Int) extends TimelineLoc

case class TimelineMonth(year: Int, month: Int) extends TimelineLoc

object AsInt {
  def unapply(in: String): Option[Int] = asInt(in)
}

object Timeline extends Loc[TimelineLoc] {
  def name = "timeline"

  def defaultValue = Full(TimelineIndex)

  def params = Nil

  override val snippets: SnippetTest = {
    case ("timeline", Full(TimelineIndex)) => showIndex
//    case ("timeline", Full(tlYear: TimelineYear)) => showYear(tlYear)
    case ("timeline", Full(tlMonth: TimelineMonth)) => showMonth(tlMonth)
  }

  val link = new Loc.Link[TimelineLoc](List("timeline"), false) {
    override def createLink(in: TimelineLoc) = in match {
      case TimelineIndex =>
        Full(Text("/timeline"))
//      case TimelineYear(year) =>
//        Full(Text("/timeline/%04d" format year))
      case TimelineMonth(year, month) =>
        Full(Text("/timeline/%04d/%02d" format (year,month)))
    }
  }

  val text = new Loc.LinkText(calcLinkText _)

  def calcLinkText(in: TimelineLoc): NodeSeq = in match {
      case TimelineIndex =>
        Text("Photos")
//      case TimelineYear(year) =>
//        Text("%04d" format year)
      case TimelineMonth(_, month) =>
        Text(monthNames(month))
    }

  val monthNames = Map( 1 -> "January", 2 -> "February", 3 -> "March",
                        4 -> "April", 5 -> "May", 6 -> "June", 7 -> "July",
                        8 -> "August", 9 -> "September", 10 -> "October",
                        11 -> "November", 12 -> "December" )

  override val rewrite: LocRewrite =
    Full(NamedPF("Timeline Rewrite") {
//      case RewriteRequest(ParsePath("timeline" :: AsInt(year) :: Nil, _, _, _), _, _) =>
//        (RewriteResponse("timeline" :: Nil), TimelineYear(year))
      case RewriteRequest(ParsePath("timeline" :: AsInt(year) :: AsInt(month) :: Nil, _, _, _), _, _) =>
        (RewriteResponse("timeline" :: Nil), TimelineMonth(year, month))
    })

  override def calcTemplate =
    Full(<fieldset class="timeline lift:surround?with=default;at=content"><div class="lift:timeline" /></fieldset>)

  def showIndex: NodeSeq => NodeSeq = { in =>
    <ul>{
      Photo.timelineMonths.groupBy(_._1).toSeq.sortBy(_._1).reverse.map{
        case (year, months) =>
          <li class="year"><h4>{year}</h4><ul>{
            months.map(m => TimelineMonth(year, m._2)).map(m =>
              <li class="month"><a href={createLink(m)}>{linkText(m)}</a></li>)
          }</ul></li>
      }
    }</ul>
  }

  def showMonth(tlMonth: TimelineMonth): NodeSeq => NodeSeq = { in =>
    <head>
      <script src="/javascript/jquery.createdomnodes-v1.1.js" />
      <script src="/javascript/jquery.fit.js" />
      <script src="/javascript/photo-overlay.js" />
    </head>
    <fieldset class="thumbnails"><ul>{
        Photo.findAllByMonth(tlMonth.year, tlMonth.month).flatMap(photo =>
          photo.images.get("thumbnail").map(thumb =>
            <li class="photo" id={photo.id}><a class="thumb" href={uri(photo)}><img alt='' src={uri(thumb)} /></a></li>))
      }</ul></fieldset>
  }
}
