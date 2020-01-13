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

package uk.gov.hmrc.exports.movements.services

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.Status.ACCEPTED
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation
import uk.gov.hmrc.exports.movements.models.movements.Movement
import uk.gov.hmrc.exports.movements.models.submissions.{Submission, SubmissionFactory}
import uk.gov.hmrc.exports.movements.repositories.{SearchParameters, SubmissionRepository}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  submissionRepository: SubmissionRepository,
  submissionFactory: SubmissionFactory,
  ileMapper: ILEMapper
)(implicit executionContext: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def submit(movement: Movement)(implicit hc: HeaderCarrier): Future[Unit] = {
    val requestXml = ileMapper.generateInventoryLinkingMovementRequestXml(movement)

    customsInventoryLinkingExportsConnector.submit(movement, requestXml).flatMap {
      case CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId)) =>
        logger.info(s"Movement Submission Accepted with conversation-id=[$conversationId]")
        val newSubmission =
          submissionFactory.buildMovementSubmission(movement.eori, movement.providerId, conversationId, requestXml, movement)

        submissionRepository.insert(newSubmission).map(_ => (): Unit)

      case CustomsInventoryLinkingResponse(status, conversationId) =>
        logger.warn(s"Movement Submission failed with conversation-id=[$conversationId] and status [$status]")
        Future.failed(
          new CustomsInventoryLinkingUpstreamException(status, conversationId, "Non Accepted status returned by Customs Inventory Linking Exports")
        )
    }
  }

  def submit(consolidation: Consolidation)(implicit hc: HeaderCarrier): Future[Unit] = {
    val requestXml = ileMapper.generateConsolidationXml(consolidation)

    customsInventoryLinkingExportsConnector.submit(consolidation, requestXml).flatMap {
      case CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId)) =>
        logger.info(s"Consolidation Submission Accepted with conversation-id=[$conversationId]")
        val newSubmission =
          submissionFactory
            .buildConsolidationSubmission(consolidation.eori, consolidation.providerId, conversationId, requestXml, consolidation.consolidationType)

        submissionRepository
          .insert(newSubmission)
          .map(_ => (): Unit)
      case CustomsInventoryLinkingResponse(status, conversationId) =>
        logger.warn(s"Consolidation Submission failed with conversation-id=[$conversationId] and status [$status]")
        Future.failed(
          new CustomsInventoryLinkingUpstreamException(status, conversationId, "Non Accepted status returned by Customs Inventory Linking Exports")
        )
    }
  }

  def getSubmissions(searchParameters: SearchParameters): Future[Seq[Submission]] =
    submissionRepository.findBy(searchParameters)

  def getSingleSubmission(searchParameters: SearchParameters): Future[Option[Submission]] =
    submissionRepository.findBy(searchParameters).map(_.headOption)

}
