package models.services

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.PostgresDriver.api._
import models.schema.Tables._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global


class TracksService() {
  
  val db = Database.forConfig("pg-postgres")
  val tracks = TableQuery[Track]
    
  def rateAndInsertTrack(spotifyId: String, name: String, stars: Int) =
    hasTrackBeenRated(spotifyId).map(result => {
      if(result.isEmpty) {
        val insertAction = DBIO.seq(Track.map(t => (t.spotifyId, t.name, t.stars)) returning Track.map(_.id) += {
          (spotifyId, name, Some(stars))
        })
        
        Await.result(db.run(insertAction), Duration.Inf)
      }
    })
  
  def hasTrackBeenRated(spotifyId: String) = db.run(tracks.filter(_.spotifyId === spotifyId).result)
  
  def getTracks() = db.run(tracks.result)
  
  def getTracksWithStars(stars: Int) = db.run(tracks.filter(_.stars === stars).result)
  
}