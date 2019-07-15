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
import uk.gov.hmrc.exports.movements.controllers.actions.Authenticator
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.metrics.ExportsMetrics
import uk.gov.hmrc.exports.movements.metrics.MetricIdentifiers._
import uk.gov.hmrc.exports.movements.models.notifications.MovementNotificationFactory
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
) extends Authenticator(authConnector, cc) {

  val logger = Logger(this.getClass)

  def saveNotification(): Action[NodeSeq] = Action.async(parse.xml) { implicit request =>
    val timer = metrics.startTimer(movementMetric)
    headerValidator.validateAndExtractMovementNotificationHeaders(request.headers.toSimpleMap) match {

      case Right(extractedHeaders) =>
        Future(
            notificationFactory
              .buildMovementNotification(extractedHeaders.conversationId.value, request.body)
          )
          .flatMap { notificationToSave =>
            notificationService.save(notificationToSave)
          }
          .map {
            case Right(_) =>
              metrics.incrementCounter(movementMetric)
              timer.stop()
              Accepted
            case Left(_) =>
              InternalServerError
          }
          .recover {
            case exc: IllegalArgumentException =>
              logger.error(s"There is a problem during parsing notification with exception: ${exc.getMessage}")
              Accepted
          }

      case Left(errorResponse) => Future.successful(errorResponse.XmlResult)
    }
  }

}
