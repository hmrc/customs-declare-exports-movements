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

package utils.testdata.notifications

import java.time.Instant

import utils.testdata.CommonTestData._
import utils.testdata.notifications.ExampleXmlAndDomainModelPair.ExampleStandardResponse
import utils.testdata.notifications.NotificationTestData._
import uk.gov.hmrc.exports.movements.models.notifications.standard._

object ExampleInventoryLinkingMovementResponse {

  object Correct {

    lazy val AllElements = ExampleStandardResponse(
      asXml = <inventoryLinkingMovementResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.EAL}</messageCode>
          <crc>{crcCode_success}</crc>
          <goodsArrivalDateTime>2019-07-12T13:14:54.000Z</goodsArrivalDateTime>
          <goodsLocation>{goodsLocation}</goodsLocation>
          <movementReference>{movementReference}</movementReference>
          <submitRole>{submitRole}</submitRole>
          <ucrBlock>
            <ucr>{ucr}</ucr>
            <ucrType>D</ucrType>
          </ucrBlock>
          <goodsItem>
            <commodityCode>{commodityCode_1}</commodityCode>
            <totalPackages>{totalPackages_1}</totalPackages>
            <totalNetMass>{totalNetMass_1}</totalNetMass>
          </goodsItem>
          <goodsItem>
            <commodityCode>{commodityCode_2}</commodityCode>
            <totalPackages>{totalPackages_2}</totalPackages>
            <totalNetMass>{totalNetMass_2}</totalNetMass>
          </goodsItem>
          <entryStatus>
            <ics>7</ics>
            <roe>6</roe>
            <soe>3</soe>
          </entryStatus>
        </inventoryLinkingMovementResponse>,
      asDomainModel = StandardNotificationData(
        responseType = "inventoryLinkingMovementResponse",
        messageCode = Some(MessageCodes.EAL),
        crcCode = Some(crcCode_success),
        goodsArrivalDateTime = Some(Instant.parse("2019-07-12T13:14:54.000Z")),
        goodsLocation = Some("Location"),
        movementReference = Some("MovRef001234"),
        entries = Seq(
          Entry(
            ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "D")),
            entryStatus = Some(EntryStatus(ics = Some("7"), roe = Some("6"), soe = Some("3"))),
            goodsItem = Seq(
              GoodsItem(
                commodityCode = Some(commodityCode_1),
                totalPackages = Some(totalPackages_1),
                totalNetMass = Some(BigDecimal(totalNetMass_1))
              ),
              GoodsItem(commodityCode = Some(commodityCode_2), totalPackages = Some(totalPackages_2), totalNetMass = Some(BigDecimal(totalNetMass_2)))
            )
          )
        )
      )
    )

    lazy val MandatoryElementsOnly = ExampleStandardResponse(asXml = <inventoryLinkingMovementResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.EAL}</messageCode>
        </inventoryLinkingMovementResponse>)

  }

  object Incorrect {

    lazy val NoMessageCode = ExampleStandardResponse(asXml = <inventoryLinkingMovementResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
        </inventoryLinkingMovementResponse>)

    lazy val WrongMessageCode = ExampleStandardResponse(asXml = <inventoryLinkingMovementResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.EAC}</messageCode>
      </inventoryLinkingMovementResponse>)
  }
}
