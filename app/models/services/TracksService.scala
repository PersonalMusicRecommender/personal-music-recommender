package models.services

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.PostgresDriver.api._
import models.schema.Tables._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import util.Domain.RatedTrack
import util.Domain.Playlists
import util.Domain.Playlist

class TracksService() {
  
  private val db = Database.forConfig("pg-postgres")
  private val tracks = TableQuery[Track]
    
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
  
  def getTracks() =
    db.run(tracks.result).map(_.map(track => RatedTrack(track.name, track.spotifyId, track.stars.get)))
  
  def getTracksWithStars(stars: Int) =
    db.run(tracks.filter(_.stars === stars).result)
    .map(_.map(track => RatedTrack(track.name, track.spotifyId, track.stars.get)))
  
  def getPlaylists() = {
    getTracks.map(tracks => {
      val tGroups = tracks.groupBy(_.stars).map(t => (t._1, t._2.map(_.spotifyId).toSet))
      Playlists(
          Playlist(null, tGroups.getOrElse(1, Set.empty)),
          Playlist(null, tGroups.getOrElse(2, Set.empty)),
          Playlist(null, tGroups.getOrElse(3, Set.empty)),
          Playlist(null, tGroups.getOrElse(4, Set.empty)),
          Playlist(null, tGroups.getOrElse(5, Set.empty))
      )
    })
  }
  
}