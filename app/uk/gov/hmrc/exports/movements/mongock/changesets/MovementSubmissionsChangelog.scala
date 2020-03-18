package uk.gov.hmrc.exports.movements.mongock.changesets

import com.github.cloudyrock.mongock.{ChangeLog, ChangeSet}
import com.mongodb.client.MongoDatabase

@ChangeLog
class MovementSubmissionsChangelog {
  private val collection = "movementSubmissions"

  @ChangeSet(order = "001", id = "Movements DB Baseline", author = "Paulo Monteiro")
  def dbBaseline(db: MongoDatabase): Unit = {}
}
