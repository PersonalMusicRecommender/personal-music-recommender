package util

object Domain {
  case class PlaylistData(id: String, name: String)
  case class RatedTrack(name: String, spotifyId: String, stars: Int)
  case class Playlist(id: String, tracks: Set[RatedTrack])
  case class Playlists(stars1: Playlist, stars2: Playlist, stars3: Playlist, stars4: Playlist, stars5: Playlist)
}