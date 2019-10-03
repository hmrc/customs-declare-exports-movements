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
import uk.gov.hmrc.exports.movements.controllers.actions.AuthenticatedController
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.consolidation.{Consolidation, ConsolidationRequest}
import uk.gov.hmrc.exports.movements.models.submissions.ActionType
import uk.gov.hmrc.exports.movements.models.{AuthorizedSubmissionRequest, ErrorResponse}
import uk.gov.hmrc.exports.movements.services.SubmissionService
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConsolidationController @Inject()(
  authConnector: AuthConnector,
  consolidationService: SubmissionService,
  cc: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends AuthenticatedController(authConnector, cc) {

  private val logger = Logger(this.getClass)

  val shutMucr: Action[AnyContentAsXml] = movementConsolidationAction(ActionType.ShutMucr)

  val associateMucr: Action[AnyContentAsXml] = movementConsolidationAction(ActionType.DucrAssociation)

  val disassociateMucr: Action[AnyContentAsXml] = movementConsolidationAction(ActionType.DucrDisassociation)

  private def movementConsolidationAction(action: ActionType): Action[AnyContentAsXml] =
    authorisedAction(bodyParser = xmlOrEmptyBody(action)) { implicit request =>
      submitMovementConsolidation(action)
    }

  private def xmlOrEmptyBody(action: ActionType): BodyParser[AnyContentAsXml] =
    BodyParser(
      rq =>
        parse.tolerantXml(rq).map {
          case Right(xml) => Right(AnyContentAsXml(xml))
          case _ =>
            logger.warn(s"Bad Consolidation Request: Invalid XML. Action ${action.value}")
            Left(ErrorResponse.ErrorInvalidPayload.XmlResult)
      }
    )

  private def submitMovementConsolidation(
    actionType: ActionType
  )(implicit hc: HeaderCarrier, request: AuthorizedSubmissionRequest[AnyContentAsXml]): Future[Result] = {
    val context =
      SubmissionRequestContext(eori = request.eori.value, actionType = actionType, requestXml = request.body.xml)
    forwardMovementConsolidationRequest(context)
  }

  private def forwardMovementConsolidationRequest(
    context: SubmissionRequestContext
  )(implicit hc: HeaderCarrier): Future[Result] =
    consolidationService
      .submitRequest(context)
      .map(_ => Accepted("Consolidation request submitted successfully"))
      .recover {
        case e: CustomsInventoryLinkingUpstreamException =>
          ErrorResponse.errorInternalServerError(e.getMessage).XmlResult
      }

  def submitConsolidation(): Action[ConsolidationRequest] = authorisedAction(parse.json[ConsolidationRequest]) {
    implicit request =>
      consolidationService
        .submitConsolidation(request.eori.value, request.body.consolidation())
        .map(_ => Accepted(request.body.consolidation()))
  }
}
