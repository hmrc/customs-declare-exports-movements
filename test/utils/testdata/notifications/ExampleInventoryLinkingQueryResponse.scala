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

package utils.testdata.notifications

import java.time.Instant

import uk.gov.hmrc.exports.movements.models.movements.Transport
import uk.gov.hmrc.exports.movements.models.notifications.queries._
import uk.gov.hmrc.exports.movements.models.notifications.standard.EntryStatus
import utils.testdata.CommonTestData._
import utils.testdata.notifications.ExampleXmlAndDomainModelPair.ExampleQueryResponse
import utils.testdata.notifications.NotificationTestData.{declarationId, declarationId_2, goodsLocation}

object ExampleInventoryLinkingQueryResponse {

  object Correct {

    lazy val Empty = ExampleQueryResponse(asXml = <inventoryLinkingQueryResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
      </inventoryLinkingQueryResponse>, asDomainModel = IleQueryResponseData())

    lazy val QueriedDucr = ExampleQueryResponse(
      asXml = <inventoryLinkingQueryResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">

          <queriedDUCR>
            <UCR>{ucr}</UCR>
            <parentMUCR>{mucr}</parentMUCR>
            <declarationID>{declarationId}</declarationID>
            <entryStatus>
              <ics>2</ics>
              <roe>H</roe>
              <soe>14</soe>
            </entryStatus>
            <movement>
              <messageCode>{MessageCodes.EAL}</messageCode>
              <goodsLocation>{goodsLocation}</goodsLocation>
              <goodsArrivalDateTime>2019-12-23T11:30:00.000Z</goodsArrivalDateTime>
              <movementReference>{movementReference}</movementReference>
            </movement>
            <movement>
              <messageCode>{MessageCodes.EDL}</messageCode>
              <goodsLocation>{goodsLocation}</goodsLocation>
              <goodsDepartureDateTime>2019-12-23T11:40:00.000Z</goodsDepartureDateTime>
              <movementReference>{movementReference}</movementReference>
              <transportDetails>
                <transportID>{transportId}</transportID>
                <transportMode>{transportMode}</transportMode>
                <transportNationality>{transportNationality}</transportNationality>
              </transportDetails>
            </movement>
            <goodsItem>
              <totalPackages>11</totalPackages>
            </goodsItem>
          </queriedDUCR>

      </inventoryLinkingQueryResponse>,
      asDomainModel = IleQueryResponseData(
        queriedDucr = Some(
          DucrInfo(
            ucr = ucr,
            parentMucr = Some(mucr),
            declarationId = declarationId,
            entryStatus = Some(EntryStatus(ics = Some("2"), roe = Some("H"), soe = Some("14"))),
            movements = Seq(
              MovementInfo(
                messageCode = MessageCodes.EAL,
                goodsLocation = goodsLocation,
                goodsArrivalDateTime = Some(Instant.parse("2019-12-23T11:30:00.000Z")),
                movementReference = Some(movementReference)
              ),
              MovementInfo(
                messageCode = MessageCodes.EDL,
                goodsLocation = goodsLocation,
                goodsDepartureDateTime = Some(Instant.parse("2019-12-23T11:40:00.000Z")),
                movementReference = Some(movementReference),
                transportDetails =
                  Some(Transport(transportId = Some(transportId), modeOfTransport = Some(transportMode), nationality = Some(transportNationality)))
              )
            ),
            goodsItem = Seq(GoodsItemInfo(totalPackages = Some(11)))
          )
        )
      )
    )

    lazy val QueriedMucr = ExampleQueryResponse(
      asXml = <inventoryLinkingQueryResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">

          <queriedMUCR>
            <UCR>{mucr_2}</UCR>
            <parentMUCR>{mucr}</parentMUCR>
            <entryStatus>
              <ics>7</ics>
              <roe>6</roe>
              <soe>3</soe>
            </entryStatus>
            <shut>true</shut>
            <movement>
              <messageCode>{MessageCodes.EAL}</messageCode>
              <goodsLocation>{goodsLocation}</goodsLocation>
              <goodsArrivalDateTime>2019-12-23T11:30:00.000Z</goodsArrivalDateTime>
              <movementReference>{movementReference}</movementReference>
            </movement>
          </queriedMUCR>

      </inventoryLinkingQueryResponse>,
      asDomainModel = IleQueryResponseData(
        queriedMucr = Some(
          MucrInfo(
            ucr = mucr_2,
            parentMucr = Some(mucr),
            entryStatus = Some(EntryStatus(ics = Some("7"), roe = Some("6"), soe = Some("3"))),
            isShut = Some(true),
            movements = Seq(
              MovementInfo(
                messageCode = MessageCodes.EAL,
                goodsLocation = goodsLocation,
                goodsArrivalDateTime = Some(Instant.parse("2019-12-23T11:30:00.000Z")),
                movementReference = Some(movementReference)
              )
            )
          )
        )
      )
    )

    lazy val ParentMucr = ExampleQueryResponse(
      asXml = <inventoryLinkingQueryResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">

          <parentMUCR>
            <UCR>{mucr_2}</UCR>
            <parentMUCR>{mucr}</parentMUCR>
            <entryStatus>
              <ics>7</ics>
              <roe>6</roe>
              <soe>3</soe>
            </entryStatus>
            <shut>true</shut>
            <movement>
              <messageCode>{MessageCodes.EAL}</messageCode>
              <goodsLocation>{goodsLocation}</goodsLocation>
              <goodsArrivalDateTime>2019-12-23T11:30:00.000Z</goodsArrivalDateTime>
              <movementReference>{movementReference}</movementReference>
            </movement>
          </parentMUCR>

      </inventoryLinkingQueryResponse>,
      asDomainModel = IleQueryResponseData(
        parentMucr = Some(
          MucrInfo(
            ucr = mucr_2,
            parentMucr = Some(mucr),
            entryStatus = Some(EntryStatus(ics = Some("7"), roe = Some("6"), soe = Some("3"))),
            isShut = Some(true),
            movements = Seq(
              MovementInfo(
                messageCode = MessageCodes.EAL,
                goodsLocation = goodsLocation,
                goodsArrivalDateTime = Some(Instant.parse("2019-12-23T11:30:00.000Z")),
                movementReference = Some(movementReference)
              )
            )
          )
        )
      )
    )

    lazy val ChildDucrs = ExampleQueryResponse(
      asXml = <inventoryLinkingQueryResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">

          <childDUCR>
            <UCR>{ucr}</UCR>
            <parentMUCR>{mucr}</parentMUCR>
            <declarationID>{declarationId}</declarationID>
            <entryStatus>
              <ics>2</ics>
              <roe>H</roe>
              <soe>14</soe>
            </entryStatus>
          </childDUCR>
          <childDUCR>
            <UCR>{ucr_2}</UCR>
            <parentMUCR>{mucr}</parentMUCR>
            <declarationID>{declarationId_2}</declarationID>
            <entryStatus>
              <ics>7</ics>
              <roe>6</roe>
              <soe>3</soe>
            </entryStatus>
          </childDUCR>

      </inventoryLinkingQueryResponse>,
      asDomainModel = IleQueryResponseData(
        childDucrs = Seq(
          DucrInfo(
            ucr = ucr,
            parentMucr = Some(mucr),
            declarationId = declarationId,
            entryStatus = Some(EntryStatus(ics = Some("2"), roe = Some("H"), soe = Some("14")))
          ),
          DucrInfo(
            ucr = ucr_2,
            parentMucr = Some(mucr),
            declarationId = declarationId_2,
            entryStatus = Some(EntryStatus(ics = Some("7"), roe = Some("6"), soe = Some("3")))
          )
        )
      )
    )

    lazy val ChildMucrs = ExampleQueryResponse(
      asXml = <inventoryLinkingQueryResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">

          <childMUCR>
            <UCR>{mucr}</UCR>
            <entryStatus>
              <ics>2</ics>
              <roe>H</roe>
              <soe>14</soe>
            </entryStatus>
            <shut>true</shut>
          </childMUCR>
          <childMUCR>
            <UCR>{mucr_2}</UCR>
            <entryStatus>
              <ics>7</ics>
              <roe>6</roe>
              <soe>3</soe>
            </entryStatus>
            <shut>false</shut>
          </childMUCR>

      </inventoryLinkingQueryResponse>,
      asDomainModel = IleQueryResponseData(
        childMucrs = Seq(
          MucrInfo(ucr = mucr, entryStatus = Some(EntryStatus(ics = Some("2"), roe = Some("H"), soe = Some("14"))), isShut = Some(true)),
          MucrInfo(ucr = mucr_2, entryStatus = Some(EntryStatus(ics = Some("7"), roe = Some("6"), soe = Some("3"))), isShut = Some(false))
        )
      )
    )
  }

}
