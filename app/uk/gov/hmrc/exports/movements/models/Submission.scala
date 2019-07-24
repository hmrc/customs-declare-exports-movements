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

package uk.gov.hmrc.exports.movements.models

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.mongoEntity

case class Submission(
  eori: String,
  conversationId: String,
  ucr: String,
  movementType: String,
  id: BSONObjectID = BSONObjectID.generate(),
  submittedTimestamp: Long = System.currentTimeMillis(),
  status: Option[String] = Some("Pending")
)

object Submission {
  implicit val objectIdFormats = ReactiveMongoFormats.objectIdFormats
  implicit val formats = mongoEntity {
    Json.format[Submission]
  }
}
