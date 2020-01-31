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

package utils.testdata

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.exports.movements.models.movements.{Movement, _}
import uk.gov.hmrc.exports.movements.models.notifications.standard.{UcrBlock => UcrBlockModel}
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, Submission}
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import utils.testdata.CommonTestData._

import scala.xml.Node

object MovementsTestData {

  val now: DateTime = DateTime.now.withZone(DateTimeZone.UTC)
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

  val exampleArrivalRequest = Movement(
    eori = validEori,
    providerId = Some(validProviderId),
    choice = MovementType.Arrival,
    consignmentReference = ConsignmentReference("D", ucr),
    movementDetails = Some(MovementDetails(dateTimeString)),
    location = Some(Location("GBAUlocation")),
    transport = None
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

  val exampleRetrospectiveArrivalRequest = Movement(
    eori = validEori,
    providerId = Some(validProviderId),
    choice = MovementType.RetrospectiveArrival,
    consignmentReference = ConsignmentReference("D", ucr),
    movementDetails = None,
    location = Some(Location("GBAUlocation")),
    transport = None
  )

  val exampleRetrospectiveArrivalRequestJson: JsValue = Json.toJson(exampleRetrospectiveArrivalRequest)

  val exampleDepartureRequestXML: Node =
    scala.xml.Utility.trim {
      <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>{MessageCodes.EDL}</messageCode>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
      <goodsLocation>GBAUlocation</goodsLocation>
      <goodsDepartureDateTime>{dateTimeString}</goodsDepartureDateTime>
      <transportDetails>
        <transportID>{transportId}</transportID>
        <transportMode>{transportMode}</transportMode>
        <transportNationality>{transportNationality}</transportNationality>
      </transportDetails>
    </inventoryLinkingMovementRequest>
    }

  val exampleDepartureRequest: Movement = Movement(
    eori = validEori,
    providerId = Some(validProviderId),
    choice = MovementType.Departure,
    consignmentReference = ConsignmentReference("D", "7GB123456789000-123ABC456DEFQWERT"),
    movementDetails = Some(MovementDetails(dateTimeString)),
    location = Some(Location("GBAUlocation")),
    transport = Some(Transport(Some(transportMode), Some(transportNationality), Some(transportId)))
  )

  val exampleDepartureRequestJson: JsValue = Json.toJson(exampleDepartureRequest)

  def exampleSubmission(
    eori: String = validEori,
    providerId: Option[String] = None,
    conversationId: String = conversationId,
    ucr: String = randomUcr,
    ucrType: String = "D",
    actionType: ActionType = ActionType.Arrival
  ): Submission =
    Submission(
      eori = eori,
      providerId = providerId,
      conversationId = conversationId,
      ucrBlocks = Seq(UcrBlockModel(ucr = ucr, ucrType = ucrType)),
      actionType = actionType
    )

  def emptySubmission: Submission =
    Submission(uuid = "", eori = "", providerId = None, conversationId = "", ucrBlocks = Seq.empty, actionType = ActionType.Arrival)

  def validInventoryLinkingExportRequest = InventoryLinkingMovementRequest(
    messageCode = "11",
    agentDetails = Some(AgentDetails(eori = Some(validEori), agentLocation = Some("location"))),
    ucrBlock = UcrBlock(ucr = declarantUcrValue, ucrType = "type"),
    goodsLocation = "goodsLocation",
    goodsArrivalDateTime = Some(now.toString),
    goodsDepartureDateTime = Some(now.toString),
    transportDetails = Some(TransportDetails(transportID = Some("transportId"), transportMode = Some("mode")))
  )

}
