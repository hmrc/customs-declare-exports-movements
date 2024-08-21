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

package uk.gov.hmrc.exports.movements.controllers

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.testdata.CommonTestData._
import utils.testdata.notifications.NotificationTestData._
import utils.testdata.notifications.{ExampleInventoryLinkingControlResponse, ExampleInventoryLinkingMovementTotalsResponse}
import uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.controllers.FakeRequestFactory._
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.models.notifications.exchange.NotificationFrontendModel
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.exports.movements.services.NotificationService

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.xml.{Elem, NodeSeq, Utility}

class NotificationControllerSpec extends AnyWordSpec with Matchers with ScalaFutures with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]
  private val headerValidator = mock[HeaderValidator]
  private val notificationService = mock[NotificationService]
  private val movementsMetrics = buildMovementsMetricsMock

  when(appConfig.maxNotificationPayloadSize).thenReturn(1024L * 100)

  private val controller = new NotificationController(headerValidator, movementsMetrics, notificationService, stubControllerComponents(), appConfig)(
    global
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(headerValidator.extractConversationIdHeader(any())).thenReturn(Some(conversationId))
    when(notificationService.save(anyString, any[NodeSeq])).thenReturn(Future.successful((): Unit))
  }

  override def afterEach(): Unit = {
    reset(appConfig, headerValidator, notificationService)
    super.afterEach()
  }

  "NotificationController on saveNotification" when {

    "received inventoryLinkingControlResponse and everything works correctly" should {

      "return Accepted (200) status" in {
        val request = postRequestWithBody(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml).withHeaders(XmlContentTypeHeader)
        val result = controller.saveNotification()(request)

        status(result) mustBe ACCEPTED
      }

      "call NotificationService once, passing conversationId from headers and request body" in {
        val request = postRequestWithBody(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml).withHeaders(XmlContentTypeHeader)
        controller.saveNotification()(request).futureValue

        val conversationIdCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val requestBodyCaptor: ArgumentCaptor[NodeSeq] = ArgumentCaptor.forClass(classOf[NodeSeq])
        verify(notificationService).save(conversationIdCaptor.capture(), requestBodyCaptor.capture())

        conversationIdCaptor.getValue must equal(conversationId)
        requestBodyCaptor.getValue must equal(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml)
      }
    }

    "received inventoryLinkingMovementTotalsResponse and everything works correctly" should {

      "return Accepted (200) status" in {
        val request = postRequestWithBody(ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asXml).withHeaders(XmlContentTypeHeader)
        val result = controller.saveNotification()(request)

        status(result) mustBe ACCEPTED
      }

      "call NotificationService once, passing conversationId from headers and request body" in {
        val request = postRequestWithBody(ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asXml).withHeaders(XmlContentTypeHeader)
        controller.saveNotification()(request).futureValue

        val conversationIdCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val requestBodyCaptor: ArgumentCaptor[Elem] = ArgumentCaptor.forClass(classOf[Elem])
        verify(notificationService).save(conversationIdCaptor.capture(), requestBodyCaptor.capture())

        conversationIdCaptor.getValue must equal(conversationId)
        Utility.trim(clearNamespaces(requestBodyCaptor.getValue)).toString must equal(
          Utility
            .trim(clearNamespaces(ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asXml))
            .toString
        )
      }
    }

    "NotificationService returns failure" should {
      "return InternalServerError" in {
        when(notificationService.save(anyString, any[NodeSeq])).thenReturn(Future.failed(new Exception("")))

        an[Exception] mustBe thrownBy {
          val request = postRequestWithBody(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml).withHeaders(XmlContentTypeHeader)
          await(controller.saveNotification()(request))
        }
      }
    }

  }

  "NotificationController on getNotificationsForSubmission" should {

    "return Accepted (200) status" in {
      when(notificationService.getAllStandardNotifications(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))

      val result = controller.getNotificationsForSubmission(Some(validEori), None, conversationId)(getRequest)

      status(result) must be(OK)
    }

    "call NotificationService once, passing SearchParameters" in {
      when(notificationService.getAllStandardNotifications(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))
      val searchParameters =
        SearchParameters(eori = Some(validEori), providerId = Some(validProviderId), conversationId = Some(conversationId))

      controller
        .getNotificationsForSubmission(searchParameters.eori, searchParameters.providerId, searchParameters.conversationId.get)(getRequest)
        .futureValue

      searchParametersPassed mustBe searchParameters
    }

    "return list of notifications returned by NotificationService" in {
      val notificationsToReturn = Seq(notification_1, notification_2).map(NotificationFrontendModel(_))
      when(notificationService.getAllStandardNotifications(any[SearchParameters])).thenReturn(Future.successful(notificationsToReturn))

      val notifications = controller.getNotificationsForSubmission(Some(validEori), None, conversationId)(getRequest)

      contentAsJson(notifications) mustBe Json.toJson(notificationsToReturn)
    }
  }

  "NotificationController on getAllNotificationsForUser" should {

    "return Accepted (200) status" in {
      when(notificationService.getAllStandardNotifications(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))

      val result = controller.getAllNotificationsForUser(Some(validEori), None)(getRequest)

      status(result) must be(OK)
    }

    "call NotificationService once, passing SearchParameters" in {
      when(notificationService.getAllStandardNotifications(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))
      val searchParameters =
        SearchParameters(eori = Some(validEori), providerId = Some(validProviderId), conversationId = None)

      controller
        .getAllNotificationsForUser(searchParameters.eori, searchParameters.providerId)(getRequest)
        .futureValue

      searchParametersPassed mustBe searchParameters
    }

    "return list of notifications returned by NotificationService" in {
      val notificationsToReturn = Seq(notification_1, notification_2).map(NotificationFrontendModel(_))
      when(notificationService.getAllStandardNotifications(any[SearchParameters])).thenReturn(Future.successful(notificationsToReturn))

      val notifications = controller.getAllNotificationsForUser(Some(validEori), None)(getRequest)

      contentAsJson(notifications) mustBe Json.toJson(notificationsToReturn)
    }
  }

  private def searchParametersPassed: SearchParameters = {
    val captor: ArgumentCaptor[SearchParameters] = ArgumentCaptor.forClass(classOf[SearchParameters])
    verify(notificationService).getAllStandardNotifications(captor.capture())
    captor.getValue
  }
}
