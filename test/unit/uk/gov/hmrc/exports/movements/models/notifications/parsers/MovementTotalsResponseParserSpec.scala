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

package unit.uk.gov.hmrc.exports.movements.models.notifications.parsers

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.parsers.{CommonTypesParser, MovementTotalsResponseParser}
import uk.gov.hmrc.exports.movements.models.notifications.standard.StandardNotificationData
import utils.testdata.CommonTestData.MessageCodes
import utils.testdata.notifications.ExampleInventoryLinkingMovementTotalsResponse
import utils.testdata.notifications.NotificationTestData.goodsLocation

class MovementTotalsResponseParserSpec extends WordSpec with MustMatchers {

  private trait Test {
    val commonTypesParser = new CommonTypesParser
    val parser = new MovementTotalsResponseParser(commonTypesParser)
  }

  "MovementResponseParser on parse" when {

    "provided with correct inventoryLinkingMovementTotalsResponse" should {
      "return NotificationData" in new Test {
        val xml = ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asXml
        val expectedNotificationData =
          ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asDomainModel

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
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
          StandardNotificationData(messageCode = Some(MessageCodes.ERS), goodsLocation = Some(goodsLocation))

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }

    "provided with missing mandatory fields" should {
      "return NotificationData with empty messageCode field" in new Test {
        val xml = <inventoryLinkingMovementTotalsResponse></inventoryLinkingMovementTotalsResponse>
        val expectedNotificationData = StandardNotificationData()

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }
  }

}
