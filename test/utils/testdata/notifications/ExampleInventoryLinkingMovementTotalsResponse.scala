/*
 * Copyright 2022 HM Revenue & Customs
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

package testdata.notifications

import java.time.Instant

import testdata.CommonTestData._
import testdata.notifications.ExampleXmlAndDomainModelPair.ExampleStandardResponse
import testdata.notifications.NotificationTestData._
import uk.gov.hmrc.exports.movements.models.notifications.standard._

object ExampleInventoryLinkingMovementTotalsResponse {

  object Correct {

    lazy val AllElements = ExampleStandardResponse(
      asXml = <inventoryLinkingMovementTotalsResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.ERS}</messageCode>
          <crc>{crcCode_success}</crc>
          <goodsLocation>{goodsLocation}</goodsLocation>
          <masterUCR>{ucr_2}</masterUCR>
          <declarationCount>{declarationCount}</declarationCount>
          <goodsArrivalDateTime>2019-07-12T13:14:54.000Z</goodsArrivalDateTime>
          <movementReference>{movementReference}</movementReference>
          <masterROE>RE</masterROE>
          <masterSOE>SO</masterSOE>
          <entry>
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
            <submitRole>{submitRole}</submitRole>
            <entryStatus>
              <ics>7</ics>
              <roe>6</roe>
              <soe>3</soe>
            </entryStatus>
          </entry>
        </inventoryLinkingMovementTotalsResponse>,
      asDomainModel = StandardNotificationData(
        responseType = "inventoryLinkingMovementTotalsResponse",
        messageCode = Some(MessageCodes.ERS),
        crcCode = Some(crcCode_success),
        declarationCount = Some(declarationCount),
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
        ),
        goodsArrivalDateTime = Some(Instant.parse("2019-07-12T13:14:54.000Z")),
        goodsLocation = Some(goodsLocation),
        masterRoe = Some("RE"),
        masterSoe = Some("SO"),
        masterUcr = Some(ucr_2),
        movementReference = Some("MovRef001234")
      )
    )

    lazy val MandatoryElementsOnly = ExampleStandardResponse(asXml = <inventoryLinkingMovementTotalsResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.ERS}</messageCode>
        <goodsLocation>{goodsLocation}</goodsLocation>
      </inventoryLinkingMovementTotalsResponse>)

  }

  object Incorrect {

    lazy val NoMessageCode = ExampleStandardResponse(asXml = <inventoryLinkingMovementTotalsResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <goodsLocation>{goodsLocation}</goodsLocation>
        </inventoryLinkingMovementTotalsResponse>)

    lazy val NoGoodsLocation = ExampleStandardResponse(asXml = <inventoryLinkingMovementTotalsResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.ERS}</messageCode>
        </inventoryLinkingMovementTotalsResponse>)

    lazy val WrongMessageCode = ExampleStandardResponse(asXml = <inventoryLinkingMovementTotalsResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.EAC}</messageCode>
          <goodsLocation>{goodsLocation}</goodsLocation>
        </inventoryLinkingMovementTotalsResponse>)

    lazy val WrongGoodsLocation = ExampleStandardResponse(asXml = <inventoryLinkingMovementTotalsResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.ERS}</messageCode>
          <goodsLocation>GoodsLocationThatIsTooLong</goodsLocation>
        </inventoryLinkingMovementTotalsResponse>)

    lazy val EntryNodeWithoutUcrBlockNode = ExampleStandardResponse(asXml = <inventoryLinkingMovementTotalsResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.ERS}</messageCode>
          <goodsLocation>{goodsLocation}</goodsLocation>
          <entry>
            <entryStatus>
              <ics>7</ics>
              <roe>6</roe>
              <soe>3</soe>
            </entryStatus>
            <submitRole>{submitRole}</submitRole>
            <goodsItem>
              <commodityCode>{commodityCode_1}</commodityCode>
              <totalPackages>{totalPackages_1}</totalPackages>
              <totalNetMass>{totalNetMass_1}</totalNetMass>
            </goodsItem>
          </entry>
        </inventoryLinkingMovementTotalsResponse>)
  }
}
