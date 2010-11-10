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

import scala.xml.{Text, NodeSeq, Node}

object AsInt {
  def unapply(in: String): Option[Int] = asInt(in)
}

object Timeline extends Loc[Unit] {
  def name = "timeline"

  def defaultValue = Full(())

  def params = Nil

  override val snippets: SnippetTest = {
    case ("timeline", Full(())) => showIndex
  }

  val link = new Loc.Link[Unit](List("timeline"), false)

  val text: Loc.LinkText[Unit] = "Photos"

  override def supplimentalKidMenuItems: List[MenuItem] =
    Photo.timelineMonths.groupBy(_._1).mapValues(_.map(_._2)).toList.sortBy(_._1).reverse.map{
      case (year, months) =>
        MenuItem(TimelineYear.linkText(year), TimelineYear.createLink(year).get, months.map(month =>
          MenuItem(TimelineMonth.linkText((year, month)), TimelineMonth.createLink((year, month)).get, Nil, false, false, Nil)),false, false, Nil)
    }

  override val calcTemplate =
    Full(<div class="lift:surround?with=default;at=content"><div class="lift:timeline" /></div>)

  def showIndex: NodeSeq => NodeSeq = { in =>
    <fieldset class="timeline">
      <lift:Menu.builder level="1" expand="true" expandAll="true" />
    </fieldset>
  }

  def path: Seq[Node] = Stream(<a href={createDefaultLink}>{linkText(())}</a>)
}

object TimelineMonth extends Loc[(Int, Int)] {
  def name = "timeline-month"

  def defaultValue = Empty

  def params = List(Hidden)

  override val snippets: SnippetTest = {
    case ("timeline-month", Full((year, month))) => showMonth(year, month)
  }

  val link = new Loc.Link[(Int, Int)](List("timeline-month"), false) {
    override def pathList(in: (Int, Int)) = List("timeline", "%04d" format in._1, "%02d" format in._2)
  }

  override def title(in: (Int, Int)) = Text("%04d >> %s" format (in._1, monthNames(in._2)))

  val text = new Loc.LinkText((_: (Int, Int)) match {
      case (_, month) =>
        Text(monthNames(month))
    })

  val monthNames = Map( 1 -> "January", 2 -> "February", 3 -> "March",
                        4 -> "April", 5 -> "May", 6 -> "June", 7 -> "July",
                        8 -> "August", 9 -> "September", 10 -> "October",
                        11 -> "November", 12 -> "December" )

  override val rewrite: LocRewrite =
    Full(NamedPF("Timeline Month Rewrite") {
      case RewriteRequest(ParsePath("timeline" :: AsInt(year) :: AsInt(month) :: Nil, _, _, _), _, _) =>
        (RewriteResponse("timeline-month" :: Nil), (year, month))
    })

  override val calcTemplate =
    Full(<div class="lift:surround?with=default;at=content"><div class="lift:timeline-month" /></div>)

  def showMonth(year: Int, month: Int): NodeSeq => NodeSeq = { in =>
    <head>
      <script src="/javascript/jquery.createdomnodes-v1.1.js" />
      <script src="/javascript/jquery.fit.js" />
      <script src="/javascript/photo-overlay.js" />
    </head>
    <fieldset class="thumbnails"><h4>{path((year, month))}</h4><ul>{
        Photo.findAllByMonth(year, month).flatMap(photo =>
          photo.images.get("thumbnail").map(thumb =>
            <li class="photo" id={photo.id}><a class="thumb" href={uri(photo)}><img alt="" src={uri(thumb)} /></a></li>))
      }</ul></fieldset>
  }

  def path(in: (Int, Int), sep: NodeSeq = Text(" / ")): NodeSeq = TimelineYear.path(in._1, sep) ++ sep ++ <a href={createLink(in)}>{linkText(in)}</a>
}

object TimelineYear extends Loc[Int] {
  def name = "timeline-year"

  def defaultValue = Empty

  def params = List(Hidden)

  override val snippets: SnippetTest = {
    case ("timeline-year", Full(year)) => showYear(year)
  }

  val link = new Loc.Link[Int](List("timeline-year"), false) {
    override def createLink(in: Int) = in match {
      case year =>
        Full(Text("/timeline/%04d" format year))
    }
  }

  val text = new Loc.LinkText((_: Int) match {
      case year =>
        Text(year.toString)
    })

  override val rewrite: LocRewrite =
    Full(NamedPF("Timeline Year Rewrite") {
      case RewriteRequest(ParsePath("timeline" :: AsInt(year) :: Nil, _, _, _), _, _) =>
        (RewriteResponse("timeline-year" :: Nil), year)
    })

  override val calcTemplate =
    Full(<div class="lift:surround?with=default;at=content"><div class="lift:timeline-year" /></div>)

  def showYear(year: Int): NodeSeq => NodeSeq = { in => in }

  def path(in: Int, sep: NodeSeq = Text(" / ")): Seq[Node] = Timeline.path ++ sep ++ <a href={createLink(in)}>{linkText(in)}</a>
}
