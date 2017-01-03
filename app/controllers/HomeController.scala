package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models.services.TracksService
import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(implicit ec: ExecutionContext) extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def rateTrack = Action.async { request =>
    val json = request.body.asJson.get
    
    val tracksService = new TracksService
    tracksService.rateAndInsertTrack(
        (json \ "spotify-id").as[String], (json \ "name").as[String], (json \ "stars").as[Short]
    ).map(_ => Ok)
  }

}
