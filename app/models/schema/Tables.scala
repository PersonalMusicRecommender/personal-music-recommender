package models.schema
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.PostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Track.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Track
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param spotifyId Database column spotify_id SqlType(text)
   *  @param name Database column name SqlType(text)
   *  @param stars Database column stars SqlType(int2), Default(None) */
  case class TrackRow(id: Int, spotifyId: String, name: String, stars: Option[Short] = None)
  /** GetResult implicit for fetching TrackRow objects using plain SQL queries */
  implicit def GetResultTrackRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[Short]]): GR[TrackRow] = GR{
    prs => import prs._
    TrackRow.tupled((<<[Int], <<[String], <<[String], <<?[Short]))
  }
  /** Table description of table track. Objects of this class serve as prototypes for rows in queries. */
  class Track(_tableTag: Tag) extends Table[TrackRow](_tableTag, "track") {
    def * = (id, spotifyId, name, stars) <> (TrackRow.tupled, TrackRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(spotifyId), Rep.Some(name), stars).shaped.<>({r=>import r._; _1.map(_=> TrackRow.tupled((_1.get, _2.get, _3.get, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column spotify_id SqlType(text) */
    val spotifyId: Rep[String] = column[String]("spotify_id")
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column stars SqlType(int2), Default(None) */
    val stars: Rep[Option[Short]] = column[Option[Short]]("stars", O.Default(None))
  }
  /** Collection-like TableQuery object for table Track */
  lazy val Track = new TableQuery(tag => new Track(tag))
}
