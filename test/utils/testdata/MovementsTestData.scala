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
import play.api.libs.json.{JsObject, JsString, JsValue}
import uk.gov.hmrc.exports.movements.models.notifications.{UcrBlock => UcrBlockModel}
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, Submission}
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import utils.testdata.CommonTestData._

import scala.xml.Elem

object MovementsTestData {

  def exampleSubmission(
    eori: String = validEori,
    conversationId: String = conversationId,
    ucr: String = randomUcr,
    ucrType: String = "D",
    actionType: ActionType = ActionType.Arrival
  ): Submission =
    Submission(
      eori = eori,
      conversationId = conversationId,
      ucrBlocks = Seq(UcrBlockModel(ucr = ucr, ucrType = ucrType)),
      actionType = actionType
    )

  def emptySubmission: Submission =
    Submission(uuid = "", eori = "", conversationId = "", ucrBlocks = Seq.empty, actionType = ActionType.Arrival)

  val now: DateTime = DateTime.now.withZone(DateTimeZone.UTC)
  def validInventoryLinkingExportRequest = InventoryLinkingMovementRequest(
    messageCode = "11",
    agentDetails = Some(AgentDetails(eori = Some(validEori), agentLocation = Some("location"))),
    ucrBlock = UcrBlock(ucr = declarantUcrValue, ucrType = "type"),
    goodsLocation = "goodsLocation",
    goodsArrivalDateTime = Some(now.toString),
    goodsDepartureDateTime = Some(now.toString),
    transportDetails = Some(TransportDetails(transportID = Some("transportId"), transportMode = Some("mode")))
  )

  val exampleArrivalRequestXML: Elem =
    <inventoryLinkingMovementRequest>
      <messageCode>{MessageCodes.EAL}</messageCode>
      <agentDetails>
        <EORI>{validEori}</EORI>
        <agentLocation>{location}</agentLocation>
        <agentRole>{agentRole}</agentRole>
      </agentDetails>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
      <goodsLocation>{location}</goodsLocation>
      <goodsArrivalDateTime>2019-07-12T13:14:54.000Z</goodsArrivalDateTime>
      <shedOPID>{shedOPID}</shedOPID>
      <movementReference>{movementReference}</movementReference>
      <masterUCR>{ucr_2}</masterUCR>
      <masterOpt>{masterOpt}</masterOpt>
      <transportDetails>
        <transportID>{transportId}</transportID>
        <transportMode>{transportMode}</transportMode>
        <transportNationality>{transportNationality}</transportNationality>
      </transportDetails>
    </inventoryLinkingMovementRequest>

  val exampleArrivalRequestJson: JsValue = JsObject(
    Map(
      "inventoryLinkingMovementRequest" -> JsObject(
        Map(
          "messageCode" -> JsString(MessageCodes.EAL),
          "agentDetails" -> JsObject(
            Map(
              "EORI" -> JsString(validEori),
              "agentLocation" -> JsString(location),
              "agentRole" -> JsString(agentRole)
            )
          ),
          "ucrBlock" -> JsObject(Map("ucr" -> JsString(ucr), "ucrType" -> JsString("D"))),
          "goodsLocation" -> JsString(location),
          "goodsArrivalDateTime" -> JsString("2019-07-12T13:14:54.000Z"),
          "shedOPID" -> JsString(shedOPID),
          "movementReference" -> JsString(movementReference),
          "masterUCR" -> JsString(ucr_2),
          "masterOpt" -> JsString(masterOpt),
          "transportDetails" -> JsObject(
            Map(
              "transportID" -> JsString(transportId),
              "transportMode" -> JsString(transportMode),
              "transportNationality" -> JsString(transportNationality)
            )
          )
        )
      )
    )
  )

  val exampleDepartureRequestXML: Elem =
    <inventoryLinkingMovementRequest>
      <messageCode>{MessageCodes.EDL}</messageCode>
      <agentDetails>
        <EORI>{validEori}</EORI>
        <agentLocation>{location}</agentLocation>
        <agentRole>{agentRole}</agentRole>
      </agentDetails>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
      <goodsLocation>{location}</goodsLocation>
      <goodsDepartureDateTime>2019-07-12T13:14:54.000Z</goodsDepartureDateTime>
      <shedOPID>{shedOPID}</shedOPID>
      <movementReference>{movementReference}</movementReference>
      <masterUCR>{ucr_2}</masterUCR>
      <masterOpt>{masterOpt}</masterOpt>
      <transportDetails>
        <transportID>{transportId}</transportID>
        <transportMode>{transportMode}</transportMode>
        <transportNationality>{transportNationality}</transportNationality>
      </transportDetails>
    </inventoryLinkingMovementRequest>

  val exampleDepartureRequestJson: JsValue = JsObject(
    Map(
      "inventoryLinkingMovementRequest" -> JsObject(
        Map(
          "messageCode" -> JsString(MessageCodes.EDL),
          "agentDetails" -> JsObject(
            Map(
              "EORI" -> JsString(validEori),
              "agentLocation" -> JsString(location),
              "agentRole" -> JsString(agentRole)
            )
          ),
          "ucrBlock" -> JsObject(Map("ucr" -> JsString(ucr), "ucrType" -> JsString("D"))),
          "goodsLocation" -> JsString(location),
          "goodsDepartureDateTime" -> JsString("2019-07-12T13:14:54.000Z"),
          "shedOPID" -> JsString(shedOPID),
          "movementReference" -> JsString(movementReference),
          "masterUCR" -> JsString(ucr_2),
          "masterOpt" -> JsString(masterOpt),
          "transportDetails" -> JsObject(
            Map(
              "transportID" -> JsString(transportId),
              "transportMode" -> JsString(transportMode),
              "transportNationality" -> JsString(transportNationality)
            )
          )
        )
      )
    )
  )

}
