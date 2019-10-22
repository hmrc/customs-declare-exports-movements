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
import uk.gov.hmrc.exports.movements.repositories.{NotificationRepository, QueryParameters, SubmissionRepository}
import uk.gov.hmrc.http.BadRequestException

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationService @Inject()(notificationRepository: NotificationRepository, submissionRepository: SubmissionRepository)(
  implicit executionContext: ExecutionContext
) {

  val logger = Logger(this.getClass)

  def save(notification: Notification): Future[Unit] =
    notificationRepository
      .insert(notification)
      .map(_ => (): Unit)

  def getAllNotifications(queryParameters: QueryParameters): Future[Seq[NotificationFrontendModel]] =
    submissionRepository.findBy(queryParameters).flatMap {
      case Nil => returnFailed()
      case _   => returnSuccess(queryParameters)
    }

  private def returnFailed(): Future[Nothing] = {
    logger.info("There is no Submission with given Conversation ID for given EORI or PID.")
    Future.failed(new BadRequestException("There is no Submission for this set of parameters"))
  }

  private def returnSuccess(queryParameters: QueryParameters): Future[Seq[NotificationFrontendModel]] =
    for {
      notifications <- notificationRepository.findByConversationId(queryParameters.conversationId.getOrElse(""))
      notificationFrontendModels = notifications.map(NotificationFrontendModel(_))
    } yield notificationFrontendModels

}
