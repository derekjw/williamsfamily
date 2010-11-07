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
import net.liftweb.json.Serialization

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

    jsArtifacts = JQuery14Artifacts

    statelessDispatchTable.append(RestServices)
    AjaxDispatch.init()

    addToPackages("ca.williams_family")

    info("Photo count: "+Photo.count)
    /*val dir = new java.io.File("output")
    val filter = new java.io.FileFilter() { def accept(file: java.io.File): Boolean = { file.getName.endsWith(".json") } }
    logTime("Loading production photos")(dir.listFiles(filter).toList.map{f =>
      Photo.save(Serialization.read[Photo](new String(readWholeFile(f), "UTF-8")))
    })*/

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
      GlobalRedisClient.stop
      GlobalRedisClient.shutdownWorkarounds
    }
  }
}
