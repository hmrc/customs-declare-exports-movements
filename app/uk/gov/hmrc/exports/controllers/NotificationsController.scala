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

package uk.gov.hmrc.exports.controllers

import com.google.inject.Singleton
import javax.inject.Inject
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.config.AppConfig
import uk.gov.hmrc.exports.metrics.ExportsMetrics
import uk.gov.hmrc.exports.metrics.MetricIdentifiers._
import uk.gov.hmrc.exports.models._
import uk.gov.hmrc.exports.repositories.MovementNotificationsRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.wco.dec._
import uk.gov.hmrc.wco.dec.inventorylinking.movement.response.InventoryLinkingMovementResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

@Singleton
class NotificationsController @Inject()(
  appConfig: AppConfig,
  authConnector: AuthConnector,
  headerValidator: HeaderValidator,
  movementNotificationsRepository: MovementNotificationsRepository,
  metrics: ExportsMetrics
) extends ExportController(authConnector) {

  def saveMovement(): Action[NodeSeq] = Action.async(parse.xml) { implicit request =>
    metrics.startTimer(movementMetric)
    headerValidator
      .validateAndExtractMovementNotificationHeaders(request.headers.toSimpleMap) match {
      case Right(extractedHeaders) =>
        getMovementNotificationFromRequest(extractedHeaders)
          .fold(Future.successful(ErrorResponse.ErrorInvalidPayload.XmlResult)) {
            saveMovement(_)
          }
      case Left(errorResponse) => Future.successful(errorResponse.XmlResult)
    }
  }

  private def saveMovement(notification: MovementNotification)(implicit hc: HeaderCarrier): Future[Result] =
    movementNotificationsRepository
      .save(notification)
      .map {
        case true =>
          metrics.incrementCounter(movementMetric)
          Accepted
        case _ =>
          metrics.incrementCounter(movementMetric)
          InternalServerError(NotificationFailedErrorResponse.toXml())
      }

  private def getMovementNotificationFromRequest(
    vhnar: MovementNotificationApiRequest
  )(implicit request: Request[NodeSeq], hc: HeaderCarrier): Option[MovementNotification] = {
    val parseResult = Try[InventoryLinkingMovementResponse] {
      InventoryLinkingMovementResponse.fromXml(request.body.toString)
    }
    parseResult match {
      case Success(response) =>
        val notification = MovementNotification(
          conversationId = vhnar.conversationId.value,
          eori = vhnar.eori.value,
          movementResponse = response
        )
        Some(notification)
      case Failure(ex) =>
        Logger.error("error parsing movementNotification", ex)
        None
    }

  }

  private def buildStatus(responses: Seq[Response]): Option[String] =
    responses.map { response =>
      (response.functionCode, response.status.flatMap(_.nameCode).headOption) match {
        case ("11", Some(nameCode)) if nameCode == "39" || nameCode == "41" =>
          s"11$nameCode"
        case _ => response.functionCode
      }
    }.headOption
}
