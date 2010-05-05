package bootstrap.liftweb

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.http._
import js.JE._
import js.JsCmds._
import js.jquery._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import Helpers._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.dispatch.Futures._
import se.scalablesolutions.akka.config.ScalaConfig._

import ca.williams_family._
import model._
import lib._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Logger {
  import LiftRules._

  def boot {
    LiftRules.early.append {
      _.setCharacterEncoding("UTF-8")
    }
    
    // Display using XHtml Strict by default
    ResponseInfo.docType = {
      case _ if S.getDocType._1 => S.getDocType._2
      case _ => Full(DocType.xhtmlStrict)
    }

    jsArtifacts = JQuery14Artifacts

    statelessDispatchTable.append(RestServices)
    AjaxDispatch.init()

    // where to search snippet
    addToPackages("ca.williams_family")

    Photo.service = new akka.PhotoService with akka.RedisPhotoStorageFactory

    Photo.withService{ps =>
      ps.start
      info("Photo count: "+ps.countPhotos)
      ps.registerIndex(new akka.InMemoryPhotoDateIndex)
      info("Photos indexed: "+logTime("Getting all from index")(ps.getPhotosByDate(Nil).map(_.foldLeft(0)(_ + _.size))))
      //val dir = new java.io.File("output")
      //val filter = new java.io.FileFilter() { def accept(file: java.io.File): Boolean = { file.getName.endsWith(".json") } }
      //logTime("Loading production photos")(dir.listFiles(filter).toList.map(f => ps.setPhoto(Photo.deserialize(new String(readWholeFile(f), "UTF-8")))))
    }

    statefulRewrite.prepend {
      case
        RewriteRequest(
          ParsePath("timeline" :: year :: month :: Nil, _, _, _), _, _) =>
        RewriteResponse(
          ParsePath("timeline-photos" :: Nil, "html", false, false),
          Map("year" -> year, "month" -> month))
      case
        RewriteRequest(
          ParsePath("photos" :: id :: Nil, _, _, _), _, _) =>
        RewriteResponse(
          ParsePath("photo" :: Nil, "html", false, false),
          Map("id" -> id))
    }

    // Build SiteMap
    val entries =
      Menu(Loc("Home", List("index"), "Home")) ::
      Menu(Loc("Location", List("location"), "Location", Hidden)) :: 
      Menu(Loc("Photo", List("photo"), "Photo", Hidden)) ::
      Nil
    setSiteMap(SiteMap(entries:_*))

    unloadHooks.append { () =>
      Photo.service.map(_.stop)
    }
  }

}

