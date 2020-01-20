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

package uk.gov.hmrc.exports.movements.repositories

import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.exports.movements.models.submissions.IleQuerySubmission
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext

class IleQueryRepository @Inject()(implicit mc: ReactiveMongoComponent, ec: ExecutionContext)
  extends ReactiveRepository[IleQuerySubmission, BSONObjectID]("ileQuerySubmissions", mc.mongoConnector.db, IleQuerySubmission.format) {

  override def indexes: Seq[Index] = Seq(
    Index(Seq("eori" -> IndexType.Ascending), name = Some("eoriIdx")),
    Index(Seq("providerId" -> IndexType.Ascending), name = Some("providerIdIdx")),
    Index(Seq("conversationId" -> IndexType.Ascending), unique = true, name = Some("conversationIdIdx"))
  )

}
