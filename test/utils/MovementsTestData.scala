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

package utils

import org.joda.time.{DateTime, DateTimeZone}
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.Codec
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames._
import uk.gov.hmrc.exports.movements.models.notifications.{UcrBlock => UcrBlockModel}
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import uk.gov.hmrc.exports.movements.models.submissions.Submission.ActionTypes
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import uk.gov.hmrc.wco.dec.{DateTimeString, MetaData, ResponseDateTimeElement, Declaration => WcoDeclaration}

import scala.util.Random
import scala.xml.Elem

object MovementsTestData {

  private lazy val responseFunctionCodes: Seq[String] =
    Seq("01", "02", "03", "05", "06", "07", "08", "09", "10", "11", "16", "17", "18")
  private def randomResponseFunctionCode: String = responseFunctionCodes(Random.nextInt(responseFunctionCodes.length))
  object MessageCodes {
    val EAA = "EAA"
    val EAL = "EAL"
    val EDL = "EDL"
    val EAC = "EAC"
    val CST = "CST"
    val ERS = "ERS"
    val EMR = "EMR"
  }

  val validEori: String = "GB167676"
  val conversationId: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e11"
  val conversationId_2: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e22"
  val conversationId_3: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e33"
  val conversationId_4: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e44"
  val conversationId_5: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e55"
  val ucr = "9GB025115188654-IAZ1"
  val ucr_2 = "7GB123456789000-123ABC456DEFQWERT"
  val randomUcr: String = randomString(16)

  val location = "LOCATION"
  val agentRole = "ARL"
  val shedOPID = "SOP"
  val movementReference = "MovRef001234"
  private val masterOptCodes = Seq("A", "F", "R", "X")
  val masterOpt = masterOptCodes.head
  val transportId = "TransportID"
  val transportMode = "X"
  val transportNationality = "UK"

  val authToken: String =
    "BXQ3/Treo4kQCZvVcCqKPlwxRN4RA9Mb5RF8fFxOuwG5WSg+S+Rsp9Nq998Fgg0HeNLXL7NGwEAIzwM6vuA6YYhRQnTRFaBhrp+1w+kVW8g1qHGLYO48QPWuxdM87VMCZqxnCuDoNxVn76vwfgtpNj0+NwfzXV2Zc12L2QGgF9H9KwIkeIPK/mMlBESjue4V]"
  val dummyToken: String = s"Bearer $authToken"
  val declarantLrnValue: String = "MyLrnValue1234"
  val declarantUcrValue: String = "MyDucrValue1234"
  val declarantMrnValue: String = "MyMucrValue1234"
  val ContentTypeHeader: (String, String) = CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
  val ValidXEoriIdentifierHeader: (String, String) = XEoriIdentifierHeaderName -> validEori
  val ValidLrnHeader: (String, String) = XLrnHeaderName -> declarantLrnValue
  val ValidAuthorizationHeader: (String, String) = HeaderNames.AUTHORIZATION -> dummyToken
  val ValidConversationIdHeader: (String, String) = XConversationIdName -> conversationId
  val ValidUcrHeader: (String, String) = XUcrHeaderName -> declarantUcrValue
  val ValidMovementTypeHeader: (String, String) = XMovementTypeHeaderName -> "Arrival"

  val ValidHeaders: Map[String, String] = Map(
    ContentTypeHeader,
    ValidAuthorizationHeader,
    ValidConversationIdHeader,
    ValidXEoriIdentifierHeader,
    // TODO: This is not needed
    ValidLrnHeader,
    ValidUcrHeader,
    ValidMovementTypeHeader
  )

  def exampleSubmission(
    eori: String = validEori,
    conversationId: String = conversationId,
    ucr: String = randomUcr,
    ucrType: String = "D",
    actionType: String = ActionTypes.Arrival
  ): Submission =
    Submission(
      eori = eori,
      conversationId = conversationId,
      ucrBlocks = Seq(UcrBlockModel(ucr = ucr, ucrType = ucrType)),
      actionType = actionType
    )

  def dateTimeElement(dateTimeVal: DateTime) =
    Some(ResponseDateTimeElement(DateTimeString("102", dateTimeVal.toString("yyyyMMdd"))))

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

  def randomSubmitDeclaration: MetaData =
    MetaData(declaration = Option(WcoDeclaration(functionalReferenceId = Some(randomString(35)))))

  protected def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  def exampleArrivalRequestXML: Elem =
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

  def exampleDepartureRequestXML: Elem =
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
