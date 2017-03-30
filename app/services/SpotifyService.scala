package services

import play.api.libs.json.Json
import javax.inject._
import play.api.libs.ws.WSClient
import scala.concurrent.ExecutionContext
import play.api.libs.json.JsValue
import util.Domain.PlaylistData
import util.Domain.Playlists
import util.Domain.Playlist
import scala.concurrent.Future
import play.api.libs.ws.WSResponse

class SpotifyService(token: String, ws: WSClient)(implicit ec: ExecutionContext) {
  
  private val ClientId = "c82f92150a734dce90ecb0a00863eaf5"
  private val ClientSecret = "a485d356b2bc423a89e8c6dfc7183a5d"
  
  private val UserId = "lsgaleana"
  private val Headers = "Authorization" -> s"Bearer $token"
  
  private val PlaylistNames = Set("1 stars", "2 stars", "3 stars", "4 stars", "5 stars")
  
  def getPlaylists = {
    getPlaylistsData.flatMap(playlistsData => {
      val id1 = playlistsData.find(_.name.contains("1")).get.id
      val id2 = playlistsData.find(_.name.contains("2")).get.id
      val id3 = playlistsData.find(_.name.contains("3")).get.id
      val id4 = playlistsData.find(_.name.contains("4")).get.id
      val id5 = playlistsData.find(_.name.contains("5")).get.id
      
      for {
        stars1 <- requestPlaylistTracks(id1)
        stars2 <- requestPlaylistTracks(id2)
        stars3 <- requestPlaylistTracks(id3)
        stars4 <- requestPlaylistTracks(id4)
        stars5 <- requestPlaylistTracks(id5)
      } yield Playlists(
          Playlist(id1, findTracks(stars1.json)),
          Playlist(id2, findTracks(stars2.json)),
          Playlist(id3, findTracks(stars3.json)),
          Playlist(id4, findTracks(stars4.json)),
          Playlist(id5, findTracks(stars5.json))
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
    
  private def requestPlaylistTracks(id: String) = {
    val url = s"https://api.spotify.com/v1/users/$UserId/playlists/$id/tracks"
    ws.url(url).withHeaders(Headers).get
  }
  
  private def findTracks(json: JsValue) =
    (json \ "items").as[Seq[JsValue]].map(j => (j \ "track" \ "id").as[String]).toSet
  
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
      jsonTracks =  buildJsonTracks(uGroups)
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
      jsonTracks =  buildJsonTracks(uGroups)
    } yield requestAddTracksToPlaylist(playlist.id, jsonTracks))
  
  private def requestAddTracksToPlaylist(id: String, tracks: JsValue) = {
    val url = s"https://api.spotify.com/v1/users/$UserId/playlists/$id/tracks"
    ws.url(url).withHeaders(Headers, "Content-Type" -> "application/json").post(tracks)
  }
  
  private def buildJsonTracks(tracks: Set[String]) =
    Json.obj("uris" -> tracks.map(uri => s"spotify:track:$uri"))
  
}