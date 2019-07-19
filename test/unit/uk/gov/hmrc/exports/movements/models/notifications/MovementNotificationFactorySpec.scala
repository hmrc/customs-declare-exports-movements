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
import uk.gov.hmrc.exports.movements.models.notifications.{MovementNotification, MovementNotificationFactory}
import utils.MovementsTestData._
import utils.NotificationTestData._

class MovementNotificationFactorySpec extends WordSpec with MustMatchers {

  private trait Test {
    val notificationFactory = new MovementNotificationFactory
  }

  "MovementNotificationFactory on buildMovementNotification(ConversationId, NodeSeq)" when {

    "provided with correct inventoryLinkingControlResponse" should {
      "return MovementNotification" in new Test {
        val xml = exampleRejectInventoryLinkingControlResponseXML
        val resultNotification = notificationFactory.buildMovementNotification(conversationId, xml)

        val expectedNotification = exampleRejectInventoryLinkingControlResponseNotification
        assertNotificationsEquality(resultNotification, expectedNotification)
      }
    }

    "provided with correct inventoryLinkingMovementTotalsResponse" should {
      "return MovementNotification" in new Test {
        val xml = exampleInventoryLinkingMovementTotalsResponseXML
        val resultNotification = notificationFactory.buildMovementNotification(conversationId, xml)

        val expectedNotification = exampleInventoryLinkingMovementTotalsResponseNotification
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
      "create MovementNotification with empty conversationId field" in new Test {
        val xml = exampleRejectInventoryLinkingControlResponseXML
        val resultNotification = notificationFactory.buildMovementNotification("", xml)

        val expectedNotification = exampleRejectInventoryLinkingControlResponseNotification.copy(conversationId = "")
        assertNotificationsEquality(resultNotification, expectedNotification)
      }
    }
  }

  private def assertNotificationsEquality(actual: MovementNotification, expected: MovementNotification): Unit = {
    actual.conversationId must equal(expected.conversationId)
    actual.errors must equal(expected.errors)
    actual.payload must equal(expected.payload)
  }

}
