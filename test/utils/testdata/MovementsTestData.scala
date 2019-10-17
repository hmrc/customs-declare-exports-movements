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

package utils.testdata

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.exports.movements.controllers.request.MovementRequest
import uk.gov.hmrc.exports.movements.models.movements._
import uk.gov.hmrc.exports.movements.models.notifications.{UcrBlock => UcrBlockModel}
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, Submission}
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import utils.testdata.CommonTestData._

import scala.xml.Node

object MovementsTestData {

  val now: DateTime = DateTime.now.withZone(DateTimeZone.UTC)

  val exampleArrivalRequestXML: Node =
    scala.xml.Utility.trim {
      <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.EAL}</messageCode>
        <ucrBlock>
          <ucr>{ucr}</ucr>
          <ucrType>D</ucrType>
        </ucrBlock>
        <goodsLocation>GBAUlocation</goodsLocation>
        <goodsArrivalDateTime>2019-07-12T13:14:54.000Z</goodsArrivalDateTime>
        <movementReference>{movementReference}</movementReference>
        <transportDetails>
          <transportID>{transportId}</transportID>
          <transportMode>{transportMode}</transportMode>
          <transportNationality>{transportNationality}</transportNationality>
        </transportDetails>
      </inventoryLinkingMovementRequest>
    }

  val exampleArrivalRequest = MovementRequest(
    choice = "EAL",
    consignmentReference = ConsignmentReference("D", "7GB123456789000-123ABC456DEFQWERT"),
    movementDetails = MovementDetails("2019-07-12T13:14:54.000Z"),
    location = Some(Location("GBAUlocation")),
    arrivalReference = Some(ArrivalReference(Some(movementReference))),
    transport = Some(Transport(transportMode, transportNationality, transportId))
  )

  val exampleArrivalRequestJson: JsValue = Json.toJson(exampleArrivalRequest)

  val exampleDepartureRequestXML: Node =
    scala.xml.Utility.trim {
      <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>{MessageCodes.EDL}</messageCode>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
      <goodsLocation>GBAUlocation</goodsLocation>
      <goodsDepartureDateTime>2019-07-12T13:14:54.000Z</goodsDepartureDateTime>
      <movementReference>{movementReference}</movementReference>
      <transportDetails>
        <transportID>{transportId}</transportID>
        <transportMode>{transportMode}</transportMode>
        <transportNationality>{transportNationality}</transportNationality>
      </transportDetails>
    </inventoryLinkingMovementRequest>
    }

  val exampleDepartureRequest: MovementRequest = MovementRequest(
    choice = "EDL",
    consignmentReference = ConsignmentReference("D", "7GB123456789000-123ABC456DEFQWERT"),
    movementDetails = MovementDetails("2019-07-12T13:14:54.000Z"),
    location = Some(Location("GBAUlocation")),
    arrivalReference = Some(ArrivalReference(Some(movementReference))),
    transport = Some(Transport(transportMode, transportNationality, transportId))
  )

  val exampleDepartureRequestJson: JsValue = Json.toJson(
    MovementRequest(
      choice = "EDL",
      consignmentReference = ConsignmentReference("D", "7GB123456789000-123ABC456DEFQWERT"),
      movementDetails = MovementDetails("2019-07-12T13:14:54.000Z"),
      location = Some(Location("GBAUlocation")),
      arrivalReference = Some(ArrivalReference(Some(movementReference))),
      transport = Some(Transport(transportMode, transportNationality, transportId))
    )
  )

  def exampleSubmission(
    eori: String = validEori,
    conversationId: String = conversationId,
    ucr: String = randomUcr,
    ucrType: String = "D",
    actionType: ActionType = ActionType.Arrival
  ): Submission =
    Submission(eori = eori, conversationId = conversationId, ucrBlocks = Seq(UcrBlockModel(ucr = ucr, ucrType = ucrType)), actionType = actionType)

  def emptySubmission: Submission =
    Submission(uuid = "", eori = "", conversationId = "", ucrBlocks = Seq.empty, actionType = ActionType.Arrival)

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
