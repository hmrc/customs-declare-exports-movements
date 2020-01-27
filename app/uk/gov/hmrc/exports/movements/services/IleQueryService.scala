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
import uk.gov.hmrc.exports.movements.errors.TimeoutError
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.misc.IleQueryTimeoutCalculator
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.movements.IleQueryRequest
import uk.gov.hmrc.exports.movements.models.notifications.exchange.IleQueryResponseExchange
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.IleQuery
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import uk.gov.hmrc.exports.movements.repositories.{NotificationRepository, SearchParameters, SubmissionRepository}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IleQueryService @Inject()(
  ileMapper: ILEMapper,
  submissionRepository: SubmissionRepository,
  notificationRepository: NotificationRepository,
  ileConnector: CustomsInventoryLinkingExportsConnector,
  ileQueryTimeoutCalculator: IleQueryTimeoutCalculator
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def submit(ileQueryRequest: IleQueryRequest)(implicit hc: HeaderCarrier): Future[String] = {
    val requestXml = ileMapper.generateIleQuery(ileQueryRequest.ucrBlock)

    ileConnector.submit(ileQueryRequest, requestXml).flatMap {

      case CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId)) =>
        val submission = Submission(
          eori = ileQueryRequest.eori,
          providerId = ileQueryRequest.providerId,
          conversationId = conversationId,
          ucrBlocks = Seq(ileQueryRequest.ucrBlock),
          actionType = IleQuery
        )

        submissionRepository.insert(submission).map(_ => conversationId)

      case CustomsInventoryLinkingResponse(status, conversationId) =>
        logger.warn(s"ILE Query failed with conversation-id=[$conversationId] and status [$status]")
        Future.failed(
          new CustomsInventoryLinkingUpstreamException(status, conversationId, "Non Accepted status returned by Customs Inventory Linking Exports")
        )
    }
  }

  def fetchResponses(searchParameters: SearchParameters): Future[Either[TimeoutError, Seq[IleQueryResponseExchange]]] =
    submissionRepository.findBy(searchParameters).flatMap {
      case Nil => Future.successful(Right(Seq.empty))

      case submission :: Nil =>
        if (ileQueryTimeoutCalculator.hasQueryTimedOut(submission)) {
          logger
            .info(s"Timeout occurred while waiting for ILE Query Response notification with conversation ID = [${searchParameters.conversationId.getOrElse("")}]")
          Future.successful(Left(TimeoutError(s"This ILE Query is too old to get information about it")))
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
