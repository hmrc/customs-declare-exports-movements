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
import uk.gov.hmrc.exports.movements.models.notifications.parsers.ControlResponseParser
import utils.CommonTestData.MessageCodes
import utils.NotificationTestData.{exampleRejectInventoryLinkingControlResponseNotification, exampleRejectInventoryLinkingControlResponseXML}

import scala.xml.{Utility, XML}

class ControlResponseParserSpec extends WordSpec with MustMatchers {

  private trait Test {
    val parser = new ControlResponseParser
  }

  "MovementResponseParser on buildNotification" when {

    "provided with correct inventoryLinkingControlResponse" should {
      "return NotificationData" in new Test {
        val xml = exampleRejectInventoryLinkingControlResponseXML
        val expectedNotificationData =
          exampleRejectInventoryLinkingControlResponseNotification
            .copy(
              payload =
                Utility.trim(XML.loadString(exampleRejectInventoryLinkingControlResponseNotification.payload)).toString
            )
            .data

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }

    "provided with inventoryLinkingControlResponse containing only mandatory data" should {
      "return NotificationData" in new Test {
        val xml =
          <inventoryLinkingControlResponse>
            <messageCode>{MessageCodes.ERS}</messageCode>
            <actionCode>1</actionCode>
          </inventoryLinkingControlResponse>
        val expectedNotificationData =
          NotificationData.empty.copy(messageCode = Some(MessageCodes.ERS), actionCode = Some("1"))

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }

    "provided with missing messageCode" should {
      "return NotificationData with empty messageCode field" in new Test {
        val xml = <inventoryLinkingControlResponse></inventoryLinkingControlResponse>
        val expectedNotificationData = NotificationData.empty

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }
  }

}
