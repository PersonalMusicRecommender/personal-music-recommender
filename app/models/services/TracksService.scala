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
import scala.concurrent.Future

class TracksService() {
  
  private val db = Database.forConfig("pg-postgres")
  private val tracks = TableQuery[Track]
    
  def rateAndInsertTrack(spotifyId: String, name: String, stars: Int) = {
    val insertAction = DBIO.seq(Track.map(t => (t.spotifyId, t.name, t.stars)) returning Track.map(_.id) +=
      (spotifyId, name, Some(stars))
    )
    db.run(insertAction)
  }
  
  def hasTrackBeenRated(spotifyId: String) = db.run(tracks.filter(_.spotifyId === spotifyId).result).map(!_.isEmpty)
  
  def getTracks() =
    db.run(tracks.result).map(_.map(track => RatedTrack(track.name, track.spotifyId, track.stars.get)))
  
  def getTracksWithStars(stars: Int) =
    db.run(tracks.filter(_.stars === stars).result)
    .map(_.map(track => RatedTrack(track.name, track.spotifyId, track.stars.get)))
  
  def getPlaylists() = {
    getTracks.map(tracks => {
      val tGroups = tracks.groupBy(_.stars)
      Playlists(
          Playlist(null, tGroups.getOrElse(1, Set.empty).toSet),
          Playlist(null, tGroups.getOrElse(2, Set.empty).toSet),
          Playlist(null, tGroups.getOrElse(3, Set.empty).toSet),
          Playlist(null, tGroups.getOrElse(4, Set.empty).toSet),
          Playlist(null, tGroups.getOrElse(5, Set.empty).toSet)
      )
    })
  }
  
  def addTracksFromPlaylists(ps: Playlists) ={
    val actions1 = ps.stars1.tracks.map(t => rateAndInsertTrack(t.spotifyId, t.name, t.stars))
    val actions2 = ps.stars2.tracks.map(t => rateAndInsertTrack(t.spotifyId, t.name, t.stars))
    val actions3 = ps.stars3.tracks.map(t => rateAndInsertTrack(t.spotifyId, t.name, t.stars))
    val actions4 = ps.stars4.tracks.map(t => rateAndInsertTrack(t.spotifyId, t.name, t.stars))
    val actions5 = ps.stars5.tracks.map(t => rateAndInsertTrack(t.spotifyId, t.name, t.stars))
    Future.sequence(actions1 ++ actions2 ++ actions3 ++ actions4 ++ actions5)
  }
  
  def updateTracksFromPlaylists(ps: Playlists) = {
    val actions1 =
      ps.stars1.tracks.map(t => tracks.filter(_.spotifyId === t.spotifyId).map(_.stars).update(Some(1)))
    val actions2 =
      ps.stars2.tracks.map(t => tracks.filter(_.spotifyId === t.spotifyId).map(_.stars).update(Some(2)))
    val actions3 =
      ps.stars3.tracks.map(t => tracks.filter(_.spotifyId === t.spotifyId).map(_.stars).update(Some(3)))
    val actions4 =
      ps.stars4.tracks.map(t => tracks.filter(_.spotifyId === t.spotifyId).map(_.stars).update(Some(4)))
    val actions5 =
      ps.stars5.tracks.map(t => tracks.filter(_.spotifyId === t.spotifyId).map(_.stars).update(Some(5)))
    Future.sequence((actions1 ++ actions2 ++ actions3 ++ actions4 ++ actions5).map(db.run))
  }
  
}