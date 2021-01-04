/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.{ConsolidationType, MovementType}
import uk.gov.hmrc.exports.movements.services.UcrBlockBuilder

import scala.xml.Node

@Singleton
class SubmissionFactory @Inject()(ucrBlockBuilder: UcrBlockBuilder) {

  def buildMovementSubmission(
    eori: String,
    providerId: Option[String] = None,
    conversationId: String,
    requestXml: Node,
    movementType: MovementType
  ): Submission =
    Submission(
      eori = eori,
      providerId = providerId,
      conversationId = conversationId,
      ucrBlocks = ucrBlockBuilder.extractUcrBlocksForSubmissionFrom(requestXml),
      actionType = movementType
    )

  def buildConsolidationSubmission(
    eori: String,
    providerId: Option[String] = None,
    conversationId: String,
    requestXml: Node,
    consolidationType: ConsolidationType
  ): Submission =
    Submission(
      eori = eori,
      providerId = providerId,
      conversationId = conversationId,
      ucrBlocks = ucrBlockBuilder.extractUcrBlocksForSubmissionFrom(requestXml),
      actionType = consolidationType
    )
}
