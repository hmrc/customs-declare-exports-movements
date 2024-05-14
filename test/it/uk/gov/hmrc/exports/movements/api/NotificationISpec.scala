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

package uk.gov.hmrc.exports.movements.api

import play.api.libs.json.{Format, Json}
import play.api.test.Helpers._
import testdata.notifications.ExampleXmlAndDomainModelPair.ExampleStandardResponse
import uk.gov.hmrc.exports.movements.base.ApiSpec
import uk.gov.hmrc.exports.movements.controllers.routes.NotificationController
import uk.gov.hmrc.exports.movements.models.common.UcrType.Mucr
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.exports.movements.models.notifications.standard.{Entry, StandardNotificationData, UcrBlock}
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.MovementType
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.xml.Elem

/*
 * API Tests are Intentionally Explicit with the JSON input, XML & DB output and DONT use TestData helpers.
 * That way these tests act as a "spec" for our API, and we dont get unintentional API changes as a result of Model/TestData refactors etc.
 */
class NotificationISpec extends ApiSpec {

  val conversationId = "conversation-id"

  private val submission =
    Submission(eori = "eori", providerId = Some("pid"), conversationId = conversationId, ucrBlocks = Seq.empty, actionType = MovementType.Arrival)

  private val notification = Notification(
    conversationId = conversationId,
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

  implicit val formatInstant: Format[Instant] = MongoJavatimeFormats.instantFormat

  private val notificationJson = Json.obj(
    "timestampReceived" -> currentInstant,
    "conversationId" -> conversationId,
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

  "GET /notifications" should {
    "return 200" when {
      "No filters" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val result = get(NotificationController.getAllNotificationsForUser(eori = None, providerId = None))

        // Then
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.arr()
      }

      "Eori" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val result = get(NotificationController.getAllNotificationsForUser(eori = Some("eori"), providerId = None))

        // Then
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.arr(notificationJson)
      }

      "Provider ID" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val result = get(NotificationController.getAllNotificationsForUser(eori = None, providerId = Some("pid")))

        // Then
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.arr(notificationJson)
      }
    }
  }

  "GET /notifications/:conversationId" should {
    "return 200" when {
      "No filters" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val call = NotificationController.getNotificationsForSubmission(eori = None, providerId = None, conversationId = conversationId)
        val result = get(call)

        // Then
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.arr(notificationJson)
      }

      "Eori" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val call = NotificationController.getNotificationsForSubmission(eori = Some("eori"), providerId = None, conversationId = conversationId)
        val result = get(call)

        // Then
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.arr(notificationJson)
      }

      "Provider ID" in {
        // Given
        givenAnExisting(submission)
        givenAnExisting(notification)

        // When
        val call = NotificationController.getNotificationsForSubmission(eori = None, providerId = Some("pid"), conversationId = conversationId)
        val result = get(call)

        // Then
        status(result) mustBe OK
        val actual = contentAsJson(result)
        val expected = Json.arr(notificationJson)
        actual mustBe expected
      }
    }
  }

  "POST /notifyMovement" should {

    val payloadUnit =
      """
        |<messageCode>{MessageCodes.CST}</messageCode>
        |<actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
        |<ucr>
        |  <ucr>{ucr}</ucr>
        |  <ucrType>M</ucrType>
        |</ucr>
        |<movementReference>{movementReference}</movementReference>
        |""".stripMargin

    def genPayload(units: Int): Elem = ExampleStandardResponse(<inventoryLinkingControlResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
        {payloadUnit * units}
      </inventoryLinkingControlResponse>).asXml

    "return ACCEPTED (202) status on payload with size within the Play default (100KB)" in {
      val result = post(NotificationController.saveNotification(), genPayload(1))
      status(result) mustBe ACCEPTED
    }

    "return REQUEST_ENTITY_TOO_LARGE (413) status on payload with size over the Play default (100KB)" in {
      val result = post(NotificationController.saveNotification(), genPayload(1000))
      status(result) mustBe REQUEST_ENTITY_TOO_LARGE
    }
  }
}
