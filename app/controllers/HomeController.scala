package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models.services.TracksService
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import services.SpotifyService
import util.Domain.RatedTrack
import services.SyncService

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(ws: WSClient)(implicit ec: ExecutionContext) extends Controller {
  
 val tracksService = new TracksService
 val spotifyService = new SpotifyService("", ws)

 def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def rateTrack = Action.async { request =>
    val json = request.body.asJson.get
    val spotifyId = (json \ "spotify-id").as[String]
    val name = (json \ "spotify-id").as[String]
    val stars = (json \ "stars").as[Int]
    
    tracksService.rateAndInsertTrack(spotifyId, name, stars).map(_ => Ok(Json.toJson(Map("success" -> true))))
  }
  
  def isTrackRated(spotifyId: String) = Action.async { request =>
    tracksService.hasTrackBeenRated(spotifyId).map(f => Ok(Json.toJson(Map("is-track-rated" -> !f.isEmpty))))
  }
  
  def getAllTracks() = Action.async { request =>
    tracksService.getTracks.map(f => Ok(Json.toJson(f)))
  }
  
  def getTracks(stars: String) = Action.async { request =>
    tracksService.getTracksWithStars(stars.toInt).map(f => Ok(Json.toJson(f)))
  }
  
  def sync() = Action.async { request =>
    //val token = (request.body.asJson.get \ "token").as[String]
    
    val f = for {
      dbPlaylists <- tracksService.getPlaylists
      sPlaylists  <- spotifyService.getPlaylists
    } yield (dbPlaylists, sPlaylists)
    
    val syncService = new SyncService(tracksService, spotifyService)
    
    f.flatMap(f => syncService.sync(f._1, f._2).map(_ => Ok(Json.toJson(Map("success" -> true)))))
  }
  
  
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
