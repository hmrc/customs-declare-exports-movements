/*
 * Copyright 2020 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, InOrder, Mockito}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.exports.movements.models.notifications.exchange.NotificationFrontendModel
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationFactory}
import uk.gov.hmrc.exports.movements.repositories.{NotificationRepository, SearchParameters, SubmissionRepository}
import uk.gov.hmrc.exports.movements.services.NotificationService
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmission
import testdata.notifications.NotificationTestData._
import testdata.notifications.{ExampleInventoryLinkingControlResponse, NotificationTestData}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.NoStackTrace
import scala.xml.{Elem, NodeSeq, Utility}

class NotificationServiceSpec extends WordSpec with MockitoSugar with ScalaFutures with MustMatchers with BeforeAndAfterEach {

  val notificationFactory: NotificationFactory = mock[NotificationFactory]
  val notificationRepository: NotificationRepository = mock[NotificationRepository]
  val submissionRepository: SubmissionRepository = mock[SubmissionRepository]
  val notificationService =
    new NotificationService(notificationFactory, notificationRepository, submissionRepository)(ExecutionContext.global)

  def conversationIdsPassed: Seq[String] = {
    val captor: ArgumentCaptor[Seq[String]] = ArgumentCaptor.forClass(classOf[Seq[String]])
    verify(notificationRepository).findByConversationIds(captor.capture())
    captor.getValue
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(notificationFactory, notificationRepository, submissionRepository)

    when(notificationFactory.buildMovementNotification(anyString, any[NodeSeq])).thenReturn(Notification.empty)
    when(notificationRepository.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
    when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(Future.successful(Seq.empty))
    when(submissionRepository.findBy(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))
  }

  override def afterEach(): Unit = {
    reset(notificationFactory, notificationRepository, submissionRepository)
    super.afterEach()
  }

  "NotificationService on save" when {

    val requestBody = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml

    "everything works correctly" should {

      "return successful Future" in {

        notificationService.save(conversationId, requestBody).futureValue must equal((): Unit)
      }

      "call MovementNotificationFactory and NotificationRepository afterwards" in {

        notificationService.save(conversationId, requestBody).futureValue

        val inOrder: InOrder = Mockito.inOrder(notificationFactory, notificationRepository)
        inOrder.verify(notificationFactory, times(1)).buildMovementNotification(any(), any())
        inOrder.verify(notificationRepository, times(1)).insert(any())(any())
      }

      "call MovementNotificationFactory once, passing conversationId from headers and request body" in {

        notificationService.save(conversationId, requestBody).futureValue

        val conversationIdCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val requestBodyCaptor: ArgumentCaptor[Elem] = ArgumentCaptor.forClass(classOf[Elem])
        verify(notificationFactory, times(1)).buildMovementNotification(conversationIdCaptor.capture(), requestBodyCaptor.capture())

        conversationIdCaptor.getValue must equal(conversationId)
        Utility.trim(clearNamespaces(requestBodyCaptor.getValue)).toString must equal(
          Utility
            .trim(clearNamespaces(requestBody))
            .toString
        )
      }

      "call NotificationRepository, passing Notification returned by NotificationFactory" in {
        when(notificationFactory.buildMovementNotification(anyString, any[NodeSeq])).thenReturn(notification_1)

        notificationService.save(conversationId, requestBody).futureValue

        verify(notificationRepository, times(1)).insert(meq(notification_1))(any())
      }
    }

    "MovementNotificationFactory throws an Exception" should {

      "return successful Future" in {
        when(notificationFactory.buildMovementNotification(any(), any()))
          .thenThrow(new IllegalArgumentException("Unknown Inventory Linking Response: UnknownLabel"))

        val requestBody = NotificationTestData.unknownFormatResponseXML

        val result = notificationService.save(conversationId, requestBody).futureValue

        result must equal((): Unit)
      }

      "not call NotificationRepository" in {
        when(notificationFactory.buildMovementNotification(any(), any()))
          .thenThrow(new IllegalArgumentException("Unknown Inventory Linking Response: UnknownLabel"))

        val requestBody = NotificationTestData.unknownFormatResponseXML

        notificationService.save(conversationId, requestBody).futureValue

        verifyNoMoreInteractions(notificationRepository)
      }
    }

    "NotificationRepository on insert returns WriteResult with Error" should {

      "return failed Future" in {
        val exceptionMsg = "Test Exception message"
        when(notificationRepository.insert(any())(any()))
          .thenReturn(Future.failed[WriteResult](new Exception(exceptionMsg) with NoStackTrace))

        the[Exception] thrownBy {
          Await.result(notificationService.save(conversationId, requestBody), patienceConfig.timeout)
        } must have message exceptionMsg
      }
    }
  }

  "NotificationService on getAllNotifications" when {

    "provided with conversationId" should {

      "call SubmissionRepository, passing SearchParameters provided" in {

        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        val submission = exampleSubmission()
        when(submissionRepository.findBy(meq(searchParameters))).thenReturn(Future.successful(Seq(submission)))

        notificationService.getAllNotifications(searchParameters).futureValue

        verify(submissionRepository).findBy(meq(searchParameters))
      }

      "call NotificationRepository, passing ConversationID provided" in {

        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        val submission = exampleSubmission()
        when(submissionRepository.findBy(meq(searchParameters))).thenReturn(Future.successful(Seq(submission)))

        notificationService.getAllNotifications(searchParameters).futureValue

        verify(notificationRepository).findByConversationIds(meq(Seq(conversationId)))
      }

      "return list of NotificationPresentationData converted from Notifications returned by repository" in {

        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        val submission = exampleSubmission()
        val firstNotification = notification_1.copy(conversationId = conversationId)
        val secondNotification = notification_2.copy(conversationId = conversationId)
        when(submissionRepository.findBy(meq(searchParameters))).thenReturn(Future.successful(Seq(submission)))
        when(notificationRepository.findByConversationIds(meq(Seq(conversationId))))
          .thenReturn(Future.successful(Seq(firstNotification, secondNotification)))

        val returnedNotifications = notificationService.getAllNotifications(searchParameters).futureValue

        val expectedFirstNotificationPresentationData = NotificationFrontendModel(firstNotification)
        val expectedSecondNotificationPresentationData = NotificationFrontendModel(secondNotification)
        returnedNotifications.length must equal(2)
        returnedNotifications must contain(expectedFirstNotificationPresentationData)
        returnedNotifications must contain(expectedSecondNotificationPresentationData)
      }

      "return empty list, if NotificationRepository returns empty list" in {

        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        val submission = exampleSubmission()
        when(submissionRepository.findBy(meq(searchParameters))).thenReturn(Future.successful(Seq(submission)))
        when(notificationRepository.findByConversationIds(meq(Seq(conversationId)))).thenReturn(Future.successful(Seq.empty))

        val returnedNotifications = notificationService.getAllNotifications(searchParameters).futureValue

        returnedNotifications must be(empty)
      }
    }

    "provided with no conversationId" should {

      "call SubmissionRepository, passing SearchParameters provided" in {

        val searchParameters = SearchParameters(eori = Some(validEori))

        val submissions = Seq(exampleSubmission(), exampleSubmission(conversationId = conversationId_2))
        when(submissionRepository.findBy(meq(searchParameters))).thenReturn(Future.successful(submissions))

        notificationService.getAllNotifications(searchParameters).futureValue

        verify(submissionRepository).findBy(meq(searchParameters))
      }

      "call NotificationRepository, passing ConversationIDs from Submissions" in {

        val searchParameters = SearchParameters(eori = Some(validEori))

        val submissions = Seq(exampleSubmission(), exampleSubmission(conversationId = conversationId_2))
        when(submissionRepository.findBy(meq(searchParameters))).thenReturn(Future.successful(submissions))

        notificationService.getAllNotifications(searchParameters).futureValue

        conversationIdsPassed mustBe Seq(conversationId, conversationId_2)
      }

      "return list of NotificationPresentationData converted from Notifications returned by repository" in {

        val searchParameters = SearchParameters(eori = Some(validEori))

        val submissions = Seq(exampleSubmission(), exampleSubmission(conversationId = conversationId_2))
        val notifications = Seq(
          notification_1.copy(conversationId = conversationId),
          notification_2.copy(conversationId = conversationId),
          notification_1.copy(conversationId = conversationId_2)
        )
        when(submissionRepository.findBy(meq(searchParameters))).thenReturn(Future.successful(submissions))
        when(notificationRepository.findByConversationIds(meq(Seq(conversationId, conversationId_2))))
          .thenReturn(Future.successful(notifications))

        val returnedNotifications = notificationService.getAllNotifications(searchParameters).futureValue

        val expectedNotifications = notifications.map(NotificationFrontendModel(_))
        returnedNotifications.length must equal(expectedNotifications.length)
        expectedNotifications.foreach { notification =>
          returnedNotifications must contain(notification)
        }
      }

      "return empty list, if NotificationRepository returns empty list" in {

        val searchParameters = SearchParameters(eori = Some(validEori))

        val submissions = Seq(exampleSubmission(), exampleSubmission(conversationId = conversationId_2))
        when(submissionRepository.findBy(meq(searchParameters))).thenReturn(Future.successful(submissions))
        when(notificationRepository.findByConversationIds(meq(Seq(conversationId)))).thenReturn(Future.successful(Seq.empty))

        val returnedNotifications = notificationService.getAllNotifications(searchParameters).futureValue

        returnedNotifications must be(empty)
      }
    }

    "there is no Submission for given set of SearchParameters" should {

      "return empty Sequence" in {

        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        when(submissionRepository.findBy(meq(searchParameters))).thenReturn(Future.successful(Seq.empty))

        val returnedNotifications = notificationService.getAllNotifications(searchParameters).futureValue

        returnedNotifications mustBe empty
      }
    }

  }
}
