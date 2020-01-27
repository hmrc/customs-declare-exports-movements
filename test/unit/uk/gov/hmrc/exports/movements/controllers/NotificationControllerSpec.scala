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

package unit.uk.gov.hmrc.exports.movements.controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.controllers.NotificationController
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.models.notifications.exchange.NotificationFrontendModel
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.exports.movements.services.NotificationService
import unit.uk.gov.hmrc.exports.movements.base.Injector
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import unit.uk.gov.hmrc.exports.movements.controllers.FakeRequestFactory._
import utils.testdata.CommonTestData._
import utils.testdata.notifications.NotificationTestData._
import utils.testdata.notifications.{ExampleInventoryLinkingControlResponse, ExampleInventoryLinkingMovementTotalsResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq, Utility}

class NotificationControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with BeforeAndAfterEach with Injector {

  private val headerValidator = mock[HeaderValidator]
  private val notificationService = mock[NotificationService]
  private val movementsMetrics = buildMovementsMetricsMock

  private val controllerComponents: ControllerComponents = instanceOf[ControllerComponents]
  implicit private val actorSystem: ActorSystem = FakeRequestFactory.actorSystem
  implicit private val materializer: ActorMaterializer = FakeRequestFactory.materializer

  private val controller =
    new NotificationController(headerValidator, movementsMetrics, notificationService, controllerComponents)(ExecutionContext.global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(headerValidator, notificationService)

    when(headerValidator.extractConversationIdHeader(any())).thenReturn(Some(conversationId))
  }

  override def afterEach(): Unit = {
    reset(headerValidator, notificationService)
    super.afterEach()
  }

  "NotificationController on saveNotification" when {

    "received inventoryLinkingControlResponse and everything works correctly" should {

      "return Accepted (200) status" in {
        when(notificationService.save(anyString, any[NodeSeq])).thenReturn(Future.successful((): Unit))
        val request = postRequestWithXmlBody(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml)

        val result = call(controller.saveNotification(), request)

        status(result) mustBe ACCEPTED
      }

      "call NotificationService once, passing conversationId from headers and request body" in {
        when(notificationService.save(anyString, any[NodeSeq])).thenReturn(Future.successful((): Unit))
        val request = postRequestWithXmlBody(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml)

        call(controller.saveNotification(), request).futureValue

        val conversationIdCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val requestBodyCaptor: ArgumentCaptor[NodeSeq] = ArgumentCaptor.forClass(classOf[NodeSeq])
        verify(notificationService, times(1)).save(conversationIdCaptor.capture(), requestBodyCaptor.capture())

        conversationIdCaptor.getValue must equal(conversationId)
        requestBodyCaptor.getValue must equal(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml)
      }
    }

    "received inventoryLinkingMovementTotalsResponse and everything works correctly" should {

      "return Accepted (200) status" in {
        when(notificationService.save(anyString, any[NodeSeq])).thenReturn(Future.successful((): Unit))
        val request = postRequestWithXmlBody(ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asXml)

        val result = call(controller.saveNotification(), request)

        status(result) mustBe ACCEPTED
      }

      "call NotificationService once, passing conversationId from headers and request body" in {
        when(notificationService.save(anyString, any[NodeSeq])).thenReturn(Future.successful((): Unit))
        val request = postRequestWithXmlBody(ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asXml)

        call(controller.saveNotification(), request).futureValue

        val conversationIdCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val requestBodyCaptor: ArgumentCaptor[Elem] = ArgumentCaptor.forClass(classOf[Elem])
        verify(notificationService, times(1)).save(conversationIdCaptor.capture(), requestBodyCaptor.capture())

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

        val request = postRequestWithXmlBody(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml)

        an[Exception] mustBe thrownBy {
          await(call(controller.saveNotification(), request))
        }
      }
    }
  }

  "NotificationController on getNotificationsForSubmission" should {

    "return Accepted (200) status" in {
      when(notificationService.getAllNotifications(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))

      val result = controller.getNotificationsForSubmission(Some(validEori), None, conversationId)(getRequest())

      status(result) must be(OK)
    }

    "call NotificationService once, passing SearchParameters" in {
      when(notificationService.getAllNotifications(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))
      val searchParameters =
        SearchParameters(eori = Some(validEori), providerId = Some(validProviderId), conversationId = Some(conversationId))

      controller
        .getNotificationsForSubmission(searchParameters.eori, searchParameters.providerId, searchParameters.conversationId.get)(getRequest())
        .futureValue

      searchParametersPassed mustBe searchParameters
    }

    "return list of notifications returned by NotificationService" in {
      val notificationsToReturn = Seq(notification_1, notification_2).map(NotificationFrontendModel(_))
      when(notificationService.getAllNotifications(any[SearchParameters])).thenReturn(Future.successful(notificationsToReturn))

      val notifications = controller.getNotificationsForSubmission(Some(validEori), None, conversationId)(getRequest())

      contentAsJson(notifications) mustBe Json.toJson(notificationsToReturn)
    }
  }

  "NotificationController on getAllNotificationsForUser" should {

    "return Accepted (200) status" in {
      when(notificationService.getAllNotifications(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))

      val result = controller.getAllNotificationsForUser(Some(validEori), None)(getRequest())

      status(result) must be(OK)
    }

    "call NotificationService once, passing SearchParameters" in {
      when(notificationService.getAllNotifications(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))
      val searchParameters =
        SearchParameters(eori = Some(validEori), providerId = Some(validProviderId), conversationId = None)

      controller
        .getAllNotificationsForUser(searchParameters.eori, searchParameters.providerId)(getRequest())
        .futureValue

      searchParametersPassed mustBe searchParameters
    }

    "return list of notifications returned by NotificationService" in {
      val notificationsToReturn = Seq(notification_1, notification_2).map(NotificationFrontendModel(_))
      when(notificationService.getAllNotifications(any[SearchParameters])).thenReturn(Future.successful(notificationsToReturn))

      val notifications = controller.getAllNotificationsForUser(Some(validEori), None)(getRequest())

      contentAsJson(notifications) mustBe Json.toJson(notificationsToReturn)
    }
  }

  private def searchParametersPassed: SearchParameters = {
    val captor: ArgumentCaptor[SearchParameters] = ArgumentCaptor.forClass(classOf[SearchParameters])
    verify(notificationService).getAllNotifications(captor.capture())
    captor.getValue
  }

}
