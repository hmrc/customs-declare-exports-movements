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
import uk.gov.hmrc.exports.movements.models.notifications.parsers.MovementTotalsResponseParser
import utils.MovementsTestData.MessageCodes
import utils.NotificationTestData.{exampleInventoryLinkingMovementTotalsResponseNotification, exampleInventoryLinkingMovementTotalsResponseXML, goodsLocation}

import scala.xml.{Utility, XML}

class MovementTotalsResponseParserSpec extends WordSpec with MustMatchers {

  private trait Test {
    val parser = new MovementTotalsResponseParser
  }

  "MovementResponseParser on buildNotification" when {

    "provided with correct inventoryLinkingMovementTotalsResponse" should {
      "return NotificationData" in new Test {
        val xml = exampleInventoryLinkingMovementTotalsResponseXML
        val expectedNotificationData =
          exampleInventoryLinkingMovementTotalsResponseNotification
            .copy(
              payload =
                Utility.trim(XML.loadString(exampleInventoryLinkingMovementTotalsResponseNotification.payload)).toString
            )
            .data

        val resultNotification = parser.parse(xml)

        NotificationsComparator.assertEquality(resultNotification, expectedNotificationData)
      }
    }

    "provided with inventoryLinkingMovementTotalsResponse containing only mandatory data" should {
      "return NotificationData" in new Test {
        val xml =
          <inventoryLinkingMovementTotalsResponse>
            <messageCode>{MessageCodes.ERS}</messageCode>
            <goodsLocation>{goodsLocation}</goodsLocation>
          </inventoryLinkingMovementTotalsResponse>
        val expectedNotificationData =
          NotificationData(messageCode = Some(MessageCodes.ERS), goodsLocation = Some(goodsLocation))

        val resultNotification = parser.parse(xml)

        NotificationsComparator.assertEquality(resultNotification, expectedNotificationData)
      }
    }

    "provided with missing mandatory fields" should {
      "return NotificationData with empty messageCode field" in new Test {
        val xml = <inventoryLinkingMovementTotalsResponse></inventoryLinkingMovementTotalsResponse>
        val expectedNotificationData = NotificationData.empty

        val resultNotification = parser.parse(xml)

        NotificationsComparator.assertEquality(resultNotification, expectedNotificationData)
      }
    }
  }

}
