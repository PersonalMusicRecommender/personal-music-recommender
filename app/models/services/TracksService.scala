package models.services

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery
import models.schema.Tables._

class TracksService() {
  
  def rateAndInsertTrack(spotifyId: String, name: String, stars: Short) = {
    val db = Database.forConfig("pg-postgres")
    val tracks = TableQuery[Track]
    
    val insertAction = DBIO.seq(Track.map(t => (t.spotifyId, t.name, t.stars)) returning Track.map(_.id) += {
      (spotifyId, name, Some(stars))
    })
    
    db.run(insertAction)
  }
  
}