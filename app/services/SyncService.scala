package services

import util.Domain.Playlists
import util.Domain.Playlist
import models.services.TracksService
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class SyncService(tracksService: TracksService, spotifyService: SpotifyService)(implicit ec: ExecutionContext) {
  
  def syncSpotify(dbPlaylists: Playlists, sPlaylists: Playlists) = {
    val playlists = mergePlaylists(dbPlaylists, sPlaylists)
    updateSpotify(sPlaylists, playlists)
  }
  
  private def mergePlaylists(dbPlaylists: Playlists, sPlaylists: Playlists) = {
    val stars1 = Playlist(sPlaylists.stars1.id, dbPlaylists.stars1.tracks)
    val stars2 = Playlist(sPlaylists.stars2.id, dbPlaylists.stars2.tracks)
    val stars3 = Playlist(sPlaylists.stars3.id, dbPlaylists.stars3.tracks)
    val stars4 = Playlist(sPlaylists.stars4.id, dbPlaylists.stars4.tracks)
    val stars5 = Playlist(sPlaylists.stars5.id, dbPlaylists.stars5.tracks)
    Playlists(stars1, stars2, stars3, stars4, stars5)
  }
  
  private def updateSpotify(sPlaylists: Playlists, playlists: Playlists) =
    spotifyService.deletePlaylists(sPlaylists).flatMap(f1 => spotifyService.addTracksToPlaylists(playlists))
}