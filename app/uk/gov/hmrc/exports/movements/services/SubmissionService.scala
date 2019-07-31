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
import play.api.mvc.Result
import play.api.mvc.Results.{Accepted, InternalServerError}
import play.mvc.Http.Status.ACCEPTED
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.models.Submission
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.repositories.SubmissionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

@Singleton
class SubmissionService @Inject()(
  linkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  movementsRepo: SubmissionRepository
) {

  // TODO return Option[String] as conversation ID and handle result in Controller
  def handleMovementSubmission(eori: String, ucr: String, actionType: String, xml: NodeSeq)(
    implicit hc: HeaderCarrier
  ): Future[Result] =
    linkingExportsConnector
      .sendInventoryLinkingRequest(eori, xml)
      .flatMap(
        response =>
          response.status match {
            case ACCEPTED =>
              response.conversationId.fold({
                Logger.info(s"No ConversationID returned for submission with Eori: $eori")
                Future.successful(InternalServerError("No conversation Id Returned"))
              }) { conversationId =>
                persistMovementsData(eori, conversationId, Seq(UcrBlock(ucr = ucr, ucrType = "")), actionType).map(
                  result =>
                    if (result) {
                      Accepted("Movement Submission submitted and persisted ok")
                    } else {
                      InternalServerError("Unable to persist data something bad happened")
                  }
                )
              }
            case _ =>
              Logger
                .info(s"Non Accepted status ${response.status} returned by Customs Declaration Service for Eori: $eori")
              Future.successful(InternalServerError("Non Accepted status returned by Customs Declaration Service"))
        }
      )

  private def persistMovementsData(
    eori: String,
    conversationId: String,
    ucrBlocks: Seq[UcrBlock],
    actionType: String
  ): Future[Boolean] = {
    val movementSubmission =
      Submission(eori = eori, conversationId = conversationId, ucrBlocks = ucrBlocks, actionType = actionType)
    movementsRepo
      .save(movementSubmission)
      .map(res => {
        if (res) Logger.debug("movement submission data saved to DB")
        else Logger.error("error  saving movement submission data to DB")
        res
      })
  }

  def getMovementsByEori(eori: String): Future[Seq[Submission]] = movementsRepo.findByEori(eori)

}
