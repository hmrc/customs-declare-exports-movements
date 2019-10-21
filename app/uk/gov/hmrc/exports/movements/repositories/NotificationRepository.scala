/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.Cursor.FailOnError
import reactivemongo.api.ReadPreference
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationRepository @Inject()(mc: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends ReactiveRepository[Notification, BSONObjectID]("movementNotifications", mc.mongoConnector.db, Notification.format, objectIdFormats) {

  override def indexes: Seq[Index] = Seq(
    Index(Seq("dateTimeReceived" -> IndexType.Ascending), name = Some("dateTimeReceivedIdx")),
    Index(Seq("conversationId" -> IndexType.Ascending), name = Some("conversationIdIdx"))
  )

  def findBy(queryParameters: QueryParameters): Future[Seq[Notification]] = {
    val query = Json.toJson(queryParameters).as[JsObject]
    collection
      .find(query, projection = None)(ImplicitBSONHandlers.JsObjectDocumentWriter, ImplicitBSONHandlers.JsObjectDocumentWriter)
      .cursor[Notification](ReadPreference.primaryPreferred)
      .collect(maxDocs = -1, FailOnError[Seq[Notification]]())
  }

}
