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

import se.scalablesolutions.akka.actor.Actor._
import se.scalablesolutions.akka.dispatch.Futures._
import se.scalablesolutions.akka.config.ScalaConfig._

import net.liftweb.ext_api.facebook.{FacebookRestApi}

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

    ResponseInfo.docType = {
      case _ if S.skipDocType => Empty
      case _ if S.getDocType._1 => S.getDocType._2
      case _ => Full(DocType.html5)
    }

    //this is really important for fb connect
    useXhtmlMimeType = false 

    println(Props.get("fbapikey"))

    Props.get("fbapikey").foreach(FacebookRestApi.apiKey = _)
    Props.get("fbsecret").foreach(FacebookRestApi.secret = _)

    jsArtifacts = JQuery14Artifacts

    ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    statelessDispatchTable.append(RestServices)
    AjaxDispatch.init()

    loggedInTest = Full(() => User.loggedIn_?)

    // where to search snippet
    addToPackages("ca.williams_family")

    Photo.service = new RedisPhotoService

    Photo.withService{ps =>
      ps.start
      info("Photo count: "+ps.countPhotos)
      ps.registerIndex(newActor[akka.InMemoryPhotoTimelineIndex])
      info("Photos indexed: "+logTime("Getting all from index")(ps.getPhotoTimeline().map(_.size)))
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
    val entries = SiteMap(
      Menu("Home") / "index",
      Menu("Location") / "location" >> Hidden,
      Menu("Photo") / "photo" >> Hidden,
      Menu("Photos") / "timeline",
      Menu("Timeline Photos") / "timeline-photos" >> Hidden,
      Menu("cross site receiver") / "xd_receiver" >> Hidden)
    
    setSiteMap(entries)

    unloadHooks.append { () =>
      Photo.service.map(_.stop)
    }
  }

}


class RedisPhotoService extends akka.PhotoService with akka.RedisPhotoStorageFactory
