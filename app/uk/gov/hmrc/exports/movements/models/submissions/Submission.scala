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

package uk.gov.hmrc.exports.movements.models.submissions

import play.api.libs.json._
import uk.gov.hmrc.exports.movements.models.UserIdentification
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.{ConsolidationType, MovementType}
import uk.gov.hmrc.exports.movements.services.UcrBlockBuilder.extractUcrBlocksForSubmissionFrom
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import java.util.UUID
import scala.xml.Node

case class Submission(
  uuid: String = UUID.randomUUID.toString,
  override val eori: String,
  override val providerId: Option[String],
  conversationId: String,
  ucrBlocks: Seq[UcrBlock],
  actionType: ActionType,
  requestTimestamp: Instant = Instant.now
) extends UserIdentification

object Submission {

  implicit private val formatInstant: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val format: OFormat[Submission] = Json.format[Submission]

  def apply(eori: String, providerId: Option[String], conversationId: String, requestXml: Node, movementType: MovementType): Submission =
    Submission(
      eori = eori,
      providerId = providerId,
      conversationId = conversationId,
      ucrBlocks = extractUcrBlocksForSubmissionFrom(requestXml),
      actionType = movementType
    )

  def apply(eori: String, providerId: Option[String], conversationId: String, requestXml: Node, consolidationType: ConsolidationType): Submission =
    Submission(
      eori = eori,
      providerId = providerId,
      conversationId = conversationId,
      ucrBlocks = extractUcrBlocksForSubmissionFrom(requestXml),
      actionType = consolidationType
    )
}
