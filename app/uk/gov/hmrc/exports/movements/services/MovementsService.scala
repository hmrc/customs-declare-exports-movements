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
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.{Accepted, InternalServerError, Ok}
import play.mvc.Http.Status.ACCEPTED
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.models.MovementSubmissions
import uk.gov.hmrc.exports.movements.repositories.MovementsRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

@Singleton
class MovementsService @Inject()(
  linkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  movementsRepo: MovementsRepository
) {

  def handleMovementSubmission(eori: String, ducr: String, mucr: Option[String], movementType: String, xml: NodeSeq)(
    implicit hc: HeaderCarrier
  ): Future[Result] =
    linkingExportsConnector
      .sendMovementRequest(eori, xml.toString())
      .flatMap(
        response =>
          response.status match {
            case ACCEPTED =>
              response.conversationId.fold({
                Logger.info(s"No ConversationID returned for submission with Eori: $eori")
                Future.successful(InternalServerError("No conversation Id Returned"))
              }) { conversationId =>
                persistMovementsData(eori, conversationId, ducr, mucr, movementType).map(
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

  def getMovementsByEori(eori: String): Future[Result] =
    movementsRepo.findByEori(eori).map { movements =>
      Ok(Json.toJson(movements))
    }

  private def persistMovementsData(
    eori: String,
    conversationId: String,
    ducr: String,
    mucr: Option[String],
    movementType: String
  ): Future[Boolean] = {
    val movementSubmission = MovementSubmissions(eori, conversationId, ducr, mucr, movementType)
    movementsRepo
      .save(movementSubmission)
      .map(res => {
        if (res) Logger.debug("movement submission data saved to DB")
        else Logger.error("error  saving movement submission data to DB")
        res
      })
  }

}
