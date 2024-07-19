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

package uk.gov.hmrc.exports.movements.repositories

import com.mongodb.client.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit.DAYS
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

@Singleton
class UnparsedNotificationRepository @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[Notification](
      mongoComponent = mongoComponent,
      collectionName = "unparsedNotifications",
      domainFormat = Notification.format,
      indexes = NotificationRepository.indexes
    ) with RepositoryOps[Notification] {

  override def classTag: ClassTag[Notification] = implicitly[ClassTag[Notification]]
  implicit val executionContext: ExecutionContext = ec
}

object UnparsedNotificationRepository {

  private val ttlDays = 122

  val indexes: Seq[IndexModel] = List(IndexModel(ascending("timestampReceived"), IndexOptions().name("ttl").expireAfter(ttlDays, DAYS)))
}
