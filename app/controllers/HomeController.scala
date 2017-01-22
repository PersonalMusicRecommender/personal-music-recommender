package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models.services.TracksService
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.json.JsValue

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(implicit ec: ExecutionContext) extends Controller {
  
 val tracksService = new TracksService 

 def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def rateTrack = Action.async { request =>
    val json = request.body.asJson.get
    
    tracksService.rateAndInsertTrack(
        (json \ "spotify-id").as[String], (json \ "name").as[String], (json \ "stars").as[Int]
    ).map(_ => Ok(Json.toJson(Map("success" -> true))))
  }
  
  def isTrackRated(spotifyId: String) = Action.async { request =>
    tracksService.hasTrackBeenRated(spotifyId).map(f => {
      Ok(Json.toJson(Map("is-track-rated" -> !f.isEmpty)))
    })
  }
  
  def getAllTracks() = Action.async { request =>
    tracksService.getTracks.map(f => {
      Ok(Json.toJson(f.map(track => RatedTrack(track.name, track.spotifyId, track.stars.get))))
    })
  }
  
  def getTracks(stars: String) = Action.async { request =>
    tracksService.getTracksWithStars(stars.toInt).map(f => {
      Ok(Json.toJson(f.map(track => RatedTrack(track.name, track.spotifyId, track.stars.get))))
    })
  }
  
  
  case class RatedTrack(name: String, spotifyId: String, stars: Int)
  
  implicit def trackWrites: Writes[RatedTrack] =
    new Writes[RatedTrack] {
      def writes(track: RatedTrack): JsValue =
        Json.obj(
          "name"        -> track.name,
          "spotify-id"  -> track.spotifyId,
          "stars"       -> track.stars
        )
    }

}
