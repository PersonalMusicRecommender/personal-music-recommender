package services

import util.Domain.Playlists
import util.Domain.Playlist
import models.services.TracksService
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class SyncService(tracksService: TracksService, spotifyService: SpotifyService)(implicit ec: ExecutionContext) {
  
  def sync() =
    for {
      sPlaylists   <- spotifyService.getPlaylists
      _            <- syncDB(sPlaylists)
      dbPlaylists  <- tracksService.getPlaylists
      f            <- syncSpotify(dbPlaylists, sPlaylists)
    } yield f
  
  private def syncSpotify(dbPlaylists: Playlists, sPlaylists: Playlists) = {
    val playlists = mergePlaylists(sPlaylists, dbPlaylists)
    spotifyService.deletePlaylists(sPlaylists).flatMap(_ => spotifyService.addTracksToPlaylists(playlists))
  }
  
  private def mergePlaylists(sPlaylists: Playlists, dbPlaylists: Playlists) = {
    Playlists(
        Playlist(sPlaylists.stars1.id, dbPlaylists.stars1.tracks),
        Playlist(sPlaylists.stars2.id, dbPlaylists.stars2.tracks),
        Playlist(sPlaylists.stars3.id, dbPlaylists.stars3.tracks),
        Playlist(sPlaylists.stars4.id, dbPlaylists.stars4.tracks),
        Playlist(sPlaylists.stars5.id, dbPlaylists.stars5.tracks)
    )
  }
  
  private def syncDB(sPlaylists: Playlists) =
    for {
      _               <- tracksService.updateTracksFromPlaylists(sPlaylists)
      newDBPlaylists  <- tracksService.getPlaylists
      playlistsToAdd  = removeDBFromSpotify(sPlaylists, newDBPlaylists)
      f               <- tracksService.addTracksFromPlaylists(playlistsToAdd)
    } yield f
  
  private def removeDBFromSpotify(sPlaylists: Playlists, dbPlaylists: Playlists) =
    Playlists(
      Playlist(null, sPlaylists.stars1.tracks -- dbPlaylists.stars1.tracks),
      Playlist(null, sPlaylists.stars2.tracks -- dbPlaylists.stars2.tracks),
      Playlist(null, sPlaylists.stars3.tracks -- dbPlaylists.stars3.tracks),
      Playlist(null, sPlaylists.stars4.tracks -- dbPlaylists.stars4.tracks),
      Playlist(null, sPlaylists.stars5.tracks -- dbPlaylists.stars5.tracks)
    )
}