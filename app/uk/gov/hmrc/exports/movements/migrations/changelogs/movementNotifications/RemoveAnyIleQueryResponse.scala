/*
 * Copyright 2023 HM Revenue & Customs
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
import org.mongodb.scala.model.Filters
import play.api.Logging
import uk.gov.hmrc.exports.movements.migrations.changelogs.{MigrationDefinition, MigrationInformation}
import uk.gov.hmrc.exports.movements.models.notifications.parsers.IleQueryResponseParser

class RemoveAnyIleQueryResponse extends MigrationDefinition with Logging {

  override val migrationInformation: MigrationInformation =
    MigrationInformation(
      id = "CEDS-5880 Remove from the repository all IleQueryResponse notifications",
      order = 5,
      author = "Lucio Biondi",
      runAlways = true
    )

  override def migrationFunction(db: MongoDatabase): Unit = {
    logger.info(s"Applying '${migrationInformation.id}' db migration...")

    val filter = Filters.eq("data.responseType", IleQueryResponseParser.responseTypeIle)
    val result = db.getCollection("movementNotifications").deleteMany(filter)

    logger.info(s"Removed ${result.getDeletedCount} IleQueryResponse notifications")
    logger.info(s"Applying '${migrationInformation.id}' db migration... Done.")
  }
}
