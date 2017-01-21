package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models.services.TracksService
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json

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
  
  def getSpotifyIds() = Action.async { request =>
    tracksService.getSpotifyIds.map(f => Ok(Json.toJson(f)))
  }

}
