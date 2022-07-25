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

import com.mongodb.client.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.exports.movements.models.submissions.IleQuerySubmission
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

@Singleton
class IleQuerySubmissionRepository @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[IleQuerySubmission](
      mongoComponent = mongoComponent,
      collectionName = "ileQuerySubmissions",
      domainFormat = IleQuerySubmission.format,
      indexes = IleQuerySubmissionRepository.indexes
    ) with RepositoryOps[IleQuerySubmission] {

  override def classTag: ClassTag[IleQuerySubmission] = implicitly[ClassTag[IleQuerySubmission]]
  implicit val executionContext = ec
}

object IleQuerySubmissionRepository {

  private val ttlSeconds = 60

  val indexes: Seq[IndexModel] = List(
    IndexModel(ascending("eori"), IndexOptions().name("eoriIdx")),
    IndexModel(ascending("providerId"), IndexOptions().name("providerIdIdx")),
    IndexModel(ascending("conversationId"), IndexOptions().name("conversationIdIdx").unique(true)),
    IndexModel(ascending("requestTimestamp"), IndexOptions().name("ttl").expireAfter(ttlSeconds, SECONDS))
  )
}
