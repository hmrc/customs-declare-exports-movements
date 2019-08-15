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
import uk.gov.hmrc.exports.movements.models.notifications.NotificationPresentation
import uk.gov.hmrc.exports.movements.repositories.{NotificationRepository, SubmissionRepository}
import uk.gov.hmrc.exports.movements.services.NotificationService
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import utils.testdata.CommonTestData.conversationId
import utils.testdata.NotificationTestData._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

class NotificationServiceSpec extends WordSpec with MockitoSugar with ScalaFutures with MustMatchers {

  trait Test {
    val notificationRepositoryMock: NotificationRepository = buildNotificationRepositoryMock
    val submissionRepositoryMock: SubmissionRepository = buildSubmissionRepositoryMock
    val notificationService =
      new NotificationService(notificationRepositoryMock, submissionRepositoryMock)(ExecutionContext.global)
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
  }

  "NotificationService on getAllNotifications" when {

    "everything works correctly" should {

      "call NotificationRepository, passing ConversationID provided" in new Test {

        notificationService.getAllNotifications(conversationId).futureValue

        verify(notificationRepositoryMock).findNotificationsByConversationId(meq(conversationId))
      }

      "return list of NotificationPresentationData converted from Notifications returned by repository" in new Test {

        val firstNotification = notification_1.copy(conversationId = "convId")
        val secondNotification = notification_2.copy(conversationId = "convId")

        when(notificationRepositoryMock.findNotificationsByConversationId("convId"))
          .thenReturn(Future.successful(Seq(firstNotification, secondNotification)))

        val returnedNotifications = notificationService.getAllNotifications("convId").futureValue

        val expectedFirstNotificationPresentationData = NotificationPresentation(firstNotification)
        val expectedSecondNotificationPresentationData = NotificationPresentation(secondNotification)
        returnedNotifications.length must equal(2)
        returnedNotifications must contain(expectedFirstNotificationPresentationData)
        returnedNotifications must contain(expectedSecondNotificationPresentationData)
      }

      "return empty list, if repository returns empty list" in new Test {

        when(notificationRepositoryMock.findNotificationsByConversationId("convId"))
          .thenReturn(Future.successful(Seq.empty))

        val returnedNotifications = notificationService.getAllNotifications("convId").futureValue

        returnedNotifications must be(empty)
      }
    }

  }
}
