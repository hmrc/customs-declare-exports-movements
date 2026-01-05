/*
 * Copyright 2024 HM Revenue & Customs
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
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit.HOURS
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@Singleton
class IleQueryResponseRepository @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[Notification](
      mongoComponent = mongoComponent,
      collectionName = "ileQueryResponses",
      domainFormat = Notification.format,
      indexes = IleQueryResponseRepository.indexes
    ) with RepositoryOps[Notification] {

  override def classTag: ClassTag[Notification] = implicitly[ClassTag[Notification]]
  val executionContext: ExecutionContext = ec

  def findByConversationIds(conversationIds: Seq[String]): Future[Seq[Notification]] =
    conversationIds match {
      case Nil => Future.successful(Seq.empty)

      case _ =>
        val query = Json.obj("conversationId" -> Json.obj("$in" -> conversationIds.map(id => JsString(id))))
        collection.find(BsonDocument(query.toString)).toFuture()
    }
}

object IleQueryResponseRepository {

  private val ttlHours = 1L

  val indexes: Seq[IndexModel] = List(
    IndexModel(ascending("conversationId"), IndexOptions().name("conversationIdIdx")),
    IndexModel(ascending("timestampReceived"), IndexOptions().name("ttl").expireAfter(ttlHours, HOURS))
  )
}
