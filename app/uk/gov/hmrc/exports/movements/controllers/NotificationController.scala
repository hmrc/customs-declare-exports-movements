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

import com.google.inject.Singleton
import javax.inject.Inject
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.controllers.actions.AuthenticatedController
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.metrics.ExportsMetrics
import uk.gov.hmrc.exports.movements.metrics.MetricIdentifiers._
import uk.gov.hmrc.exports.movements.models.notifications.{MovementNotification, MovementNotificationFactory}
import uk.gov.hmrc.exports.movements.services.NotificationService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

@Singleton
class NotificationController @Inject()(
  appConfig: AppConfig,
  authConnector: AuthConnector,
  headerValidator: HeaderValidator,
  metrics: ExportsMetrics,
  notificationService: NotificationService,
  notificationFactory: MovementNotificationFactory,
  cc: ControllerComponents
) extends AuthenticatedController(authConnector, cc) {

  val logger = Logger(this.getClass)

  def saveNotification(): Action[NodeSeq] = Action.async(parse.xml) { implicit request =>
    val timer = metrics.startTimer(movementMetric)
    saveNotification(request).map { res =>
      timer.stop()
      res
    }
  }

  private def saveNotification(request: Request[NodeSeq]): Future[Status] =
    headerValidator.validateAndExtractMovementNotificationHeaders(request.headers.toSimpleMap) match {
      case Right(extractedHeaders) =>
        val savingNotificationResult = for {
          notificationToSave <- buildNotificationFromResponse(extractedHeaders.conversationId.value, request.body)
          serviceResponse <- notificationService.save(notificationToSave)
        } yield handleServiceResponse(serviceResponse)

        savingNotificationResult.recover {
          case exc: IllegalArgumentException =>
            logger.error(s"There is a problem during parsing notification with exception: ${exc.getMessage}")
            Accepted
        }

      case Left(_) => Future.successful(Accepted)
    }

  private def buildNotificationFromResponse(
    conversationId: String,
    responseXml: NodeSeq
  ): Future[MovementNotification] =
    Future(notificationFactory.buildMovementNotification(conversationId, responseXml))

  private def handleServiceResponse(serviceResponse: Either[String, Unit]): Status =
    serviceResponse match {
      case Right(_) =>
        metrics.incrementCounter(movementMetric)
        Accepted
      case Left(_) =>
        InternalServerError
    }

}
