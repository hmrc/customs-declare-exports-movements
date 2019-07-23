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
import uk.gov.hmrc.exports.movements.models.Eori
import uk.gov.hmrc.exports.movements.models.notifications.MovementNotification
import uk.gov.hmrc.exports.movements.repositories.{MovementSubmissionRepository, NotificationRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class NotificationService @Inject()(
  notificationRepository: NotificationRepository,
  submissionRepository: MovementSubmissionRepository
) {

  private val logger = Logger(this.getClass)

  def save(notification: MovementNotification): Future[Either[String, Unit]] =
    notificationRepository
      .insert(notification)
      .map(_ => Right((): Unit))
      .recover {
        case exc: Throwable =>
          logger.error(exc.getMessage)
          Left(exc.getMessage)
      }

  def getAllNotifications(eori: Eori): Future[Seq[MovementNotification]] =
    for {
      submissions <- submissionRepository.findByEori(eori.value)
      convIds = submissions.map(_.conversationId)
      notifications <- getNotificationByConversationIds(convIds)
    } yield notifications

  def getNotificationByConversationIds(conversationIds: Seq[String]): Future[Seq[MovementNotification]] =
    Future.sequence(conversationIds.map(notificationRepository.findNotificationsByConversationId(_))).map(_.flatten)
}
