/*
 * Copyright 2023 HM Revenue & Customs
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

package utils.testdata

import play.api.libs.json.{JsValue, Json}
import utils.testdata.CommonTestData._
import uk.gov.hmrc.exports.movements.models.movements.{MovementsExchange, _}
import uk.gov.hmrc.exports.movements.models.notifications.standard.{UcrBlock => UcrBlockModel}
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.MovementType
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, IleQuerySubmission, Submission}

import java.time.{Instant, ZonedDateTime}
import scala.xml.Node

object MovementsTestData {

  val now: ZonedDateTime = ZonedDateTime.now()
  val dateTimeString: String = "2019-07-12T13:14:54Z"

  def exampleArrivalRequestXML(reference: String): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.EAL}</messageCode>
        <ucrBlock>
          <ucr>{ucr}</ucr>
          <ucrType>D</ucrType>
        </ucrBlock>
        <goodsLocation>GBAUlocation</goodsLocation>
        <goodsArrivalDateTime>{dateTimeString}</goodsArrivalDateTime>
        <movementReference>{reference}</movementReference>
      </inventoryLinkingMovementRequest>
    }

  val exampleArrivalRequest = MovementsExchange(
    eori = validEori,
    providerId = Some(validProviderId),
    choice = MovementType.Arrival,
    consignmentReference = ConsignmentReference("D", ucr),
    movementDetails = Some(MovementDetails(dateTimeString)),
    location = Some(Location("GBAUlocation"))
  )

  val exampleArrivalRequestJson: JsValue = Json.toJson(exampleArrivalRequest)

  def exampleRetrospectiveArrivalRequestXML(reference: String): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.RET}</messageCode>
        <ucrBlock>
          <ucr>{ucr}</ucr>
          <ucrType>D</ucrType>
        </ucrBlock>
        <goodsLocation>GBAUlocation</goodsLocation>
        <goodsArrivalDateTime>{dateTimeString}</goodsArrivalDateTime>
        <movementReference>{reference}</movementReference>
      </inventoryLinkingMovementRequest>
    }

  val exampleRetrospectiveArrivalRequest = MovementsExchange(
    eori = validEori,
    providerId = Some(validProviderId),
    choice = MovementType.RetrospectiveArrival,
    consignmentReference = ConsignmentReference(reference = "D", referenceValue = ucr),
    location = Some(Location("GBAUlocation"))
  )

  val exampleRetrospectiveArrivalRequestJson: JsValue = Json.toJson(exampleRetrospectiveArrivalRequest)

  def exampleDepartureRequestXML(reference: String): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>{MessageCodes.EDL}</messageCode>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
      <goodsLocation>GBAUlocation</goodsLocation>
      <goodsDepartureDateTime>{dateTimeString}</goodsDepartureDateTime>
        <movementReference>{reference}</movementReference>
      <transportDetails>
        <transportID>{transportId}</transportID>
        <transportMode>{transportMode}</transportMode>
        <transportNationality>{transportNationality}</transportNationality>
      </transportDetails>
    </inventoryLinkingMovementRequest>
    }

  val exampleDepartureRequest: MovementsExchange = MovementsExchange(
    eori = validEori,
    providerId = Some(validProviderId),
    choice = MovementType.Departure,
    consignmentReference = ConsignmentReference(reference = "D", referenceValue = ucr),
    movementDetails = Some(MovementDetails(dateTimeString)),
    location = Some(Location("GBAUlocation")),
    transport = Some(Transport(Some(transportMode), Some(transportNationality), Some(transportId)))
  )

  val exampleDepartureRequestJson: JsValue = Json.toJson(exampleDepartureRequest)

  def exampleCreateEmptyMucrRequestXML(reference: String): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.EAL}</messageCode>
        <ucrBlock>
          <ucr>{ucr}</ucr>
          <ucrType>D</ucrType>
        </ucrBlock>
        <goodsLocation>GBAUlocation</goodsLocation>
        <goodsArrivalDateTime>{dateTimeString}</goodsArrivalDateTime>
        <movementReference>{reference}</movementReference>
      </inventoryLinkingMovementRequest>
    }

  val exampleCreateEmptyMucrRequest: MovementsExchange = MovementsExchange(
    eori = validEori,
    providerId = Some(validProviderId),
    choice = MovementType.CreateEmptyMucr,
    consignmentReference = ConsignmentReference(reference = "D", referenceValue = ucr),
    location = Some(Location("GBAUlocation"))
  )

  val exampleCreateEmptyMucrRequestJson: JsValue = Json.toJson(exampleDepartureRequest)

  def exampleSubmission(
    eori: String = validEori,
    providerId: Option[String] = None,
    conversationId: String = conversationId,
    ucr: String = randomUcr,
    ucrType: String = "D",
    actionType: ActionType = MovementType.Arrival
  ): Submission =
    Submission(
      eori = eori,
      providerId = providerId,
      conversationId = conversationId,
      ucrBlocks = Seq(UcrBlockModel(ucr = ucr, ucrType = ucrType)),
      actionType = actionType,
      requestTimestamp = Instant.parse(dateTimeString)
    )

  def emptySubmission: Submission =
    Submission(uuid = "", eori = "", providerId = None, conversationId = "", ucrBlocks = Seq.empty, actionType = MovementType.Arrival)

  def exampleIleQuerySubmission(
    eori: String = validEori,
    providerId: Option[String] = None,
    conversationId: String = conversationId,
    ucr: String = ucr,
    ucrType: String = "D"
  ): IleQuerySubmission =
    IleQuerySubmission(
      eori = eori,
      providerId = providerId,
      conversationId = conversationId,
      ucrBlock = UcrBlockModel(ucr = ucr, ucrType = ucrType),
      requestTimestamp = Instant.parse(dateTimeString)
    )

}
