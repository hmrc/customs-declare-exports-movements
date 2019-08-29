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

package unit.uk.gov.hmrc.exports.movements.models.notifications

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.parsers.ResponseParserContext
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationData, NotificationFactory}
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder
import utils.testdata.CommonTestData.conversationId
import utils.testdata.notifications.NotificationTestData._
import utils.testdata.notifications._

import scala.util.Try
import scala.xml.{Node, NodeSeq, Utility, XML}

class NotificationFactorySpec extends WordSpec with MustMatchers with MockitoSugar {

  private trait Test {
    val responseValidatorMock = UnitTestMockBuilder.buildResponseValidatorMock
    val responseParserFactoryMock = UnitTestMockBuilder.buildResponseParserFactoryMock

    when(responseValidatorMock.validate(any[NodeSeq])).thenReturn(Try((): Unit))
    val responseParserMock = UnitTestMockBuilder.buildResponseParserMock
    when(responseParserFactoryMock.buildResponseParser(any())).thenReturn(responseParserMock)
    val exampleResponseParserContext = ResponseParserContext("ResponseType", responseParserMock)
    when(responseParserFactoryMock.buildResponseParserContext(any())).thenReturn(exampleResponseParserContext)

    val notificationFactory = new NotificationFactory(responseValidatorMock, responseParserFactoryMock)
  }

  "MovementNotificationFactory on buildMovementNotification" when {

    "everything works correctly" should {

      "call ResponseParserFactory, passing response XML provided" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        notificationFactory.buildMovementNotification(conversationId, responseXml)

        verify(responseParserFactoryMock).buildResponseParserContext(meq(responseXml))
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

      "return Notification containing responseType from context" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        val expectedResponseType = exampleResponseParserContext.responseType

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        resultNotification.responseType must equal(expectedResponseType)
      }

      "return Notification containing notificationData from ResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        val expectedNotificationData = NotificationData(messageCode = Some("TestMessageCode"))
        when(responseParserMock.parse(any())).thenReturn(expectedNotificationData)

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, responseXml)

        resultNotification.data must equal(expectedNotificationData)
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
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        val expectedNotification = Notification(
          conversationId = "",
          responseType = "inventoryLinkingControlResponse",
          payload = Utility.trim(ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml).toString(),
          data = ExampleInventoryLinkingControlResponse.Correct.Rejected.asNotificationData
        )

        val resultNotification = notificationFactory.buildMovementNotification("", responseXml)

        resultNotification.conversationId must equal("")
        resultNotification.responseType mustNot be(empty)
        resultNotification.payload mustNot be(empty)
        resultNotification.data must equal(NotificationData.empty)
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

      "return Notification containing responseType from context" in new Test {
        val response = ExampleInventoryLinkingControlResponse.Incorrect.DoubleUcrBlock.asXml
        val expectedResponseType: String = exampleResponseParserContext.responseType

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, response)

        resultNotification.responseType must equal(expectedResponseType)
      }

      "return Notification containing notificationData from ResponseParser" in new Test {
        val response = ExampleInventoryLinkingControlResponse.Incorrect.DoubleUcrBlock.asXml
        val expectedNotificationData = NotificationData(messageCode = Some("TestMessageCode"))
        when(responseParserMock.parse(any())).thenReturn(expectedNotificationData)

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, response)

        resultNotification.data must equal(expectedNotificationData)
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
