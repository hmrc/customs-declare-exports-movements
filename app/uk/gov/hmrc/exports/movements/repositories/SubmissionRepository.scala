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
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionRepository @Inject()(implicit mc: ReactiveMongoComponent, ec: ExecutionContext)
    extends ReactiveRepository[Submission, BSONObjectID]("movementSubmissions", mc.mongoConnector.db, Submission.formats) {

  override def indexes: Seq[Index] = Seq(
    Index(Seq("eori" -> IndexType.Ascending), name = Some("eoriIdx")),
    Index(Seq("providerId" -> IndexType.Ascending), name = Some("providerIdIdx")),
    Index(Seq("conversationId" -> IndexType.Ascending), unique = true, name = Some("conversationIdIdx"))
  )

  def findBy(searchParameters: SearchParameters): Future[Seq[Submission]] = {
    val query = Json.toJson(searchParameters).as[JsObject]
    collection
      .find(query, projection = None)(ImplicitBSONHandlers.JsObjectDocumentWriter, ImplicitBSONHandlers.JsObjectDocumentWriter)
      .cursor[Submission](ReadPreference.primaryPreferred)
      .collect(maxDocs = -1, FailOnError[Seq[Submission]]())
  }

}
