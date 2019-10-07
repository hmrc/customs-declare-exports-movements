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

import javax.inject.Singleton
import uk.gov.hmrc.exports.movements.models.XmlTags
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationType.ConsolidationType
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext

import scala.xml.{Node, NodeSeq}

@Singleton
class SubmissionFactory {

  def buildMovementSubmission(conversationId: String, context: SubmissionRequestContext): Submission =
    Submission(
      eori = context.eori,
      conversationId = conversationId,
      ucrBlocks = extractUcrListFrom(context.requestXml),
      actionType = context.actionType
    )

  def buildSubmission(
    eori: String,
    conversationId: String,
    requestXml: Node,
    consolidationType: ConsolidationType
  ): Submission =
    Submission(
      eori = eori,
      conversationId = conversationId,
      ucrBlocks = extractUcrListFrom(requestXml),
      actionType = ActionType.fromConsolidationType(consolidationType)
    )

  private def extractUcrListFrom(request: NodeSeq): Seq[UcrBlock] = {
    val ucrBlocks = (request \ XmlTags.ucrBlock).map { node =>
      val ucr = (node \ XmlTags.ucr).text
      val ucrType = (node \ XmlTags.ucrType).text
      UcrBlock(ucr = ucr, ucrType = ucrType)
    }

    val masterUcr = (request \ XmlTags.masterUCR).map(node => UcrBlock(ucr = node.text, ucrType = "M"))

    masterUcr ++ ucrBlocks
  }
}
