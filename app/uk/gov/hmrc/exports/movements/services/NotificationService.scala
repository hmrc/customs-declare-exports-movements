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

package uk.gov.hmrc.exports.movements.services

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationFrontendModel}
import uk.gov.hmrc.exports.movements.repositories.{NotificationRepository, SubmissionRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationService @Inject()(
  notificationRepository: NotificationRepository,
  submissionRepository: SubmissionRepository
)(implicit executionContext: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def save(notification: Notification): Future[Either[String, Unit]] =
    notificationRepository
      .insert(notification)
      .map(_ => Right((): Unit))
      .recover {
        case exc: Throwable =>
          logger.error(exc.getMessage)
          Left(exc.getMessage)
      }

  def getAllNotifications(conversationId: String): Future[Seq[NotificationFrontendModel]] =
    for {
      notifications <- notificationRepository.findNotificationsByConversationId(conversationId)
      notificationPresentations <- Future.successful(notifications.map(NotificationFrontendModel(_)))
    } yield notificationPresentations
}
