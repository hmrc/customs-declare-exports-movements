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

  def shutMucr(): Action[AnyContent] =
    authorisedAction(bodyParser = xmlOrEmptyBody) { implicit request =>
      submitMovementConsolidation(ActionType.ShutMucr)
    }

  def associateMucr(): Action[AnyContent] =
    authorisedAction(bodyParser = xmlOrEmptyBody) { implicit request =>
      submitMovementConsolidation(ActionType.DucrAssociation)
    }

  def disassociateMucr(): Action[AnyContent] =
    authorisedAction(bodyParser = xmlOrEmptyBody) { implicit request =>
      submitMovementConsolidation(ActionType.DucrDisassociation)
    }

  private def xmlOrEmptyBody: BodyParser[AnyContent] =
    BodyParser(
      rq =>
        parse.tolerantXml(rq).map {
          case Right(xml) => Right(AnyContentAsXml(xml))
          case _ =>
            logger.warn("Invalid xml payload")
            Left(ErrorResponse.ErrorInvalidPayload.XmlResult)
      }
    )

  private def submitMovementConsolidation(
    actionType: ActionType
  )(implicit hc: HeaderCarrier, request: AuthorizedSubmissionRequest[AnyContent]): Future[Result] =
    request.body.asXml match {
      case Some(requestXml) =>
        val context =
          SubmissionRequestContext(eori = request.eori.value, actionType = actionType, requestXml = requestXml)
        forwardMovementConsolidationRequest(context)
      case None =>
        logger.warn("Body is not xml")
        Future.successful(ErrorResponse.ErrorInvalidPayload.XmlResult)
    }

  private def forwardMovementConsolidationRequest(
    context: SubmissionRequestContext
  )(implicit hc: HeaderCarrier): Future[Result] =
    consolidationService.submitRequest(context).map {
      case Right(_)       => Accepted("Consolidation request submitted successfully")
      case Left(errorMsg) => ErrorResponse.errorInternalServerError(errorMsg).XmlResult
    }

}
