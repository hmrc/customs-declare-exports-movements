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
import uk.gov.hmrc.exports.movements.models.ErrorResponse
import uk.gov.hmrc.exports.movements.services.ConsolidationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

@Singleton
class ConsolidationController @Inject()(
  authConnector: AuthConnector,
  consolidationService: ConsolidationService,
  cc: ControllerComponents
) extends AuthenticatedController(authConnector, cc) {

  private val logger = Logger(this.getClass)

  def submitMovementConsolidation(): Action[AnyContent] =
    authorisedAction(bodyParser = xmlOrEmptyBody) { implicit request =>
      request.body.asXml match {
        case Some(requestXml) =>
          forwardMovementConsolidationRequest(request.eori.value, requestXml)
        case None =>
          logger.error("Body is not xml")
          Future.successful(ErrorResponse.ErrorInvalidPayload.XmlResult)
      }
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

  private def forwardMovementConsolidationRequest(eori: String, requestXml: NodeSeq)(
    implicit hc: HeaderCarrier
  ): Future[Result] =
    consolidationService.submitConsolidationRequest(eori, requestXml).map {
      case Right(_)       => Accepted("Consolidation request submitted successfully")
      case Left(errorMsg) => ErrorResponse.errorInternalServerError(errorMsg).XmlResult
    }

}
