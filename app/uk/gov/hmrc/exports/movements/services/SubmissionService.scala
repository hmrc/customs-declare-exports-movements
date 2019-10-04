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

package uk.gov.hmrc.exports.movements.services

import javax.inject.{Inject, Singleton}
import play.api.http.Status.ACCEPTED
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation
import uk.gov.hmrc.exports.movements.models.submissions.{Submission, SubmissionFactory}
import uk.gov.hmrc.exports.movements.repositories.SubmissionRepository
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  submissionRepository: SubmissionRepository,
  submissionFactory: SubmissionFactory,
  wcoMapper: ILEMapper
)(implicit executionContext: ExecutionContext) {

  def submitRequest(context: SubmissionRequestContext)(implicit hc: HeaderCarrier): Future[Unit] =
    customsInventoryLinkingExportsConnector.sendInventoryLinkingRequest(context.eori, context.requestXml).flatMap {

      case CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId)) =>
        val newSubmission = submissionFactory.buildMovementSubmission(conversationId, context)

        submissionRepository
          .insert(newSubmission)
          .map(_ => (): Unit)

      case CustomsInventoryLinkingResponse(status, conversationId) =>
        Future.failed(
          new CustomsInventoryLinkingUpstreamException(
            status,
            conversationId,
            "Non Accepted status returned by Customs Inventory Linking Exports"
          )
        )
    }

  def submitConsolidation(eori: String, consolidation: Consolidation)(implicit hc: HeaderCarrier): Future[Unit] = {
    val requestXml = wcoMapper.generateConsolidationXml(consolidation)

    submitRequest(consolidation.buildSubmissionContext(eori, requestXml))
  }

  def getSubmissionsByEori(eori: String): Future[Seq[Submission]] = submissionRepository.findByEori(eori)

  def getSubmissionByConversationId(conversationId: String): Future[Option[Submission]] =
    submissionRepository.findByConversationId(conversationId)

}
