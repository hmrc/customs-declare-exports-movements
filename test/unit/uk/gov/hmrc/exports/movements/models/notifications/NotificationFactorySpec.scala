/*
 * Copyright 2021 HM Revenue & Customs
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

package unit.uk.gov.hmrc.exports.movements.models.notifications

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import testdata.CommonTestData.conversationId
import testdata.notifications.NotificationTestData._
import testdata.notifications._
import uk.gov.hmrc.exports.movements.models.notifications.parsers.ResponseParser
import uk.gov.hmrc.exports.movements.models.notifications.standard.StandardNotificationData
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationData, NotificationFactory}
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder

import scala.util.{Failure, Try}
import scala.xml.{Node, NodeSeq, Utility, XML}

class NotificationFactorySpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val responseValidator = UnitTestMockBuilder.buildResponseValidatorMock
  private val responseParserProvider = UnitTestMockBuilder.buildResponseParserProviderMock
  private val responseParser: ResponseParser[NotificationData] = mock[ResponseParser[NotificationData]]

  private val notificationFactory = new NotificationFactory(responseValidator, responseParserProvider)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(responseValidator, responseParserProvider, responseParser)
    when(responseValidator.validate(any[NodeSeq])).thenReturn(Try((): Unit))
    when(responseParserProvider.provideResponseParser(any())).thenReturn(responseParser)
  }

  override protected def afterEach(): Unit = {
    reset(responseValidator, responseParserProvider, responseParser)
    super.afterEach()
  }

  "MovementNotificationFactory on buildMovementNotification" when {

    "everything works correctly" should {

      val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml

      "call ResponseParserFactory, passing response XML provided" in {
        notificationFactory.buildMovementNotification(conversationId, responseXml)

        verify(responseParserProvider).provideResponseParser(meq(responseXml))
      }

      "call ResponseValidator, passing response XML provided" in {
        notificationFactory.buildMovementNotification(conversationId, responseXml)

        verify(responseValidator).validate(meq(responseXml))
      }

      "call ResponseParser returned by the ResponseParserFactory, passing response XML provided" in {
        notificationFactory.buildMovementNotification(conversationId, responseXml)

        verify(responseParser).parse(meq(responseXml))
      }

      "return Notification containing correct conversationId" in {
        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        resultNotification.conversationId must equal(conversationId)
      }

      "return Notification containing notificationData from ResponseParser" in {
        val expectedNotificationData =
          StandardNotificationData(messageCode = Some("TestMessageCode"), responseType = "inventoryLinkingControlResponse")
        when(responseParser.parse(any())).thenReturn(expectedNotificationData)

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        resultNotification.data mustBe defined
        resultNotification.data.get must equal(expectedNotificationData)
      }

      "return Notification containing correct payload" in {
        val expectedPayload =
          clearNamespaces(Utility.trim(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml))

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        clearNamespaces(XML.loadString(resultNotification.payload)) must equal(expectedPayload)
      }
    }

    "provided with empty Conversation ID" should {

      "create Notification with empty conversationId field" in {
        val responseTypeTest = "ResponseType"
        when(responseParser.responseTypeIle).thenReturn(responseTypeTest)
        val testNotificationData = ExampleInventoryLinkingControlResponse.Correct.Rejected
        when(responseParser.parse(any())).thenReturn(testNotificationData.asDomainModel)
        val responseXml = testNotificationData.asXml
        val expectedNotification =
          Notification(
            conversationId = "",
            payload = Utility.trim(testNotificationData.asXml).toString(),
            data = Some(testNotificationData.asDomainModel)
          )

        val resultNotification = notificationFactory.buildMovementNotification("", responseXml)

        resultNotification.conversationId must equal(expectedNotification.conversationId)
        resultNotification.payload must equal(expectedNotification.payload)
        resultNotification.data must equal(expectedNotification.data)
      }
    }

    "provided with response not matching schema (ResponseValidator returns Failure)" should {

      val responseXml = ExampleInventoryLinkingControlResponse.Incorrect.DoubleUcrBlock.asXml

      "not throw an Exception" in {
        when(responseValidator.validate(any[NodeSeq])).thenReturn(Failure(new Exception("")))

        noException should be thrownBy notificationFactory.buildMovementNotification(conversationId, responseXml)
      }

      "call ResponseValidator, passing response XML provided" in {
        when(responseValidator.validate(any[NodeSeq])).thenReturn(Failure(new Exception("")))

        notificationFactory.buildMovementNotification(conversationId, responseXml)

        verify(responseValidator).validate(meq(responseXml))
      }

      "not call ResponseParserProvider" in {
        when(responseValidator.validate(any[NodeSeq])).thenReturn(Failure(new Exception("")))

        notificationFactory.buildMovementNotification(conversationId, responseXml)

        verifyNoInteractions(responseParserProvider)
      }

      "return Notification containing correct conversationId" in {
        when(responseValidator.validate(any[NodeSeq])).thenReturn(Failure(new Exception("")))

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        resultNotification.conversationId must equal(conversationId)
      }

      "return Notification containing correct payload" in {
        when(responseValidator.validate(any[NodeSeq])).thenReturn(Failure(new Exception("")))
        val expectedPayload: Node = clearNamespaces(Utility.trim(responseXml))

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        clearNamespaces(XML.loadString(resultNotification.payload)) must equal(expectedPayload)
      }

      "return Notification containing empty notificationData" in {
        when(responseValidator.validate(any[NodeSeq])).thenReturn(Failure(new Exception("")))

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        resultNotification.data mustBe empty
      }
    }

    "ResponseParserProvider throws Exception" should {

      val responseXml = ExampleInventoryLinkingControlResponse.Incorrect.DoubleUcrBlock.asXml

      "not throw an Exception" in {
        when(responseParserProvider.provideResponseParser(any())).thenThrow(new IllegalArgumentException("Test Exception"))

        noException should be thrownBy notificationFactory.buildMovementNotification(conversationId, responseXml)
      }

      "return Notification containing correct conversationId" in {
        when(responseParserProvider.provideResponseParser(any())).thenThrow(new IllegalArgumentException("Test Exception"))

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        resultNotification.conversationId must equal(conversationId)
      }

      "return Notification containing correct payload" in {
        when(responseParserProvider.provideResponseParser(any())).thenThrow(new IllegalArgumentException("Test Exception"))
        val expectedPayload: Node = clearNamespaces(Utility.trim(responseXml))

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        clearNamespaces(XML.loadString(resultNotification.payload)) must equal(expectedPayload)
      }

      "return Notification containing empty notificationData" in {
        when(responseParserProvider.provideResponseParser(any())).thenThrow(new IllegalArgumentException("Test Exception"))

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        resultNotification.data mustBe empty
      }
    }
  }

}
