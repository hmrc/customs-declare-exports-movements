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

package uk.gov.hmrc.exports.movements.services

import javax.inject.{Inject, Singleton}
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
import uk.gov.hmrc.exports.movements.repositories.{IleQuerySubmissionRepository, NotificationRepository, SearchParameters}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IleQueryService @Inject()(
  ileMapper: IleMapper,
  ileQuerySubmissionRepository: IleQuerySubmissionRepository,
  notificationRepository: NotificationRepository,
  ileConnector: CustomsInventoryLinkingExportsConnector,
  ileQueryTimeoutCalculator: IleQueryTimeoutCalculator
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

        ileQuerySubmissionRepository.insert(submission).map(_ => conversationId)

      case CustomsInventoryLinkingResponse(status, conversationId) =>
        logger.warn(s"ILE Query failed with conversation-id=[$conversationId] and status [$status]")
        Future.failed(
          new CustomsInventoryLinkingUpstreamException(status, conversationId, "Non Accepted status returned by Customs Inventory Linking Exports")
        )
    }
  }

  def fetchResponses(searchParameters: SearchParameters): Future[Either[TimeoutError, Seq[IleQueryResponseExchange]]] =
    ileQuerySubmissionRepository.findBy(searchParameters).flatMap {
      case Nil => Future.successful(Right(Seq.empty))

      case submission :: Nil =>
        if (ileQueryTimeoutCalculator.hasQueryTimedOut(submission)) {
          logger
            .info(
              s"Timeout occurred while waiting for ILE Query Response notification with Conversation ID = [${searchParameters.conversationId.getOrElse("")}]"
            )
          Future.successful(Left(TimeoutError("Timeout occurred while waiting for ILE Query Response notification")))
        } else {
          getNotificationsConverted(Seq(submission.conversationId))
        }

      case _ => throw new IllegalStateException(s"Found multiple Submissions for given searchParameters: ${searchParameters}")
    }

  private def getNotificationsConverted(conversationIds: Seq[String]): Future[Either[TimeoutError, Seq[IleQueryResponseExchange]]] =
    for {
      notifications <- notificationRepository.findByConversationIds(conversationIds)
      result <- Future.successful(notifications.map(IleQueryResponseExchange(_)))
    } yield Right(result)

}
