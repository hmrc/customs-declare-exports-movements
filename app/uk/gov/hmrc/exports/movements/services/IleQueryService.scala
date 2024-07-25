/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.Logger
import play.api.http.Status.ACCEPTED
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.errors.TimeoutError
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.misc.IleQueryTimeoutCalculator
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.movements.IleQueryRequest
import uk.gov.hmrc.exports.movements.models.notifications.exchange.IleQueryResponseExchange
import uk.gov.hmrc.exports.movements.models.submissions.IleQuerySubmission
import uk.gov.hmrc.exports.movements.repositories.{IleQueryResponseRepository, IleQuerySubmissionRepository, NotificationRepository, SearchParameters}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IleQueryService @Inject() (
  ileMapper: IleMapper,
  ileQuerySubmissionRepository: IleQuerySubmissionRepository,
  ileQueryResponseRepository: IleQueryResponseRepository,
  ileConnector: CustomsInventoryLinkingExportsConnector,
  ileQueryTimeoutCalculator: IleQueryTimeoutCalculator,
  notificationRepository: NotificationRepository
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def submit(ileQueryRequest: IleQueryRequest)(implicit hc: HeaderCarrier): Future[String] = {
    val requestXml = ileMapper.buildIleQuery(ileQueryRequest.ucrBlock)

    ileConnector.submit(ileQueryRequest, requestXml).flatMap {

      case CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId)) =>
        val submission = IleQuerySubmission(
          eori = ileQueryRequest.eori,
          providerId = ileQueryRequest.providerId,
          conversationId = conversationId,
          ucrBlock = ileQueryRequest.ucrBlock
        )

        ileQuerySubmissionRepository.insertOne(submission).map(_ => conversationId)

      case CustomsInventoryLinkingResponse(status, conversationId) =>
        logger.warn(s"ILE Query failed with conversation-id=[$conversationId] and status [$status]")
        val msg = "Non Accepted status returned by Customs Inventory Linking Exports"
        Future.failed(new CustomsInventoryLinkingUpstreamException(status, conversationId, msg))
    }
  }

  def fetchResponses(searchParameters: SearchParameters): Future[Either[TimeoutError, Seq[IleQueryResponseExchange]]] =
    ileQuerySubmissionRepository.findAll(searchParameters).flatMap {
      case Nil => Future.successful(Right(List.empty))

      case submission :: Nil =>
        if (ileQueryTimeoutCalculator.hasQueryTimedOut(submission)) {
          val id = searchParameters.conversationId.getOrElse("")
          logger.info(s"Timeout occurred while waiting for ILE Query Response notification with conversation-id=[$id]")
          Future.successful(Left(TimeoutError("Timeout occurred while waiting for ILE Query Response notification")))
        } else getNotificationsConverted(List(submission.conversationId))

      case _ => throw new IllegalStateException(s"Found multiple Submissions for given searchParameters: ${searchParameters}")
    }

  private def getNotificationsConverted(conversationIds: Seq[String]): Future[Either[TimeoutError, Seq[IleQueryResponseExchange]]] =
    ileQueryResponseRepository.findByConversationIds(conversationIds).flatMap { notifications =>
      if (notifications.nonEmpty) Future.successful(Right(notifications.map(IleQueryResponseExchange(_))))
      else
        /*
        When the 'ucr' for a requested 'conversationId' is not found the upstream send a
        'inventoryLinkingControlResponse' notification, to notify the error, that we store
        in the "movementNotifications" collection.

        Of course then the first lookup in the "ileQueryResponses" collection will always
        be unsuccessful, as this collection only contains 'inventoryLinkingQueryResponse'
        notifications. This is why when that is the case we need to do a further lookup in
        the "movementNotifications" collection.
         */
        notificationRepository.findByConversationIds(conversationIds).map { notifications =>
          Right(notifications.map(IleQueryResponseExchange(_)))
        }
    }
}
