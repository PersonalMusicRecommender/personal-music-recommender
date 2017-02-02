package services

import play.api.libs.json.Json
import javax.inject._
import play.api.libs.ws.WSClient
import scala.concurrent.ExecutionContext
import play.api.libs.json.JsValue

class SpotifyService(token: String, ws: WSClient)(implicit ec: ExecutionContext) {
  
  val ClientId = "c82f92150a734dce90ecb0a00863eaf5"
  val ClientSecret = "a485d356b2bc423a89e8c6dfc7183a5d"
  
  val UserId = "lsgaleana"
  val Headers = "AUTHORIZATION" -> s"Bearer $token"
  
  val PlaylistNames = Set("1 stars", "2 stars", "3 stars", "4 stars", "5 stars")
  
  case class PlaylistData(id: String, name: String)
  case class Playlists(
      stars1: Seq[String],
      stars2: Seq[String],
      stars3: Seq[String],
      stars4: Seq[String],
      stars5: Seq[String]
  )
  
  def getStarPlaylists = {
    getStarPlaylistsData.flatMap(playlistsData => {
      for {
        stars1 <- requestPlaylistTracks(playlistsData(0).id)
        stars2 <- requestPlaylistTracks(playlistsData(1).id)
        stars3 <- requestPlaylistTracks(playlistsData(2).id)
        stars4 <- requestPlaylistTracks(playlistsData(3).id)
        stars5 <- requestPlaylistTracks(playlistsData(4).id)
      } yield Playlists(
          findTracks(stars1.json),
          findTracks(stars2.json),
          findTracks(stars3.json),
          findTracks(stars4.json),
          findTracks(stars5.json)
       )
    })
  }
  
  private def getStarPlaylistsData =
    requestPlaylistsData.map(r => findPlaylists(r.json).filter(p => PlaylistNames.contains(p.name)))
  
  private def requestPlaylistsData = {
    val url = "https://api.spotify.com/v1/me/playlists"
    ws.url(url).withHeaders(Headers).get
  }
  
  private def requestPlaylistTracks(id: String) = {
    val url = s"https://api.spotify.com/v1/users/$UserId/playlists/${id}/tracks"
    ws.url(url).withHeaders(Headers).get
  }
  
  private def findPlaylists(json: JsValue) =
    (json \ "items").as[Seq[JsValue]].map(j => PlaylistData((j \ "id").as[String], (j \ "name").as[String]))
    
  private def findTracks(json: JsValue) = (json \ "items").as[Seq[JsValue]].map(j => (j \ "id").as[String])
  
}