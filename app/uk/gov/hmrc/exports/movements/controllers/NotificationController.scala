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
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.metrics.MetricIdentifiers._
import uk.gov.hmrc.exports.movements.metrics.MovementsMetrics
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationFactory}
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.exports.movements.services.NotificationService
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import scala.xml.NodeSeq

@Singleton
class NotificationController @Inject()(
  headerValidator: HeaderValidator,
  metrics: MovementsMetrics,
  notificationService: NotificationService,
  notificationFactory: NotificationFactory,
  cc: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(cc) {

  val logger = Logger(this.getClass)

  def saveNotification(): Action[NodeSeq] = Action.async(parse.xml) { implicit request =>
    val timer = metrics.startTimer(movementMetric)
    saveNotification(request).map { res =>
      timer.stop()
      res
    }
  }

  private def saveNotification(request: Request[NodeSeq]): Future[Status] =
    headerValidator.extractConversationIdHeader(request.headers.toSimpleMap) match {
      case Some(conversationId) =>
        buildNotificationFromResponse(conversationId, request.body) match {
          case Some(notificationToSave) => forwardNotificationToService(notificationToSave)
          case None                     => Future.successful(Accepted)
        }
      case None => Future.successful(Accepted)
    }

  private def buildNotificationFromResponse(conversationId: String, responseXml: NodeSeq): Option[Notification] =
    try {
      Some(notificationFactory.buildMovementNotification(conversationId, responseXml))
    } catch {
      case exc: IllegalArgumentException =>
        logger.warn(s"There is a problem during parsing notification with exception: ${exc.getMessage}")
        None
    }

  private def forwardNotificationToService(notification: Notification): Future[Status] =
    notificationService.save(notification).map(_ => Accepted).andThen {
      case Success(_) =>
        metrics.incrementCounter(movementMetric)
    }

  def listOfNotifications(eori: Option[String], providerId: Option[String], conversationId: String): Action[AnyContent] = Action.async {
    implicit request =>
      notificationService
        .getAllNotifications(SearchParameters(eori = eori, providerId = providerId, conversationId = Some(conversationId)))
        .map(notifications => Ok(Json.toJson(notifications)))
  }
}
