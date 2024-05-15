/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.metrics.MetricIdentifiers._
import uk.gov.hmrc.exports.movements.metrics.MovementsMetrics
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.exports.movements.services.NotificationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import scala.xml.NodeSeq

@Singleton
class NotificationController @Inject() (
  headerValidator: HeaderValidator,
  metrics: MovementsMetrics,
  notificationService: NotificationService,
  cc: ControllerComponents,
  appConfig: AppConfig
)(implicit executionContext: ExecutionContext)
    extends BackendController(cc) with Logging {

  val saveNotification: Action[NodeSeq] = Action.async(parse.xml(appConfig.maxNotificationPayloadSize)) { implicit request =>
    val timer = metrics.startTimer(movementMetric)

    val result = headerValidator.extractConversationIdHeader(request.headers.toSimpleMap) match {
      case Some(conversationId) =>
        logger.info(s"Notification received with conversation-id=[$conversationId]")
        notificationService.save(conversationId, request.body).map(_ => Accepted).andThen { case Success(_) =>
          metrics.incrementCounter(movementMetric)
        }
      case None =>
        logger.warn("Notification received without a conversation-id. It will be dropped.")
        Future.successful(Accepted)
    }

    timer.stop()
    result
  }

  def getNotificationsForSubmission(eori: Option[String], providerId: Option[String], conversationId: String): Action[AnyContent] =
    searchNotifications(eori, providerId, Some(conversationId))

  def getAllNotificationsForUser(eori: Option[String], providerId: Option[String]): Action[AnyContent] =
    searchNotifications(eori, providerId, None)

  private def searchNotifications(eori: Option[String], providerId: Option[String], conversationId: Option[String]): Action[AnyContent] =
    Action.async { _ =>
      notificationService
        .getAllNotifications(SearchParameters(eori = eori, providerId = providerId, conversationId = conversationId))
        .map(notifications => Ok(Json.toJson(notifications)))
    }
}
