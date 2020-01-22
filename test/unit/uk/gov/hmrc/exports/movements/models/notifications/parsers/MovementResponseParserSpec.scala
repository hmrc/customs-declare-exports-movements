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
import uk.gov.hmrc.exports.movements.models.notifications.NotificationData
import uk.gov.hmrc.exports.movements.models.notifications.parsers.{CommonTypesParser, MovementResponseParser}
import uk.gov.hmrc.exports.movements.models.notifications.standard.StandardNotificationData
import utils.testdata.CommonTestData.MessageCodes
import utils.testdata.notifications.ExampleInventoryLinkingMovementResponse

class MovementResponseParserSpec extends WordSpec with MustMatchers {

  private trait Test {
    val commonTypesParser = new CommonTypesParser
    val parser = new MovementResponseParser(commonTypesParser)
  }

  "MovementResponseParser on parse" when {

    "provided with correct inventoryLinkingMovementResponse" should {
      "return NotificationData" in new Test {
        val xml = ExampleInventoryLinkingMovementResponse.Correct.AllElements.asXml
        val expectedNotificationData: NotificationData =
          ExampleInventoryLinkingMovementResponse.Correct.AllElements.asDomainModel

        val resultNotificationData: NotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }

    "provided with inventoryLinkingMovementResponse containing only mandatory data" should {
      "return NotificationData" in new Test {
        val xml = ExampleInventoryLinkingMovementResponse.Correct.MandatoryElementsOnly.asXml
        val expectedNotificationData = StandardNotificationData(messageCode = Some(MessageCodes.EAL))

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }

    "provided with missing mandatory fields" should {
      "return NotificationData with empty messageCode field" in new Test {
        val xml = ExampleInventoryLinkingMovementResponse.Incorrect.NoMessageCode.asXml
        val expectedNotificationData = StandardNotificationData()

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }
  }

}
