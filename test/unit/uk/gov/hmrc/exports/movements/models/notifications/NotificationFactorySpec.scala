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

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationData, NotificationFactory}
import utils.MovementsTestData._
import utils.NotificationTestData._

import scala.xml.{Elem, Utility, XML}

class NotificationFactorySpec extends WordSpec with MustMatchers {

  private trait Test {
    val notificationFactory = new NotificationFactory
  }

  "MovementNotificationFactory on buildMovementNotification(ConversationId, NodeSeq)" when {

    "provided with correct inventoryLinkingControlResponse" should {
      "return Notification" in new Test {
        val xml = exampleRejectInventoryLinkingControlResponseXML
        val expectedNotification =
          exampleRejectInventoryLinkingControlResponseNotification.copy(
            payload =
              Utility.trim(XML.loadString(exampleRejectInventoryLinkingControlResponseNotification.payload)).toString
          )

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, xml)

        assertNotificationsEquality(resultNotification, expectedNotification)
      }
    }

    "provided with correct inventoryLinkingMovementTotalsResponse" should {
      "return Notification" in new Test {
        val xml = exampleInventoryLinkingMovementTotalsResponseXML
        val expectedNotification =
          exampleInventoryLinkingMovementTotalsResponseNotification.copy(
            payload =
              Utility.trim(XML.loadString(exampleInventoryLinkingMovementTotalsResponseNotification.payload)).toString
          )

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, xml)

        assertNotificationsEquality(resultNotification, expectedNotification)
      }
    }

    "provided with correct inventoryLinkingMovementResponse" should {
      "return Notification" in new Test {
        val xml = exampleInventoryLinkingMovementResponseXML
        val expectedNotification =
          exampleInventoryLinkingMovementResponseNotification.copy(
            payload = Utility.trim(exampleInventoryLinkingMovementResponseXML).toString
          )

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, xml)

        assertNotificationsEquality(resultNotification, expectedNotification)
      }
    }

    "provided with unknown XML format" should {
      "throw an IllegalArgumentException" in new Test {
        val xml = unknownFormatResponseXML
        val exc = intercept[IllegalArgumentException] {
          notificationFactory.buildMovementNotification(conversationId, xml)
        }

        exc.getMessage must include("Unknown Inventory Linking Response: UnknownFormat")
      }
    }

    "provided with empty Conversation ID" should {
      "create Notification with empty conversationId field" in new Test {
        val xml = exampleRejectInventoryLinkingControlResponseXML
        val expectedNotification = exampleRejectInventoryLinkingControlResponseNotification.copy(
          conversationId = "",
          payload =
            Utility.trim(XML.loadString(exampleRejectInventoryLinkingControlResponseNotification.payload)).toString
        )

        val resultNotification = notificationFactory.buildMovementNotification("", xml)

        assertNotificationsEquality(resultNotification, expectedNotification)
      }
    }

    "provided with only mandatory fields" should {
      "create Notification with empty nested fields" in new Test {
        val xml: Elem =
          <inventoryLinkingMovementTotalsResponse>
            <messageCode>{MessageCodes.ERS}</messageCode>
            <goodsLocation>{goodsLocation}</goodsLocation>
          </inventoryLinkingMovementTotalsResponse>
        val expectedNotification = Notification(
          conversationId = conversationId,
          payload = Utility.trim(xml).toString,
          responseType = "inventoryLinkingMovementTotalsResponse",
          data = NotificationData(messageCode = MessageCodes.ERS, goodsLocation = Some(goodsLocation))
        )

        val resultNotification = notificationFactory.buildMovementNotification(conversationId, xml)

        assertNotificationsEquality(resultNotification, expectedNotification)
      }
    }
  }

  private def assertNotificationsEquality(actual: Notification, expected: Notification): Unit = {
    actual.conversationId must equal(expected.conversationId)
    actual.responseType must equal(expected.responseType)
    actual.payload must equal(expected.payload)

    actual.data.messageCode must equal(expected.data.messageCode)
    actual.data.actionCode must equal(expected.data.actionCode)
    actual.data.crcCode must equal(expected.data.crcCode)
    actual.data.declarationCount must equal(expected.data.declarationCount)
    actual.data.entries must equal(expected.data.entries)
    actual.data.errorCode must equal(expected.data.errorCode)
    actual.data.goodsArrivalDateTime must equal(expected.data.goodsArrivalDateTime)
    actual.data.goodsLocation must equal(expected.data.goodsLocation)
    actual.data.masterRoe must equal(expected.data.masterRoe)
    actual.data.masterSoe must equal(expected.data.masterSoe)
    actual.data.masterUcr must equal(expected.data.masterUcr)
    actual.data.movementReference must equal(expected.data.movementReference)
  }

}
