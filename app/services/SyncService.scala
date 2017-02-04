package services

import util.Domain.Playlists
import util.Domain.Playlist
import models.services.TracksService
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class SyncService(tracksService: TracksService, spotifyService: SpotifyService)(implicit ec: ExecutionContext) {
  
  def sync(dbPlaylists: Playlists, sPlaylists: Playlists) = {
    val playlists = mergePlaylists(dbPlaylists, sPlaylists)
    updateSpotify(sPlaylists, playlists)
  }
  
  private def mergePlaylists(dbPlaylists: Playlists, sPlaylists: Playlists) = {
    val stars1 = Playlist(sPlaylists.stars1.id, sPlaylists.stars1.tracks ++ dbPlaylists.stars1.tracks)
    val stars2 = Playlist(sPlaylists.stars2.id, sPlaylists.stars2.tracks ++ dbPlaylists.stars2.tracks)
    val stars3 = Playlist(sPlaylists.stars3.id, sPlaylists.stars3.tracks ++ dbPlaylists.stars3.tracks)
    val stars4 = Playlist(sPlaylists.stars4.id, sPlaylists.stars4.tracks ++ dbPlaylists.stars4.tracks)
    val stars5 = Playlist(sPlaylists.stars5.id, sPlaylists.stars5.tracks ++ dbPlaylists.stars5.tracks)
    Playlists(stars1, stars2, stars3, stars4, stars5)
  }
  
  private def updateSpotify(sPlaylists: Playlists, playlists: Playlists) =
    spotifyService.deletePlaylists(sPlaylists).flatMap(f1 => spotifyService.addTracksToPlaylists(playlists))
}