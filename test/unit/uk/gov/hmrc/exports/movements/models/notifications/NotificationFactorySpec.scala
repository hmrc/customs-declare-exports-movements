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

package unit.uk.gov.hmrc.exports.movements.models.notifications

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import testdata.CommonTestData.conversationId
import testdata.notifications.NotificationTestData._
import testdata.notifications._
import uk.gov.hmrc.exports.movements.models.notifications.parsers.ResponseParser
import uk.gov.hmrc.exports.movements.models.notifications.standard.StandardNotificationData
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationData, NotificationFactory}
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder

import scala.util.Try
import scala.xml.{Node, NodeSeq, Utility, XML}

class NotificationFactorySpec extends WordSpec with MustMatchers with MockitoSugar {

  private val responseTypeTest = "TestResponse"

  private trait Test {
    val responseValidatorMock = UnitTestMockBuilder.buildResponseValidatorMock
    val responseParserFactoryMock = UnitTestMockBuilder.buildResponseParserFactoryMock

    when(responseValidatorMock.validate(any[NodeSeq])).thenReturn(Try((): Unit))
    val responseParserMock: ResponseParser[NotificationData] =
      UnitTestMockBuilder.buildResponseParserMock(StandardNotificationData(responseType = responseTypeTest), responseTypeIle = responseTypeTest)
    when(responseParserFactoryMock.provideResponseParser(any())).thenReturn(responseParserMock)

    val notificationFactory = new NotificationFactory(responseValidatorMock, responseParserFactoryMock)
  }

  "MovementNotificationFactory on buildMovementNotification" when {

    "everything works correctly" should {

      "call ResponseParserFactory, passing response XML provided" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        notificationFactory.buildMovementNotification(conversationId, responseXml)

        verify(responseParserFactoryMock).provideResponseParser(meq(responseXml))
      }

      "call ResponseValidator, passing response XML provided" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        notificationFactory.buildMovementNotification(conversationId, responseXml)

        verify(responseValidatorMock).validate(meq(responseXml))
      }

      "call ResponseParser returned by the ResponseParserFactory, passing response XML provided" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        notificationFactory.buildMovementNotification(conversationId, responseXml)

        verify(responseParserMock).parse(meq(responseXml))
      }

      "return Notification containing correct conversationId" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        resultNotification.conversationId must equal(conversationId)
      }

      "return Notification containing notificationData from ResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        val expectedNotificationData =
          StandardNotificationData(messageCode = Some("TestMessageCode"), responseType = "inventoryLinkingControlResponse")
        when(responseParserMock.parse(any())).thenReturn(expectedNotificationData)

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        resultNotification.data mustBe defined
        resultNotification.data.get must equal(expectedNotificationData)
      }

      "return Notification containing correct payload" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        val expectedPayload =
          clearNamespaces(Utility.trim(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml))

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        clearNamespaces(XML.loadString(resultNotification.payload)) must equal(expectedPayload)
      }
    }

    "provided with empty Conversation ID" should {

      "create Notification with empty conversationId field" in new Test {
        val responseTypeTest = "ResponseType"
        when(responseParserMock.responseTypeIle).thenReturn(responseTypeTest)
        val testNotificationData = ExampleInventoryLinkingControlResponse.Correct.Rejected
        when(responseParserMock.parse(any())).thenReturn(testNotificationData.asDomainModel)
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

    "provided with response not matching schema" should {

      "not throw an Exception" in new Test {
        val response = ExampleInventoryLinkingControlResponse.Incorrect.DoubleUcrBlock.asXml

        noException should be thrownBy notificationFactory.buildMovementNotification(conversationId, response)
      }

      "return Notification containing correct conversationId" in new Test {
        val response = ExampleInventoryLinkingControlResponse.Incorrect.DoubleUcrBlock.asXml

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, response)

        resultNotification.conversationId must equal(conversationId)
      }

      "return Notification containing notificationData from ResponseParser" in new Test {
        val response = ExampleInventoryLinkingControlResponse.Incorrect.DoubleUcrBlock.asXml
        val expectedNotificationData =
          StandardNotificationData(messageCode = Some("TestMessageCode"), responseType = "inventoryLinkingControlResponse")
        when(responseParserMock.parse(any())).thenReturn(expectedNotificationData)

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, response)

        resultNotification.data mustBe defined
        resultNotification.data.get must equal(expectedNotificationData)
      }

      "return Notification containing correct payload" in new Test {
        val response = ExampleInventoryLinkingControlResponse.Incorrect.DoubleUcrBlock.asXml
        val expectedPayload: Node = clearNamespaces(Utility.trim(response))

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, response)

        clearNamespaces(XML.loadString(resultNotification.payload)) must equal(expectedPayload)
      }
    }
  }

}
