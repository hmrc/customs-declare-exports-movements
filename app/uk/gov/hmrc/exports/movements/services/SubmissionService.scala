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
import play.api.Logger
import play.api.http.Status.ACCEPTED
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import uk.gov.hmrc.exports.movements.repositories.SubmissionRepository
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Try}
import scala.xml.NodeSeq

@Singleton
class SubmissionService @Inject()(
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  submissionRepository: SubmissionRepository
) {

  private val logger = Logger(this.getClass)

  def submitRequest(
    context: SubmissionRequestContext
  )(implicit hc: HeaderCarrier): Future[Either[String, Unit]] =
    customsInventoryLinkingExportsConnector.sendInventoryLinkingRequest(context.eori, context.requestXml).flatMap {

      case CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId)) =>
        val newSubmission = Submission(
          eori = context.eori,
          conversationId = conversationId,
          ucrBlocks = extractUcrListFrom(context.requestXml),
          actionType = context.actionType
        )

        submissionRepository
          .insert(newSubmission)
          .map(_ => Right((): Unit))
          .recover {
            case exc: Throwable =>
              logger.error(exc.getMessage)
              Left(exc.getMessage)
          }

      case CustomsInventoryLinkingResponse(status, _) =>
        logger
          .error(s"Customs Inventory Linking Exports returned $status for Eori: ${context.eori}")
        Future.successful(Left("Non Accepted status returned by Customs Inventory Linking Exports"))
    }

  private def extractUcrListFrom(request: NodeSeq): Seq[UcrBlock] =
    Try {
      val ucrBlocksNodes = request \ "ucrBlock"
      ucrBlocksNodes.map { node =>
        val ucr = (node \ "ucr").text
        val ucrType = (node \ "ucrType").text
        UcrBlock(ucr = ucr, ucrType = ucrType)
      }
    }.recoverWith {
      case exc =>
        logger.error(s"Exception thrown during UCR extraction from request: ${exc.getMessage}")
        Failure(exc)
    }.getOrElse(Seq.empty)

  def getSubmissionsByEori(eori: String): Future[Seq[Submission]] = submissionRepository.findByEori(eori)

}
