/*
 * Copyright 2024 HM Revenue & Customs
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

import org.bson.types.ObjectId
import play.api.Logging
import uk.gov.hmrc.exports.movements.models.notifications.exchange.NotificationFrontendModel
import uk.gov.hmrc.exports.movements.models.notifications.queries.IleQueryResponseData
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationFactory}
import uk.gov.hmrc.exports.movements.repositories._
import uk.gov.hmrc.exports.movements.services.audit.{AuditNotifications, AuditService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class NotificationService @Inject() (
  notificationFactory: NotificationFactory,
  notificationRepository: NotificationRepository,
  ileQueryResponseRepository: IleQueryResponseRepository,
  unparsedNotificationRepository: UnparsedNotificationRepository,
  submissionRepository: SubmissionRepository,
  auditService: AuditService
)(implicit executionContext: ExecutionContext)
    extends Logging {

  def save(conversationId: String, body: NodeSeq): Future[Unit] = {
    val notification = notificationFactory.buildMovementNotification(conversationId, body)
    logger.info(s"Notification created with conversation-id=[${notification.conversationId}] and payload=[${notification.payload}]")

    insertNotification(notification)
  }

  def getAllStandardNotifications(searchParameters: SearchParameters): Future[Seq[NotificationFrontendModel]] =
    submissionRepository.findAll(searchParameters).flatMap { submissions =>
      notificationRepository
        .findByConversationIds(submissions.map(_.conversationId))
        .map(_.map(NotificationFrontendModel(_)))
    }

  def handleUnparsedNotifications: Future[Unit] =
    unparsedNotificationRepository.findAll.flatMap { unparsedNotifications =>
      logger.info(s"Found ${unparsedNotifications.size} unparsed Notifications. Attempting to parse them.")
      val parsedNotifications = unparsedNotifications.map { notification =>
        val result = notificationFactory.buildMovementNotification(notification.conversationId, notification.payload)
        result.copy(_id = notification._id)
      }.filter(_.data.nonEmpty)

      logger.info(s"Inserting ${parsedNotifications.size} previously unparsed Notifications.")
      Future.sequence(parsedNotifications.map(insertNotification(_, wasUnparsed = true)))
    }.map(_ => ())

  private def insertNotification(notification: Notification, wasUnparsed: Boolean = false): Future[Unit] = {
    val result = notification.data match {
      case Some(_: IleQueryResponseData) => ileQueryResponseRepository.insertOne(notification)
      case Some(_)                       => notificationRepository.insertOne(notification)
      case _                             => unparsedNotificationRepository.insertOne(notification)
    }

    result.map {
      case Right(notification) =>
        AuditNotifications.audit(notification, notification.conversationId, auditService)
        if (wasUnparsed) unparsedNotificationRepository.removeOne[ObjectId]("_id", notification._id)

      case Left(writeError) =>
        val parsingType = notification.data.fold("unparsed")(_ => "parsed")
        val message = s"Failed to save ($parsingType) Notification with conversation-id=[${notification.conversationId}]"
        logger.error(s"$message: ${writeError.message}.\nPayload was [${notification.payload}]")
    }
  }
}
