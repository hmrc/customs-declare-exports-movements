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

package component.uk.gov.hmrc.exports.movements

import java.time.Instant

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.controllers.routes
import uk.gov.hmrc.exports.movements.models.common.UcrType.Mucr
import uk.gov.hmrc.exports.movements.models.movements.Transport
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.exports.movements.models.notifications.queries.{DucrInfo, GoodsItemInfo, IleQueryResponseData, MovementInfo}
import uk.gov.hmrc.exports.movements.models.notifications.standard.{Entry, EntryStatus, StandardNotificationData, UcrBlock}
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.MovementType
import uk.gov.hmrc.exports.movements.models.submissions.Submission

/*
 * Component Tests are Intentionally Explicit with the JSON input, XML & DB output and DONT use TestData helpers.
 * That way these tests act as a "spec" for our API, and we dont get unintentional API changes as a result of Model/TestData refactors etc.
 */
class NotificationSpec extends ComponentSpec {

  private val submission =
    Submission(eori = "eori", providerId = Some("pid"), conversationId = "conversation-id", ucrBlocks = Seq.empty, actionType = MovementType.Arrival)
  private val notification = Notification(
    conversationId = "conversation-id",
    payload = "",
    data = Some(
      StandardNotificationData(
        messageCode = Some("message-code"),
        crcCode = Some("crc-code"),
        declarationCount = Some(1),
        entries = Seq(Entry(Some(UcrBlock(ucr = "UCR", ucrType = Mucr.codeValue)))),
        goodsArrivalDateTime = Some(Instant.parse("2020-03-01T12:45:00.000Z")),
        goodsLocation = Some("location"),
        masterRoe = Some("master-roe"),
        masterSoe = Some("master-soe"),
        masterUcr = Some("master-ucr"),
        movementReference = Some("movement-ref"),
        actionCode = Some("action-code"),
        errorCodes = Seq("error-code"),
        responseType = "response-type"
      )
    ),
    timestampReceived = currentInstant
  )
  private val notificationJson = Json.obj(
    "timestampReceived" -> currentInstant,
    "conversationId" -> "conversation-id",
    "responseType" -> "response-type",
    "entries" -> Json.arr(
      Json.obj(
        "ucrBlock" -> Json.obj("ucr" -> "master-ucr", "ucrType" -> "M"),
        "goodsItem" -> Json.arr(),
        "entryStatus" -> Json.obj("roe" -> "master-roe", "soe" -> "master-soe")
      ),
      Json.obj("ucrBlock" -> Json.obj("ucr" -> "UCR", "ucrType" -> "M"), "goodsItem" -> Json.arr())
    ),
    "crcCode" -> "crc-code",
    "actionCode" -> "action-code",
    "errorCodes" -> Json.arr("error-code"),
    "messageCode" -> "message-code"
  )

  "POST" should {
    "return 202" when {

      "payload contains ControlResponse" in {
        // Given
        val payload =
          <inventoryLinkingControlResponse
          xmlns="http://gov.uk/customs/inventoryLinking/v1"
          xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1">
            <messageCode>CST</messageCode>
            <actionCode>3</actionCode>
            <ucr>
              <ucr>UCR</ucr>
              <ucrType>M</ucrType>
            </ucr>
            <movementReference>Reference</movementReference>
            <error>
              <errorCode>22</errorCode>
            </error>
            <error>
              <errorCode>Description</errorCode>
            </error>
          </inventoryLinkingControlResponse>

        // When
        val response = post(routes.NotificationController.saveNotification(), payload, "X-Conversation-Id" -> "conversation-id")

        // Then
        status(response) mustBe ACCEPTED

        val notifications = theNotificationsFor("conversation-id")
        notifications.size mustBe 1
        notifications.head.conversationId mustBe "conversation-id"
        notifications.head.data mustBe Some(
          StandardNotificationData(
            messageCode = Some("CST"),
            entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = "UCR", ucrType = Mucr.codeValue)))),
            movementReference = Some("Reference"),
            actionCode = Some("3"),
            errorCodes = Seq("22"),
            responseType = "inventoryLinkingControlResponse"
          )
        )
      }

      "payload contains QueryResponse" in {
        // Given
        val payload =
          <inventoryLinkingQueryResponse
          xmlns="http://gov.uk/customs/inventoryLinking/v1"
          xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1">
            <queriedDUCR>
              <UCR>UCR</UCR>
              <declarationID>DeclarationID</declarationID>
              <entryStatus>
                <ics>1</ics>
                <roe>2</roe>
                <soe>3</soe>
              </entryStatus>
              <movement>
                <messageCode>EDL</messageCode>
                <goodsLocation>GoodsLocation</goodsLocation>
                <goodsDepartureDateTime>2019-12-23T11:40:00.000Z</goodsDepartureDateTime>
                <movementReference>MovementReference</movementReference>
                <transportDetails>
                  <transportID>TransportID</transportID>
                  <transportMode>1</transportMode>
                  <transportNationality>GB</transportNationality>
                </transportDetails>
              </movement>
              <goodsItem>
                <totalPackages>10</totalPackages>
              </goodsItem>
            </queriedDUCR>
          </inventoryLinkingQueryResponse>

        // When
        val response = post(routes.NotificationController.saveNotification(), payload, "X-Conversation-Id" -> "conversation-id")

        // Then
        status(response) mustBe ACCEPTED

        val notifications = theNotificationsFor("conversation-id")
        notifications.size mustBe 1
        notifications.head.conversationId mustBe "conversation-id"
        notifications.head.data mustBe IleQueryResponseData(
          queriedDucr = Some(
            DucrInfo(
              ucr = "UCR",
              declarationId = "DeclarationID",
              entryStatus = Some(EntryStatus(ics = Some("1"), roe = Some("2"), soe = Some("3"))),
              movements = Seq(
                MovementInfo(
                  messageCode = "EDL",
                  goodsLocation = "GoodsLocation",
                  movementDateTime = Some(Instant.parse("2019-12-23T11:40:00.000Z")),
                  movementReference = Some("MovementReference"),
                  transportDetails = Some(Transport(transportId = Some("TransportID"), modeOfTransport = Some("1"), nationality = Some("GB")))
                )
              ),
              goodsItem = Seq(GoodsItemInfo(totalPackages = Some(10)))
            )
          ),
          responseType = "inventoryLinkingControlResponse"
        )
      }
    }
  }

  "GET" should {
    "return 200" when {
      "No filters" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val response = get(routes.NotificationController.getAllNotificationsForUser(eori = None, providerId = None))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr()
      }

      "Eori" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val response = get(routes.NotificationController.getAllNotificationsForUser(eori = Some("eori"), providerId = None))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(notificationJson)
      }

      "Provider ID" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val response = get(routes.NotificationController.getAllNotificationsForUser(eori = None, providerId = Some("pid")))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(notificationJson)
      }
    }
  }

  "GET /id" should {
    "return 200" when {
      "No filters" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val response =
          get(routes.NotificationController.getNotificationsForSubmission(eori = None, providerId = None, conversationId = "conversation-id"))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(notificationJson)
      }

      "Eori" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val response =
          get(routes.NotificationController.getNotificationsForSubmission(eori = Some("eori"), providerId = None, conversationId = "conversation-id"))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(notificationJson)
      }

      "Provider ID" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val response =
          get(routes.NotificationController.getNotificationsForSubmission(eori = None, providerId = Some("pid"), conversationId = "conversation-id"))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(notificationJson)
      }
    }
  }

}
