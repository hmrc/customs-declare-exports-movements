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

package component.uk.gov.hmrc.exports.movements

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.controllers.routes
import uk.gov.hmrc.exports.movements.models.notifications.{Entry, Notification, NotificationData, UcrBlock}
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, Submission}

/*
 * Component Tests are Intentionally Explicit with the JSON input, XML & DB output and DONT use TestData helpers.
 * That way these tests act as a "spec" for our API, and we dont get unintentional API changes as a result of Model/TestData refactors etc.
 */
class NotificationSpec extends ComponentSpec {

  private val submission =
    Submission(eori = "eori", providerId = Some("pid"), conversationId = "conversation-id", ucrBlocks = Seq.empty, actionType = ActionType.Arrival)
  private val notification = Notification(
    conversationId = "conversation-id",
    responseType = "response-type",
    payload = "",
    data = NotificationData(
      messageCode = Some("message-code"),
      crcCode = Some("crc-code"),
      declarationCount = Some(1),
      entries = Seq(Entry(Some(UcrBlock("UCR", "M")))),
      goodsArrivalDateTime = Some("arrival-time"),
      goodsLocation = Some("location"),
      masterRoe = Some("master-roe"),
      masterSoe = Some("master-soe"),
      masterUcr = Some("master-ucr"),
      movementReference = Some("movement-ref"),
      actionCode = Some("action-code"),
      errorCodes = Seq("error-code")
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
    "return 202" in {
      // Given
      givenAnExisting(
        Submission(eori = "eori", providerId = None, conversationId = "conversation-id", ucrBlocks = Seq.empty, actionType = ActionType.Arrival)
      )

      // When
      val payload =
        <inventoryLinkingControlResponse xmlns="http://gov.uk/customs/inventoryLinking/v1" xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1">
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
      val response = post(routes.NotificationController.saveNotification(), payload, "X-Conversation-Id" -> "conversation-id")

      // Then
      status(response) mustBe ACCEPTED

      val notifications = theNotificationsFor("conversation-id")
      notifications.size mustBe 1
      notifications.head.conversationId mustBe "conversation-id"
      notifications.head.responseType mustBe "inventoryLinkingControlResponse"
      notifications.head.data mustBe NotificationData(
        messageCode = Some("CST"),
        entries = Seq(Entry(ucrBlock = Some(UcrBlock("UCR", "M")))),
        movementReference = Some("Reference"),
        actionCode = Some("3"),
        errorCodes = Seq("22")
      )
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
