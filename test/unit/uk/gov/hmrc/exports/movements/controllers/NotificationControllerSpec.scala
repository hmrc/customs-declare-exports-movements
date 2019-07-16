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

package unit.uk.gov.hmrc.exports.movements.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, InOrder, Mockito}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.movements.controllers.util.{CustomsHeaderNames, HeaderValidator}
import uk.gov.hmrc.exports.movements.metrics.ExportsMetrics
import uk.gov.hmrc.exports.movements.models.notifications.{MovementNotification, MovementNotificationFactory}
import uk.gov.hmrc.exports.movements.services.NotificationService
import unit.uk.gov.hmrc.exports.movements.base.AuthTestSupport
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import utils.MovementsTestData
import utils.NotificationTestData._

import scala.concurrent.Future
import scala.xml.Elem

class NotificationControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with AuthTestSupport with MovementsTestData with BeforeAndAfterEach
    with ScalaFutures with MustMatchers {

  val saveMovementNotificationUri = "/customs-declare-exports/notifyMovement"

  private val notificationServiceMock: NotificationService = buildNotificationServiceMock
  private val movementNotificationFactoryMock: MovementNotificationFactory = buildMovementNotificationFactoryMock
  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(
      bind[AuthConnector].to(mockAuthConnector),
      bind[NotificationService].to(notificationServiceMock),
      bind[MovementNotificationFactory].to(movementNotificationFactoryMock)
    )
    .build()

  private val metrics: ExportsMetrics = app.injector.instanceOf[ExportsMetrics]
  private val headerValidator: HeaderValidator = app.injector.instanceOf[HeaderValidator]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, notificationServiceMock, movementNotificationFactoryMock)
  }

  "Notification Controller on saveNotification" when {

    "received inventoryLinkingControlResponse and everything works correctly" should {

      "return Accepted status" in new HappyPathSaveControlResponseTest {

        val result = routePostSaveNotification()

        status(result) must be(ACCEPTED)
      }

      "call MovementNotificationFactory and NotificationService afterwards" in new HappyPathSaveControlResponseTest {

        routePostSaveNotification().futureValue

        val inOrder: InOrder = Mockito.inOrder(movementNotificationFactoryMock, notificationServiceMock)
        inOrder.verify(movementNotificationFactoryMock, times(1)).buildMovementNotification(any(), any())
        inOrder.verify(notificationServiceMock, times(1)).save(any())
      }

      "call MovementNotificationFactory once, " +
        "passing conversationId from headers and request body" in new HappyPathSaveControlResponseTest {

        routePostSaveNotification().futureValue

        val conversationIdCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val requestBodyCaptor: ArgumentCaptor[Elem] = ArgumentCaptor.forClass(classOf[Elem])
        verify(movementNotificationFactoryMock, times(1))
          .buildMovementNotification(conversationIdCaptor.capture(), requestBodyCaptor.capture())
        conversationIdCaptor.getValue must equal(validHeaders(CustomsHeaderNames.XConversationIdName))
        requestBodyCaptor.getValue must equal(exampleRejectInventoryLinkingControlResponseXML)
      }

      "call NotificationService once, passing parsed MovementNotification" in new HappyPathSaveControlResponseTest {

        routePostSaveNotification().futureValue

        val notificationCaptor: ArgumentCaptor[MovementNotification] =
          ArgumentCaptor.forClass(classOf[MovementNotification])
        verify(notificationServiceMock, times(1)).save(notificationCaptor.capture())
        notificationCaptor.getValue must equal(exampleRejectInventoryLinkingControlResponseNotification)
      }

      trait HappyPathSaveControlResponseTest {
        withAuthorizedUser()
        when(notificationServiceMock.save(any())).thenReturn(Future.successful(Right((): Unit)))
        when(movementNotificationFactoryMock.buildMovementNotification(any(), any()))
          .thenReturn(exampleRejectInventoryLinkingControlResponseNotification)
      }
    }

    "received inventoryLinkingMovementTotalsResponse and everything works correctly" should {

      "return Accepted status" in new HappyPathSaveTotalsResponseTest {

        val result = routePostSaveNotification(xmlBody = exampleInventoryLinkingMovementTotalsResponseXML)

        status(result) must be(ACCEPTED)
      }

      "call MovementNotificationFactory and NotificationService afterwards" in new HappyPathSaveTotalsResponseTest {

        routePostSaveNotification(xmlBody = exampleInventoryLinkingMovementTotalsResponseXML).futureValue

        val inOrder: InOrder = Mockito.inOrder(movementNotificationFactoryMock, notificationServiceMock)
        inOrder.verify(movementNotificationFactoryMock, times(1)).buildMovementNotification(any(), any())
        inOrder.verify(notificationServiceMock, times(1)).save(any())
      }

      "call MovementNotificationFactory once, " +
        "passing conversationId from headers and request body" in new HappyPathSaveTotalsResponseTest {

        routePostSaveNotification(xmlBody = exampleInventoryLinkingMovementTotalsResponseXML).futureValue

        val conversationIdCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val requestBodyCaptor: ArgumentCaptor[Elem] = ArgumentCaptor.forClass(classOf[Elem])
        verify(movementNotificationFactoryMock, times(1))
          .buildMovementNotification(conversationIdCaptor.capture(), requestBodyCaptor.capture())
        conversationIdCaptor.getValue must equal(validHeaders(CustomsHeaderNames.XConversationIdName))
        requestBodyCaptor.getValue must equal(exampleInventoryLinkingMovementTotalsResponseXML)
      }

      "call NotificationService once, passing parsed MovementNotification" in new HappyPathSaveTotalsResponseTest {

        routePostSaveNotification(xmlBody = exampleInventoryLinkingMovementTotalsResponseXML).futureValue

        val notificationCaptor: ArgumentCaptor[MovementNotification] =
          ArgumentCaptor.forClass(classOf[MovementNotification])
        verify(notificationServiceMock, times(1)).save(notificationCaptor.capture())
        notificationCaptor.getValue must equal(exampleInventoryLinkingMovementTotalsResponseNotification)
      }

      trait HappyPathSaveTotalsResponseTest {
        withAuthorizedUser()
        when(notificationServiceMock.save(any())).thenReturn(Future.successful(Right((): Unit)))
        when(movementNotificationFactoryMock.buildMovementNotification(any(), any()))
          .thenReturn(exampleInventoryLinkingMovementTotalsResponseNotification)
      }
    }

    "NotificationService returns Either.Left" should {

      "return InternalServerError" in {
        withAuthorizedUser()
        when(notificationServiceMock.save(any())).thenReturn(Future.successful(Left("Error message")))

        val result = routePostSaveNotification()

        status(result) must be(INTERNAL_SERVER_ERROR)
      }
    }

    "MovementNotificationFactory throws an Exception" should {

      "return Accepted status" in {
        withAuthorizedUser()
        when(movementNotificationFactoryMock.buildMovementNotification(any(), any()))
          .thenThrow(new IllegalArgumentException("Unknown Inventory Linking Response: UnknownLabel"))

        val result = routePostSaveNotification(xmlBody = unknownFormatResponseXML)

        status(result) must be(ACCEPTED)
      }

      "not call NotificationService" in {
        withAuthorizedUser()
        when(movementNotificationFactoryMock.buildMovementNotification(any(), any()))
          .thenThrow(new IllegalArgumentException("Unknown Inventory Linking Response: UnknownLabel"))

        routePostSaveNotification(xmlBody = unknownFormatResponseXML).futureValue

        verifyZeroInteractions(notificationServiceMock)
      }
    }

    "there is no EORI number in movement notification header" should {

      "return Accepted" in {
        withAuthorizedUser()

        val result = routePostSaveNotification(headers = noEoriHeaders)

        status(result) must be(ACCEPTED)
      }
    }

  }

  def routePostSaveNotification(
    headers: Map[String, String] = validHeaders,
    xmlBody: Elem = exampleRejectInventoryLinkingControlResponseXML
  ): Future[Result] =
    route(
      app,
      FakeRequest(POST, saveMovementNotificationUri)
        .withHeaders(headers.toSeq: _*)
        .withXmlBody(xmlBody)
    ).get
}
