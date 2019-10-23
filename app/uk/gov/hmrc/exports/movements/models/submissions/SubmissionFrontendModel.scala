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

package uk.gov.hmrc.exports.movements.models.submissions

import java.time.Instant
import java.util.UUID

import play.api.libs.json._
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock

case class SubmissionFrontendModel(
  uuid: String = UUID.randomUUID().toString,
  eori: String,
  conversationId: String,
  ucrBlocks: Seq[UcrBlock],
  actionType: ActionType,
  requestTimestamp: Instant = Instant.now()
)

object SubmissionFrontendModel {
  implicit val formats = Json.format[SubmissionFrontendModel]

  def apply(submission: Submission): SubmissionFrontendModel = SubmissionFrontendModel(
    uuid = submission.uuid,
    eori = submission.eori,
    conversationId = submission.conversationId,
    ucrBlocks = submission.ucrBlocks,
    actionType = submission.actionType,
    requestTimestamp = submission.requestTimestamp
  )
}