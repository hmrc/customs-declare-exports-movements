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

package unit.uk.gov.hmrc.exports.movements.services

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.exports.movements.models.{Eori, MovementSubmissions}
import uk.gov.hmrc.exports.movements.models.notifications.MovementNotification
import uk.gov.hmrc.exports.movements.repositories.{MovementSubmissionRepository, NotificationRepository}
import uk.gov.hmrc.exports.movements.services.NotificationService
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import utils.NotificationTestData._

import scala.concurrent.Future
import scala.util.control.NoStackTrace

class NotificationServiceSpec extends WordSpec with MockitoSugar with ScalaFutures with MustMatchers {

  trait Test {
    val notificationRepositoryMock: NotificationRepository = buildNotificationRepositoryMock
    val submissionRepositoryMock: MovementSubmissionRepository = mock[MovementSubmissionRepository]
    val notificationService = new NotificationService(notificationRepositoryMock, submissionRepositoryMock)
  }

  "NotificationService on save" when {

    "everything works correctly" should {

      "return Either.Right" in new Test {
        when(notificationRepositoryMock.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))

        notificationService.save(notification_1).futureValue must equal(Right((): Unit))
      }

      "call NotificationRepository, passing Notification provided" in new Test {
        when(notificationRepositoryMock.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))

        notificationService.save(notification_1).futureValue

        verify(notificationRepositoryMock, times(1)).insert(meq(notification_1))(any())
      }
    }

    "NotificationRepository on insert returns WriteResult with Error" should {

      "return Either.Left with error message" in new Test {
        val exceptionMsg = "Test Exception message"
        when(notificationRepositoryMock.insert(any())(any()))
          .thenReturn(Future.failed[WriteResult](new Exception(exceptionMsg) with NoStackTrace))

        val saveResult = notificationService.save(notification_1).futureValue

        saveResult must equal(Left(exceptionMsg))
      }
    }

    "Notification Service" should {

      "return list of notifications" in new Test {

        val firstSubmission = MovementSubmissions("eori1", "convId1", "ucr1", "arrival")
        val firstNotification =
          MovementNotification(conversationId = "convId1", errors = Seq.empty, payload = "payload")
        val secondSubmission = MovementSubmissions("eori1", "convId2", "ucr2", "arrival")
        val secondNotification =
          MovementNotification(conversationId = "convId2", errors = Seq.empty, payload = "payload")

        when(submissionRepositoryMock.findByEori(any()))
          .thenReturn(Future.successful(Seq(firstSubmission, secondSubmission)))
        when(notificationRepositoryMock.findNotificationsByConversationId("convId1"))
          .thenReturn(Future.successful(Seq(firstNotification)))
        when(notificationRepositoryMock.findNotificationsByConversationId("convId2"))
          .thenReturn(Future.successful(Seq(secondNotification)))

        val notifications = notificationService.getAllNotifications(Eori("eori1")).futureValue

        notifications must be(Seq(firstNotification, secondNotification))
      }
    }
  }
}
