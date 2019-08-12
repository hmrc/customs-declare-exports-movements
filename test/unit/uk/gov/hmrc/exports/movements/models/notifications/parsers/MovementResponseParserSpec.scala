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

package unit.uk.gov.hmrc.exports.movements.models.notifications.parsers

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.NotificationData
import uk.gov.hmrc.exports.movements.models.notifications.parsers.MovementResponseParser
import utils.CommonTestData.MessageCodes
import utils.NotificationTestData.{exampleInventoryLinkingMovementResponseNotification, exampleInventoryLinkingMovementResponseXML}

import scala.xml.Utility

class MovementResponseParserSpec extends WordSpec with MustMatchers {

  private trait Test {
    val parser = new MovementResponseParser
  }

  "MovementResponseParser on buildNotification" when {

    "provided with correct inventoryLinkingMovementResponse" should {
      "return NotificationData" in new Test {
        val xml = exampleInventoryLinkingMovementResponseXML
        val expectedNotificationData =
          exampleInventoryLinkingMovementResponseNotification
            .copy(payload = Utility.trim(exampleInventoryLinkingMovementResponseXML).toString)
            .data

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }

    "provided with inventoryLinkingMovementResponse containing only mandatory data" should {
      "return NotificationData" in new Test {
        val xml =
          <inventoryLinkingMovementResponse>
            <messageCode>{MessageCodes.EAL}</messageCode>
          </inventoryLinkingMovementResponse>
        val expectedNotificationData = NotificationData(messageCode = Some(MessageCodes.EAL))

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }

    "provided with missing mandatory fields" should {
      "return NotificationData with empty messageCode field" in new Test {
        val xml = <inventoryLinkingMovementResponse></inventoryLinkingMovementResponse>
        val expectedNotificationData = NotificationData.empty

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }
  }

}
