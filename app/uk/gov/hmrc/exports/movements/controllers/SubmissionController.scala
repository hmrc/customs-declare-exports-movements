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
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.movements.controllers.actions.AuthenticatedController
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.models.submissions.ActionType
import uk.gov.hmrc.exports.movements.models.{AuthorizedSubmissionRequest, ErrorResponse}
import uk.gov.hmrc.exports.movements.services.SubmissionService
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionController @Inject()(
  authConnector: AuthConnector,
  headerValidator: HeaderValidator,
  submissionService: SubmissionService,
  cc: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends AuthenticatedController(authConnector, cc) {

  private val logger = Logger(this.getClass)

  def submitArrival(): Action[AnyContent] =
    authorisedAction(bodyParser = xmlOrEmptyBody) { implicit request =>
      submitMovementSubmission(ActionType.Arrival)
    }

  def submitDeparture(): Action[AnyContent] =
    authorisedAction(bodyParser = xmlOrEmptyBody) { implicit request =>
      submitMovementSubmission(ActionType.Departure)
    }

  private def xmlOrEmptyBody: BodyParser[AnyContent] =
    BodyParser(
      rq =>
        parse.tolerantXml(rq).map {
          case Right(xml) => Right(AnyContentAsXml(xml))
          case _ =>
            logger.error("Invalid xml payload")
            Left(ErrorResponse.ErrorInvalidPayload.XmlResult)
      }
    )

  private def submitMovementSubmission(
    actionType: ActionType
  )(implicit request: AuthorizedSubmissionRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    request.body.asXml match {
      case Some(requestXml) =>
        val context =
          SubmissionRequestContext(eori = request.eori.value, actionType = actionType, requestXml = requestXml)
        forwardMovementSubmissionRequest(context)
      case None =>
        logger.error("Body is not xml")
        Future.successful(ErrorResponse.ErrorInvalidPayload.XmlResult)
    }

  private def forwardMovementSubmissionRequest(
    context: SubmissionRequestContext
  )(implicit hc: HeaderCarrier): Future[Result] =
    submissionService
      .submitRequest(
        SubmissionRequestContext(eori = context.eori, actionType = context.actionType, requestXml = context.requestXml)
      )
      .map {
        case Right(_)       => Accepted("Movement Submission submitted successfully")
        case Left(errorMsg) => ErrorResponse.errorInternalServerError(errorMsg).XmlResult
      }

  def getAllSubmissions: Action[AnyContent] =
    authorisedAction(parse.default) { implicit authorizedRequest =>
      submissionService.getSubmissionsByEori(authorizedRequest.eori.value).map(movements => Ok(Json.toJson(movements)))
    }
}
