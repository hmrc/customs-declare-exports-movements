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
import play.api.Logger
import play.api.libs.json.JsString
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.exports.movements.models.MovementSubmissions
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovementSubmissionRepository @Inject()(implicit mc: ReactiveMongoComponent, ec: ExecutionContext)
    extends ReactiveRepository[MovementSubmissions, BSONObjectID](
      "movements",
      mc.mongoConnector.db,
      MovementSubmissions.formats,
      objectIdFormats
    ) {

  override def indexes: Seq[Index] = Seq(
    Index(Seq("eori" -> IndexType.Ascending), name = Some("eoriIdx")),
    Index(Seq("conversationId" -> IndexType.Ascending), unique = true, name = Some("conversationIdIdx"))
  )

  def findByEori(eori: String): Future[Seq[MovementSubmissions]] =
    find("eori" -> JsString(eori))

  def getByConversationId(conversationId: String): Future[Option[MovementSubmissions]] =
    find("conversationId" -> JsString(conversationId)).map(_.headOption)

  def getByEoriAndDucr(eori: String, ducr: String): Future[Option[MovementSubmissions]] =
    find("eori" -> JsString(eori), "ucr" -> JsString(ducr)).map(_.headOption)

  def save(movementSubmission: MovementSubmissions): Future[Boolean] =
    insert(movementSubmission).map { res =>
      if (!res.ok)
        // $COVERAGE-OFF$Trivial
        Logger.error("Error during inserting movement result " + res.writeErrors.mkString("--"))
      // $COVERAGE-ON$
      res.ok
    }
}