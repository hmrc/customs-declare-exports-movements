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

package uk.gov.hmrc.exports.movements.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.models.ErrorResponse
import uk.gov.hmrc.exports.movements.controllers.actions.AuthenticatedController
import uk.gov.hmrc.exports.movements.controllers.request.MovementRequest
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.Eori
import uk.gov.hmrc.exports.movements.models.submissions.ActionType
import uk.gov.hmrc.exports.movements.services.SubmissionService
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovementsController @Inject()(
  authConnector: AuthConnector,
  submissionService: SubmissionService,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends AuthenticatedController(authConnector, controllerComponents) {

  private val logger = Logger(this.getClass)

  def submitMovement(): Action[MovementRequest] = authorisedAction(parse.json[MovementRequest]) { implicit request =>
    val data: InventoryLinkingMovementRequest = request.body.createMovementRequest(request.eori)
    request.body.choice match {
      case "EAL" =>
        submitMovementSubmission(data: InventoryLinkingMovementRequest, request.eori, ActionType.Arrival)
      case "EDL" =>
        submitMovementSubmission(data: InventoryLinkingMovementRequest, request.eori, ActionType.Departure)
    }
  }

  private def submitMovementSubmission(data: InventoryLinkingMovementRequest, eori: Eori, actionType: ActionType)(
    implicit hc: HeaderCarrier
  ): Future[Result] =
    submissionService
      .submitRequest(
        SubmissionRequestContext(
          eori = eori.value,
          actionType = actionType,
          requestXml = xml.XML.loadString(data.toXml)
        )
      )
      .map(_ => Accepted("Movement Submission submitted successfully"))
      .recover {
        case e: CustomsInventoryLinkingUpstreamException =>
          ErrorResponse.errorInternalServerError(e.getMessage).XmlResult
      }

}
