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
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.exports.movements.models.notifications.NotificationFrontendModel
import uk.gov.hmrc.exports.movements.repositories.{NotificationRepository, SearchParameters, SubmissionRepository}
import uk.gov.hmrc.exports.movements.services.NotificationService
import uk.gov.hmrc.http.BadRequestException
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import utils.testdata.CommonTestData.{conversationId, validEori}
import utils.testdata.MovementsTestData.exampleSubmission
import utils.testdata.notifications.NotificationTestData._

import scala.concurrent.{Await, ExecutionContext, Future}
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

        notificationService.save(notification_1).futureValue must equal((): Unit)
      }

      "call NotificationRepository, passing Notification provided" in new Test {
        when(notificationRepositoryMock.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))

        notificationService.save(notification_1).futureValue

        verify(notificationRepositoryMock, times(1)).insert(meq(notification_1))(any())
      }
    }

    "NotificationRepository on insert returns WriteResult with Error" should {

      "return failed future" in new Test {
        val exceptionMsg = "Test Exception message"
        when(notificationRepositoryMock.insert(any())(any()))
          .thenReturn(Future.failed[WriteResult](new Exception(exceptionMsg) with NoStackTrace))

        the[Exception] thrownBy {
          Await.result(notificationService.save(notification_1), patienceConfig.timeout)
        } must have message exceptionMsg
      }
    }
  }

  "NotificationService on getAllNotifications" when {

    "everything works correctly" should {

      "call SubmissionRepository, passing SearchParameters provided" in new Test {

        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        val submission = exampleSubmission()
        when(submissionRepositoryMock.findBy(meq(searchParameters))).thenReturn(Future.successful(Seq(submission)))

        notificationService.getAllNotifications(searchParameters).futureValue

        verify(submissionRepositoryMock).findBy(meq(searchParameters))
      }

      "call NotificationRepository, passing ConversationID provided" in new Test {

        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        val submission = exampleSubmission()
        when(submissionRepositoryMock.findBy(meq(searchParameters))).thenReturn(Future.successful(Seq(submission)))

        notificationService.getAllNotifications(searchParameters).futureValue

        verify(notificationRepositoryMock).findByConversationId(meq(conversationId))
      }

      "return list of NotificationPresentationData converted from Notifications returned by repository" in new Test {

        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        val submission = exampleSubmission()
        val firstNotification = notification_1.copy(conversationId = conversationId)
        val secondNotification = notification_2.copy(conversationId = conversationId)
        when(submissionRepositoryMock.findBy(meq(searchParameters))).thenReturn(Future.successful(Seq(submission)))
        when(notificationRepositoryMock.findByConversationId(conversationId))
          .thenReturn(Future.successful(Seq(firstNotification, secondNotification)))

        val returnedNotifications = notificationService.getAllNotifications(searchParameters).futureValue

        val expectedFirstNotificationPresentationData = NotificationFrontendModel(firstNotification)
        val expectedSecondNotificationPresentationData = NotificationFrontendModel(secondNotification)
        returnedNotifications.length must equal(2)
        returnedNotifications must contain(expectedFirstNotificationPresentationData)
        returnedNotifications must contain(expectedSecondNotificationPresentationData)
      }

      "return empty list, if repository returns empty list" in new Test {

        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        val submission = exampleSubmission()
        when(submissionRepositoryMock.findBy(meq(searchParameters))).thenReturn(Future.successful(Seq(submission)))
        when(notificationRepositoryMock.findByConversationId(conversationId)).thenReturn(Future.successful(Seq.empty))

        val returnedNotifications = notificationService.getAllNotifications(searchParameters).futureValue

        returnedNotifications must be(empty)
      }
    }

    "there is no Submission for given set of SearchParameters" should {

      "return failed Future" in new Test {

        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        when(submissionRepositoryMock.findBy(meq(searchParameters))).thenReturn(Future.successful(Seq.empty))

        val exc = notificationService.getAllNotifications(searchParameters).failed.futureValue

        exc mustBe an[BadRequestException]
        exc.getMessage must include("There is no Submission for this set of parameters")
      }
    }

  }
}
