/*
 * Copyright 2020 HM Revenue & Customs
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
import org.bson.Document
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{exists, eq => feq}
import org.mongodb.scala.model.UpdateOneModel
import org.mongodb.scala.model.Updates.{combine, set, unset}
import play.api.Logger
import uk.gov.hmrc.exports.movements.migrations.changelogs.{MigrationDefinition, MigrationInformation}

import scala.collection.JavaConverters._

class MakeParsedDataOptional extends MigrationDefinition {

  private val logger = Logger(this.getClass)

  private val collectionName = "movementNotifications"
  private val queryBatchSize = 10
  private val updateBatchSize = 10

  private val RESPONSE_TYPE = "responseType"
  private val INDEX_ID = "_id"
  private val DATA = "data"

  override val migrationInformation: MigrationInformation =
    MigrationInformation(id = "CEDS-2801 Make parsed Notification data optional", order = 1, author = "Maciej Rewera", runAlways = true)

  override def migrationFunction(db: MongoDatabase): Unit = {
    logger.info(s"Applying '${migrationInformation.id}' db migration...")

    val queryFilter = exists(RESPONSE_TYPE)

    getDocumentsToUpdate(db, queryFilter).map { document =>
      val newDataElement = createDataElement(document)

      val documentId = document.get(INDEX_ID)
      val filter = feq(INDEX_ID, documentId)
      val update = combine(unset(RESPONSE_TYPE), set(DATA, newDataElement))

      new UpdateOneModel[Document](filter, update)
    }.grouped(updateBatchSize).zipWithIndex.foreach {
      case (requests, idx) =>
        logger.info(s"Updating batch no. $idx...")

        db.getCollection(collectionName).bulkWrite(seqAsJavaList(requests))
        logger.info(s"Updated batch no. $idx")
    }

    logger.info(s"Applying '${migrationInformation.id}' db migration... Done.")
  }

  private def createDataElement(document: Document): Document = {
    val data = document.get(DATA, classOf[Document])
    val responseType = document.get(RESPONSE_TYPE).asInstanceOf[String]

    data.append(RESPONSE_TYPE, responseType)
  }

  private def getDocumentsToUpdate(db: MongoDatabase, filter: Bson): Iterator[Document] = asScalaIterator(
    db.getCollection(collectionName).find(filter).batchSize(queryBatchSize).iterator
  )
}
