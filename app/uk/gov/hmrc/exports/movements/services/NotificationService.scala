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

package uk.gov.hmrc.exports.movements.services

import play.api.Logger
import uk.gov.hmrc.exports.movements.models.notifications.exchange.NotificationFrontendModel
import uk.gov.hmrc.exports.movements.models.notifications.standard.StandardNotificationData
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationFactory}
import uk.gov.hmrc.exports.movements.repositories.{NotificationRepository, SearchParameters, SubmissionRepository}
import uk.gov.hmrc.exports.movements.services.audit.{AuditNotifications, AuditService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class NotificationService @Inject() (
  notificationFactory: NotificationFactory,
  notificationRepository: NotificationRepository,
  submissionRepository: SubmissionRepository,
  auditService: AuditService
)(implicit executionContext: ExecutionContext) {

  private val logger: Logger = Logger(this.getClass)

  def save(conversationId: String, body: NodeSeq): Future[Unit] = {
    val notification = notificationFactory.buildMovementNotification(conversationId, body)
    logger.info(s"Notification created with conversation-id=[${notification.conversationId}] and payload=[${notification.payload}]")

    notificationRepository
      .insertOne(notification)
      .map(_ => AuditNotifications.audit(notification, notification.conversationId, auditService))
  }

  def getAllNotifications(searchParameters: SearchParameters): Future[Seq[NotificationFrontendModel]] =
    submissionRepository.findAll(searchParameters).flatMap { submissions =>
      getNotifications(submissions.map(_.conversationId))
    }

  private def getNotifications(conversationIds: Seq[String]): Future[Seq[NotificationFrontendModel]] =
    notificationRepository
      .findByConversationIds(conversationIds)
      .map(
        _.filter(notification => notification.data.isDefined && notification.data.exists(_.isInstanceOf[StandardNotificationData]))
          .map(NotificationFrontendModel(_))
      )

  def parseUnparsedNotifications: Future[Seq[Option[Notification]]] =
    notificationRepository.findUnparsedNotifications().flatMap { unparsedNotifications =>
      logger.info(s"Found ${unparsedNotifications.size} unparsed Notifications. Attempting to parse them.")
      val parsedNotifications = unparsedNotifications.map { notification =>
        notificationFactory.buildMovementNotification(notification.conversationId, notification.payload).copy(_id = notification._id)
      }.filter(_.data.nonEmpty)

      logger.info(s"Updating ${parsedNotifications.size} previously unparsed Notifications.")
      Future.sequence(parsedNotifications.map { parsedNotification =>
        notificationRepository.update(parsedNotification).map { result =>
          AuditNotifications.audit(parsedNotification, parsedNotification.conversationId, auditService)
          result
        }
      })
    }
}
