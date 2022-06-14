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

package uk.gov.hmrc.exports.movements.routines

import com.google.inject.Singleton
import com.mongodb.client.{MongoClient, MongoClients}
import play.api.Logging
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.migrations.changelogs.movementNotifications.MakeParsedDataOptional
import uk.gov.hmrc.exports.movements.migrations.{ExportsMigrationTool, LockManagerConfig, MigrationsRegistry}

import javax.inject.Inject
import scala.concurrent.Future

@Singleton
class MigrationRoutine @Inject() (appConfig: AppConfig)(implicit rec: RoutinesExecutionContext) extends Routine with Logging {

  private val (client, mongoDatabase) = createMongoClient
  private val db = client.getDatabase(mongoDatabase)

  override def execute(): Future[Unit] = Future {
    logger.info("Exports Migration feature enabled. Starting migration with ExportsMigrationTool")
    migrateWithExportsMigrationTool()
  }

  private def createMongoClient: (MongoClient, String) = {
    val (mongoUri, _) = {
      val sslParamPos = appConfig.mongodbUri.lastIndexOf('?'.toInt)
      if (sslParamPos > 0) appConfig.mongodbUri.splitAt(sslParamPos) else (appConfig.mongodbUri, "")
    }
    val (_, mongoDatabase) = mongoUri.splitAt(mongoUri.lastIndexOf('/'.toInt))
    (MongoClients.create(appConfig.mongodbUri), mongoDatabase.drop(1))
  }

  val lockMaxTries = 10
  val lockMaxWaitMillis = minutesToMillis(5)
  val lockAcquiredForMillis = minutesToMillis(3)

  private def migrateWithExportsMigrationTool(): Unit = {
    val lockManagerConfig = LockManagerConfig(lockMaxTries, lockMaxWaitMillis, lockAcquiredForMillis)
    val migrationsRegistry = MigrationsRegistry().register(new MakeParsedDataOptional())
    val migrationTool = ExportsMigrationTool(db, migrationsRegistry, lockManagerConfig)

    migrationTool.execute()
    client.close()
  }

  private def minutesToMillis(minutes: Int): Long = minutes * 60L * 1000L
}
