/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.exports.movements.migrations.changelogs.movementNotifications

import com.mongodb.client.MongoDatabase
import org.bson.{BsonType, Document}
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{bsonType, eq => feq}
import org.mongodb.scala.model.UpdateOneModel
import org.mongodb.scala.model.Updates.set
import play.api.Logger
import uk.gov.hmrc.exports.movements.migrations.changelogs.{MigrationDefinition, MigrationInformation}

import java.time.ZonedDateTime
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class ConvertNotificationTimestampToDateType extends MigrationDefinition {

  private val logger = Logger(this.getClass)

  private val collectionName = "movementNotifications"
  private val queryBatchSize = 10
  private val updateBatchSize = 10

  private val TIMESTAMP_RECEIVED = "timestampReceived"
  private val INDEX_ID = "_id"

  override val migrationInformation: MigrationInformation =
    MigrationInformation(id = "CEDS-3905 Convert string dates to BSON date in notifications", order = 2, author = "Tom Robinson", runAlways = true)

  override def migrationFunction(db: MongoDatabase): Unit = {
    logger.info(s"Applying '${migrationInformation.id}' db migration...")

    val queryFilter = bsonType(TIMESTAMP_RECEIVED, BsonType.STRING)

    getDocumentsToUpdate(db, queryFilter).map { document =>
      val timestamp = document.get(TIMESTAMP_RECEIVED, classOf[String])
      val newTimestamp = Try(ZonedDateTime.parse(timestamp).toInstant)

      val documentId = document.get(INDEX_ID)
      val filter = feq(INDEX_ID, documentId)
      val update = newTimestamp match {
        case Success(value) => set(TIMESTAMP_RECEIVED, BsonDateTime(value.toEpochMilli))
        case Failure(_) =>
          logger.warn(s"Unable to update $TIMESTAMP_RECEIVED for document with id $documentId")
          set(TIMESTAMP_RECEIVED, timestamp)
      }

      new UpdateOneModel[Document](filter, update)
    }.grouped(updateBatchSize).zipWithIndex.foreach { case (requests, idx) =>
      logger.info(s"Updating batch no. $idx...")

      db.getCollection(collectionName).bulkWrite(seqAsJavaList(requests))
      logger.info(s"Updated batch no. $idx")
    }

    logger.info(s"Applying '${migrationInformation.id}' db migration... Done.")
  }

  private def getDocumentsToUpdate(db: MongoDatabase, filter: Bson): Iterator[Document] = asScalaIterator(
    db.getCollection(collectionName).find(filter).batchSize(queryBatchSize).iterator
  )
}
