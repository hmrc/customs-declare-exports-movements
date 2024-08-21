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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.Mockito.never
import org.mockito.MockitoSugar.{mock, reset, verify, verifyZeroInteractions, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.testdata.CommonTestData._
import utils.testdata.MovementsTestData.exampleSubmission
import utils.testdata.notifications.ExampleInventoryLinkingQueryResponse.Correct.ParentMucr
import utils.testdata.notifications.NotificationTestData._
import utils.testdata.notifications.{ExampleInventoryLinkingControlResponse, NotificationTestData}
import uk.gov.hmrc.exports.movements.models.notifications.exchange.NotificationFrontendModel
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationFactory}
import uk.gov.hmrc.exports.movements.repositories._
import uk.gov.hmrc.exports.movements.services.audit.AuditService

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq, Utility}

class NotificationServiceSpec extends AnyWordSpec with ScalaFutures with Matchers with BeforeAndAfterEach {

  private val notificationFactory = mock[NotificationFactory]
  private val ileQueryResponseRepository = mock[IleQueryResponseRepository]
  private val notificationRepository = mock[NotificationRepository]
  private val unparsedNotificationRepository = mock[UnparsedNotificationRepository]
  private val submissionRepository = mock[SubmissionRepository]
  private val auditService = mock[AuditService]

  private val notificationService = new NotificationService(
    notificationFactory,
    notificationRepository,
    ileQueryResponseRepository,
    unparsedNotificationRepository,
    submissionRepository,
    auditService
  )(ExecutionContext.global)

  def conversationIdsPassed: Seq[String] = {
    val captor: ArgumentCaptor[Seq[String]] = ArgumentCaptor.forClass(classOf[Seq[String]])
    verify(notificationRepository).findByConversationIds(captor.capture())
    captor.getValue
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(notificationFactory, notificationRepository, ileQueryResponseRepository, unparsedNotificationRepository, submissionRepository, auditService)
  }

  "NotificationService on save" when {
    val requestBody = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml

    "everything works correctly" should {

      "call NotificationFactory, passing conversationId from headers and request body and" should {
        "call NotificationRepository afterwards and" should {
          "call AuditService to audit processing" in {
            when(notificationFactory.buildMovementNotification(anyString, any[NodeSeq])).thenReturn(notification_1)
            when(notificationRepository.insertOne(any())).thenReturn(Future.successful(Right(notification_1)))

            notificationService.save(conversationId, requestBody).futureValue must equal(())

            val conversationIdCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
            val requestBodyCaptor: ArgumentCaptor[Elem] = ArgumentCaptor.forClass(classOf[Elem])
            verify(notificationFactory).buildMovementNotification(conversationIdCaptor.capture(), requestBodyCaptor.capture())

            conversationIdCaptor.getValue must equal(conversationId)
            Utility.trim(clearNamespaces(requestBodyCaptor.getValue)).toString must equal(
              Utility
                .trim(clearNamespaces(requestBody))
                .toString
            )

            verify(notificationRepository).insertOne(meq(notification_1))
            verify(auditService).auditNotificationProcessed(any(), any())

            verifyZeroInteractions(ileQueryResponseRepository)
            verifyZeroInteractions(unparsedNotificationRepository)
          }
        }
      }

      "call NotificationFactory and IleQueryResponseRepository afterwards" when {
        "the notification is a IleQueryResponse" in {
          when(notificationFactory.buildMovementNotification(anyString, any[NodeSeq])).thenReturn(ileQueryResponse_1)
          when(ileQueryResponseRepository.insertOne(any())).thenReturn(Future.successful(Right(ileQueryResponse_1)))

          notificationService.save(conversationId, ParentMucr.asXml).futureValue

          verify(ileQueryResponseRepository).insertOne(meq(ileQueryResponse_1))

          verifyZeroInteractions(auditService)
          verifyZeroInteractions(notificationRepository)
          verifyZeroInteractions(unparsedNotificationRepository)
        }
      }
    }

    "NotificationFactory throws an Exception" should {
      "return failed Future with the same exception and" should {
        "not call NotificationRepository and" should {
          "not audit notification processed" in {
            val message = "Unknown Inventory Linking Response: UnknownLabel"
            val exception = new IllegalArgumentException(message)
            when(notificationFactory.buildMovementNotification(any(), any[NodeSeq])).thenThrow(exception)

            val requestBody = NotificationTestData.unknownFormatResponseXML

            the[IllegalArgumentException] thrownBy {
              Await.result(notificationService.save(conversationId, requestBody), patienceConfig.timeout)
            } must have message message

            verifyZeroInteractions(auditService)
            verifyZeroInteractions(notificationRepository)
            verifyZeroInteractions(ileQueryResponseRepository)
            verifyZeroInteractions(unparsedNotificationRepository)
          }
        }
      }
    }
  }

  "NotificationService on getAllStandardNotifications" when {

    "provided with conversationId" should {

      "call SubmissionRepository, passing SearchParameters provided and" should {
        "call NotificationRepository afterwards, passing ConversationID provided" in {
          val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

          val submission = exampleSubmission()
          when(submissionRepository.findAll(meq(searchParameters))).thenReturn(Future.successful(List(submission)))
          when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(Future.successful(List.empty))

          notificationService.getAllStandardNotifications(searchParameters).futureValue

          verify(submissionRepository).findAll(meq(searchParameters))
          verify(notificationRepository).findByConversationIds(meq(List(conversationId)))
        }
      }

      "return list of NotificationPresentationData converted from Notifications returned by repository" in {
        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        val submission = exampleSubmission()
        val firstNotification = notification_1.copy(conversationId = conversationId)
        val secondNotification = notification_2.copy(conversationId = conversationId)
        when(submissionRepository.findAll(meq(searchParameters))).thenReturn(Future.successful(List(submission)))
        when(notificationRepository.findByConversationIds(meq(List(conversationId))))
          .thenReturn(Future.successful(List(firstNotification, secondNotification)))

        val returnedNotifications = notificationService.getAllStandardNotifications(searchParameters).futureValue

        val expectedFirstNotificationPresentationData = NotificationFrontendModel(firstNotification)
        val expectedSecondNotificationPresentationData = NotificationFrontendModel(secondNotification)
        returnedNotifications.length must equal(2)
        returnedNotifications must contain(expectedFirstNotificationPresentationData)
        returnedNotifications must contain(expectedSecondNotificationPresentationData)
      }

      "return empty list, if NotificationRepository returns empty list" in {
        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        val submission = exampleSubmission()
        when(submissionRepository.findAll(meq(searchParameters))).thenReturn(Future.successful(List(submission)))
        when(notificationRepository.findByConversationIds(meq(List(conversationId)))).thenReturn(Future.successful(List.empty))

        val returnedNotifications = notificationService.getAllStandardNotifications(searchParameters).futureValue

        returnedNotifications must be(empty)
      }
    }

    "provided with no conversationId" should {

      "call SubmissionRepository, passing SearchParameters provided and" should {
        "call NotificationRepository afterwards, passing ConversationIDs from Submissions" in {
          val searchParameters = SearchParameters(eori = Some(validEori))

          val submissions = List(exampleSubmission(), exampleSubmission(conversationId = conversationId_2))
          when(submissionRepository.findAll(meq(searchParameters))).thenReturn(Future.successful(submissions))
          when(notificationRepository.findByConversationIds(any())).thenReturn(Future.successful(List.empty))

          notificationService.getAllStandardNotifications(searchParameters).futureValue

          verify(submissionRepository).findAll(meq(searchParameters))
          conversationIdsPassed mustBe List(conversationId, conversationId_2)
        }
      }

      "return list of NotificationPresentationData converted from Notifications returned by repository" in {
        val searchParameters = SearchParameters(eori = Some(validEori))

        val submissions = List(exampleSubmission(), exampleSubmission(conversationId = conversationId_2))
        val notifications = List(
          notification_1.copy(conversationId = conversationId),
          notification_2.copy(conversationId = conversationId),
          notification_1.copy(conversationId = conversationId_2)
        )
        when(submissionRepository.findAll(meq(searchParameters))).thenReturn(Future.successful(submissions))
        when(notificationRepository.findByConversationIds(meq(List(conversationId, conversationId_2))))
          .thenReturn(Future.successful(notifications))

        val returnedNotifications = notificationService.getAllStandardNotifications(searchParameters).futureValue

        val expectedNotifications = notifications.map(NotificationFrontendModel(_))
        returnedNotifications.length must equal(expectedNotifications.length)
        expectedNotifications.foreach { notification =>
          returnedNotifications must contain(notification)
        }
      }

      "return empty list, if NotificationRepository returns empty list" in {
        val searchParameters = SearchParameters(eori = Some(validEori))

        val submissions = List(exampleSubmission(), exampleSubmission(conversationId = conversationId_2))
        when(submissionRepository.findAll(meq(searchParameters))).thenReturn(Future.successful(submissions))
        when(notificationRepository.findByConversationIds(any())).thenReturn(Future.successful(List.empty))

        val returnedNotifications = notificationService.getAllStandardNotifications(searchParameters).futureValue
        returnedNotifications mustBe empty
      }
    }

    "there is no Submission for given set of SearchParameters" should {
      "return empty Sequence" in {
        val searchParameters = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

        when(submissionRepository.findAll(meq(searchParameters))).thenReturn(Future.successful(List.empty))
        when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(Future.successful(List.empty))

        val returnedNotifications = notificationService.getAllStandardNotifications(searchParameters).futureValue

        returnedNotifications mustBe empty
      }
    }
  }

  "NotificationService on handleUnparsedNotifications" when {

    "there are no unparsed notifications" should {
      "call UnparsedNotificationRepository.findAll and" should {
        "not call NotificationFactory or the Notification Repositories" in {
          when(unparsedNotificationRepository.findAll).thenReturn(Future.successful(List.empty))

          notificationService.handleUnparsedNotifications.futureValue

          verify(unparsedNotificationRepository).findAll

          verifyZeroInteractions(notificationFactory)
          verifyZeroInteractions(notificationRepository)
          verifyZeroInteractions(ileQueryResponseRepository)
          verifyZeroInteractions(auditService)
        }
      }
    }

    "there are unparsed notifications" which {
      val unparsedNotification = notification_1.copy(data = None)
      val parsedNotification = notification_1

      "cannot be parsed" should {
        "call UnparsedNotificationRepository.findAll and" should {
          "only call NotificationFactory afterwards" in {
            when(unparsedNotificationRepository.findAll).thenReturn(Future.successful(List(unparsedNotification)))
            when(notificationFactory.buildMovementNotification(anyString(), anyString())).thenReturn(unparsedNotification)

            notificationService.handleUnparsedNotifications.futureValue

            val expectedConversationId = unparsedNotification.conversationId
            val xmlCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
            verify(notificationFactory).buildMovementNotification(meq(expectedConversationId), xmlCaptor.capture())
            xmlCaptor.getValue mustBe unparsedNotification.payload

            verify(unparsedNotificationRepository).findAll
            verify(unparsedNotificationRepository, never()).removeOne(any(), any())

            verifyZeroInteractions(notificationRepository)
            verifyZeroInteractions(ileQueryResponseRepository)
            verifyZeroInteractions(auditService)
          }
        }
      }

      "can be parsed" should {

        "call UnparsedNotificationRepository.findAll and NotificationFactory and" should {
          "insert the Notification afterwards" in {
            when(unparsedNotificationRepository.findAll).thenReturn(Future.successful(List(unparsedNotification)))
            when(unparsedNotificationRepository.removeOne(any(), any())).thenReturn(Future.successful(()))
            when(notificationFactory.buildMovementNotification(anyString(), anyString())).thenReturn(parsedNotification)
            when(notificationRepository.insertOne(any[Notification])).thenReturn(Future.successful(Right(parsedNotification)))

            notificationService.handleUnparsedNotifications.futureValue

            verify(unparsedNotificationRepository).findAll

            val expectedConversationId = unparsedNotification.conversationId
            val xmlCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
            verify(notificationFactory).buildMovementNotification(meq(expectedConversationId), xmlCaptor.capture())
            xmlCaptor.getValue mustBe unparsedNotification.payload

            verify(notificationRepository).insertOne(meq(parsedNotification))
            verify(unparsedNotificationRepository).removeOne(any(), any())
            verify(auditService).auditNotificationProcessed(any(), any())

            verifyZeroInteractions(ileQueryResponseRepository)
          }
        }

        "call UnparsedNotificationRepository.findAll and NotificationFactory and" should {
          "call IleQueryResponseRepository" when {
            "the Notification is a IleQueryResponse" in {
              val unparsedNotification = ileQueryResponse_1.copy(data = None)
              when(unparsedNotificationRepository.findAll).thenReturn(Future.successful(List(unparsedNotification)))
              when(unparsedNotificationRepository.removeOne(any(), any())).thenReturn(Future.successful(()))
              when(notificationFactory.buildMovementNotification(anyString(), anyString())).thenReturn(ileQueryResponse_1)
              when(ileQueryResponseRepository.insertOne(any[Notification])).thenReturn(Future.successful(Right(ileQueryResponse_1)))

              notificationService.handleUnparsedNotifications.futureValue

              verify(unparsedNotificationRepository).findAll

              val expectedConversationId = unparsedNotification.conversationId
              val xmlCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
              verify(notificationFactory).buildMovementNotification(meq(expectedConversationId), xmlCaptor.capture())
              xmlCaptor.getValue mustBe unparsedNotification.payload

              verify(ileQueryResponseRepository).insertOne(meq(ileQueryResponse_1))
              verify(unparsedNotificationRepository).removeOne(any(), any())

              verifyZeroInteractions(auditService)
              verifyZeroInteractions(notificationRepository)
            }
          }
        }
      }
    }
  }
}
