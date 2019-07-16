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
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.controllers.actions.AuthenticatedController
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.models.{AuthorizedSubmissionRequest, ErrorResponse, ValidatedHeadersMovementsRequest}
import uk.gov.hmrc.exports.movements.services.MovementSubmissionService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

@Singleton
class MovementSubmissionController @Inject()(
  appConfig: AppConfig,
  authConnector: AuthConnector,
  headerValidator: HeaderValidator,
  movementsService: MovementSubmissionService,
  cc: ControllerComponents
) extends AuthenticatedController(authConnector, cc) {

  def submitMovement(): Action[AnyContent] =
    authorisedAction(bodyParser = xmlOrEmptyBody) { implicit request =>
      implicit val headers: Map[String, String] = request.headers.toSimpleMap
      processMovementsRequest()
    }

  private def xmlOrEmptyBody: BodyParser[AnyContent] =
    BodyParser(
      rq =>
        parse.tolerantXml(rq).map {
          case Right(xml) => Right(AnyContentAsXml(xml))
          case _          => Left(ErrorResponse.ErrorInvalidPayload.XmlResult)
      }
    )

  private def processMovementsRequest()(
    implicit request: AuthorizedSubmissionRequest[AnyContent],
    hc: HeaderCarrier,
    headers: Map[String, String]
  ): Future[Result] =
    headerValidator.validateAndExtractMovementSubmissionHeaders match {
      case Right(vhr) =>
        request.body.asXml match {
          case Some(xml) =>
            processSave(vhr, xml).recoverWith {
              case e: Exception =>
                Logger.error(s"problem calling declaration api ${e.getMessage}")
                Future.successful(ErrorResponse.ErrorInternalServerError.XmlResult)
            }
          case None =>
            Logger.error("body is not xml")
            Future.successful(ErrorResponse.ErrorInvalidPayload.XmlResult)
        }
      case Left(_) =>
        Logger.error("Invalid Headers found")
        Future.successful(ErrorResponse.ErrorGenericBadRequest.XmlResult)
    }

  private def processSave(
    vhr: ValidatedHeadersMovementsRequest,
    xml: NodeSeq
  )(implicit request: AuthorizedSubmissionRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    movementsService
      .handleMovementSubmission(request.eori.value, vhr.ducr, vhr.movementType, xml)

  def getMovements: Action[AnyContent] =
    authorisedAction(parse.default) { implicit authorizedRequest =>
      movementsService.getMovementsByEori(authorizedRequest.eori.value)
    }
}
