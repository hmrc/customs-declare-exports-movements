/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.exports.movements.models.notifications.parsers

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.{Environment, Mode}
import uk.gov.hmrc.exports.movements.models.notifications.standard.StandardNotificationData
import uk.gov.hmrc.exports.movements.utils.JsonFile
import utils.testdata.CommonTestData.MessageCodes
import utils.testdata.notifications.ExampleInventoryLinkingControlResponse
import utils.testdata.notifications.NotificationTestData._

class ControlResponseParserSpec extends AnyWordSpec with Matchers {

  private trait Test {

    private lazy val jsonFile = new JsonFile(Environment.simple(mode = Mode.Test))
    val errorValidator = new ErrorValidator(jsonFile)
    val parser = new ControlResponseParser(errorValidator)
  }

  "MovementResponseParser on parse" when {

    "provided with correct inventoryLinkingControlResponse" should {
      "return NotificationData for MUCR" in new Test {
        val xml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml
        val expectedNotificationData =
          ExampleInventoryLinkingControlResponse.Correct.Rejected.asDomainModel

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }

    "provided with multiple errorCodes" should {
      "return NotificationData" in new Test {
        val xml = ExampleInventoryLinkingControlResponse.Correct.RejectedMultipleErrors.asXml
        val expectedNotificationData =
          ExampleInventoryLinkingControlResponse.Correct.RejectedMultipleErrors.asDomainModel

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }

    "provided with inventoryLinkingControlResponse" when {
      "containing only mandatory data should return NotificationData" in new Test {
        val xml =
          <inventoryLinkingControlResponse>
            <messageCode>{MessageCodes.ERS}</messageCode>
            <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
          </inventoryLinkingControlResponse>

        val expectedNotificationData =
          StandardNotificationData(responseType = "inventoryLinkingControlResponse")
            .copy(messageCode = Some(MessageCodes.ERS), actionCode = Some(actionCode_acknowledgedAndProcessed))

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }

      "containing a known error code should return populated NotificationData" in new Test {
        val error = "6 E3303 Inventory System not valid for Freight Location"
        val xml =
          <inventoryLinkingControlResponse>
            <messageCode>{MessageCodes.ERS}</messageCode>
            <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
            <error>
              <errorCode>{error}</errorCode>
            </error>
          </inventoryLinkingControlResponse>

        val expectedNotificationData =
          StandardNotificationData(responseType = "inventoryLinkingControlResponse")
            .copy(messageCode = Some(MessageCodes.ERS), actionCode = Some(actionCode_acknowledgedAndProcessed), errorCodes = Seq(error))

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }

      "containing an unknown error code should return populated NotificationData" in new Test {
        val xml =
          <inventoryLinkingControlResponse>
            <messageCode>{MessageCodes.ERS}</messageCode>
            <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
            <error>
              <errorCode>6 E533 Inventory System not valid for Freight Location</errorCode>
            </error>
          </inventoryLinkingControlResponse>

        val expectedNotificationData =
          StandardNotificationData(responseType = "inventoryLinkingControlResponse")
            .copy(
              messageCode = Some(MessageCodes.ERS),
              actionCode = Some(actionCode_acknowledgedAndProcessed),
              errorCodes = Seq("6 E533 Inventory System not valid for Freight Location")
            )

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }

    "provided with missing messageCode" should {
      "return NotificationData with empty messageCode field" in new Test {
        val xml = <inventoryLinkingControlResponse></inventoryLinkingControlResponse>
        val expectedNotificationData = StandardNotificationData(responseType = "inventoryLinkingControlResponse")

        val resultNotificationData = parser.parse(xml)

        resultNotificationData must equal(expectedNotificationData)
      }
    }
  }
}
