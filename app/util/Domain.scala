package util

object Domain {
  case class RatedTrack(name: String, spotifyId: String, stars: Int)
  
  case class PlaylistData(id: String, name: String)
  case class Playlist(id: String, tracks: Set[String])
  case class Playlists(stars1: Playlist, stars2: Playlist, stars3: Playlist, stars4: Playlist, stars5: Playlist)
}