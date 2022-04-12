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

package uk.gov.hmrc.exports.movements.repositories

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsNull, JsObject, JsString, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONNull, BSONObjectID}
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationRepository @Inject()(mc: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends ReactiveRepository[Notification, BSONObjectID]("movementNotifications", mc.mongoConnector.db, Notification.format, objectIdFormats) {

  override lazy val collection: JSONCollection =
    mongo().collection[JSONCollection](collectionName, failoverStrategy = RepositorySettings.failoverStrategy)

  override def indexes: Seq[Index] = Seq(
    Index(Seq("dateTimeReceived" -> IndexType.Ascending), name = Some("dateTimeReceivedIdx")),
    Index(Seq("conversationId" -> IndexType.Ascending), name = Some("conversationIdIdx")),
    Index(Seq("data" -> IndexType.Ascending), name = Some("dataMissingIdx"), partialFilter = Some(BSONDocument("data" -> BSONNull)))
  )

  def findByConversationIds(conversationIds: Seq[String]): Future[Seq[Notification]] =
    conversationIds match {
      case Nil => Future.successful(Seq.empty)
      case _   => find("conversationId" -> Json.obj("$in" -> conversationIds.map(JsString)))
    }

  def findUnparsedNotifications(): Future[Seq[Notification]] = find("data" -> JsNull)

  def update(id: BSONObjectID, notification: Notification): Future[Option[Notification]] = {
    val query = _id(id)
    val update = Json.toJsObject(notification)

    performUpdate(query, update)
  }

  private def performUpdate(query: JsObject, update: JsObject): Future[Option[Notification]] =
    findAndUpdate(query, update, fetchNewObject = true).map { updateResult =>
      if (updateResult.value.isEmpty) {
        updateResult.lastError.foreach(_.err.foreach(errorMsg => logger.error(s"Problem during database update: $errorMsg")))
      }
      updateResult.result[Notification]
    }
}
