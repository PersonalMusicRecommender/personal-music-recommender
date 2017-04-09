package services

import play.api.libs.json.Json
import javax.inject._
import play.api.libs.ws.WSClient
import scala.concurrent.ExecutionContext
import play.api.libs.json.JsValue
import util.Domain.PlaylistData
import util.Domain.Playlists
import util.Domain.Playlist
import util.Domain.RatedTrack
import scala.concurrent.Future
import play.api.libs.ws.WSResponse

class SpotifyService(ws: WSClient)(implicit ec: ExecutionContext) {
  
  private val ClientId = "c82f92150a734dce90ecb0a00863eaf5"
  private val ClientSecret = "a485d356b2bc423a89e8c6dfc7183a5d"
  
  private val UserId = "lsgaleana"
  var token = ""
  private def Headers = "Authorization" -> s"Bearer $token"
  
  private val PlaylistNames = Set("1 stars", "2 stars", "3 stars", "4 stars", "5 stars")
  
  def getPlaylists = {
    getPlaylistsData.flatMap(playlistsData => {
      val id1 = playlistsData.find(_.name.contains("1")).get.id
      val id2 = playlistsData.find(_.name.contains("2")).get.id
      val id3 = playlistsData.find(_.name.contains("3")).get.id
      val id4 = playlistsData.find(_.name.contains("4")).get.id
      val id5 = playlistsData.find(_.name.contains("5")).get.id
      
      for {
        stars1 <- getPlaylistTracks(id1, 1, 0, Set.empty)
        stars2 <- getPlaylistTracks(id2, 2, 0, Set.empty)
        stars3 <- getPlaylistTracks(id3, 3, 0, Set.empty)
        stars4 <- getPlaylistTracks(id4, 4, 0, Set.empty)
        stars5 <- getPlaylistTracks(id5, 5, 0, Set.empty)
      } yield Playlists(
          Playlist(id1, stars1),
          Playlist(id2, stars2),
          Playlist(id3, stars3),
          Playlist(id4, stars4),
          Playlist(id5, stars5)
       )
    })
  }
  
  private def getPlaylistsData =
    requestPlaylistsData.map(r => findPlaylists(r.json).filter(p => PlaylistNames.contains(p.name)))
    
  private def requestPlaylistsData = {
    val url = "https://api.spotify.com/v1/me/playlists"
    ws.url(url).withHeaders(Headers).get
  }
  
  private def findPlaylists(json: JsValue) =
    (json \ "items").as[Seq[JsValue]].map(j => PlaylistData((j \ "id").as[String], (j \ "name").as[String]))
    
  private def getPlaylistTracks(id: String, stars: Int, offset: Int, tracks: Set[RatedTrack]):
  Future[Set[RatedTrack]] = {
    val f = requestPlaylistTracks(id, offset)
    f.flatMap(r => {
      val moreTracks = findTracks(r.json, stars)
      if(moreTracks.isEmpty)
        Future { tracks }
      else
        getPlaylistTracks(id, stars, offset + 100, tracks ++ moreTracks)
    })
  }
    
  private def requestPlaylistTracks(id: String, offset: Int) = {
    val url = s"https://api.spotify.com/v1/users/$UserId/playlists/$id/tracks?offset=$offset"
    ws.url(url).withHeaders(Headers).get
  }
  
  private def findTracks(json: JsValue, stars: Int) =
    (json \ "items").as[Seq[JsValue]].map(j => RatedTrack(
         (j \ "track" \ "name").as[String],
        (j \ "track" \ "id").as[String],
        stars
    )).toSet
  
  def deletePlaylists(playlists: Playlists) =
    for {
      r1 <- deletePlaylistTracks(playlists.stars1)
      r2 <- deletePlaylistTracks(playlists.stars2)
      r3 <- deletePlaylistTracks(playlists.stars3)
      r4 <- deletePlaylistTracks(playlists.stars4)
      r5 <- deletePlaylistTracks(playlists.stars5)
    } yield (r1, r2, r3, r4, r5)
    
  private def deletePlaylistTracks(playlist: Playlist) =
    Future.sequence(for {
      uGroups    <- playlist.tracks.grouped(100)
      jsonTracks =  buildJsonTracks(uGroups.map(_.spotifyId))
    } yield requestDeletePlaylistTracks(playlist.id, jsonTracks))
    
  private def requestDeletePlaylistTracks(id: String, tracks: JsValue) = {
    val url = s"https://api.spotify.com/v1/users/$UserId/playlists/$id/tracks"
    ws.url(url).withHeaders(Headers).withBody(tracks).delete
  }
    
  def addTracksToPlaylists(playlists: Playlists) =
    for {
      r1 <- addTracksToPlaylist(playlists.stars1)
      r2 <- addTracksToPlaylist(playlists.stars2)
      r3 <- addTracksToPlaylist(playlists.stars3)
      r4 <- addTracksToPlaylist(playlists.stars4)
      r5 <- addTracksToPlaylist(playlists.stars5)
    } yield (r1, r2, r3, r4, r5)
  
  private def addTracksToPlaylist(playlist: Playlist) =
    Future.sequence(for {
      uGroups    <- playlist.tracks.grouped(100)
      jsonTracks =  buildJsonTracks(uGroups.map(_.spotifyId))
    } yield requestAddTracksToPlaylist(playlist.id, jsonTracks))
  
  private def requestAddTracksToPlaylist(id: String, tracks: JsValue) = {
    val url = s"https://api.spotify.com/v1/users/$UserId/playlists/$id/tracks"
    ws.url(url).withHeaders(Headers, "Content-Type" -> "application/json").post(tracks)
  }
  
  private def buildJsonTracks(tracks: Set[String]) =
    Json.obj("uris" -> tracks.map(uri => s"spotify:track:$uri"))
  
}