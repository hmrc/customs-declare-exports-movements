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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.verify
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.controllers.routes
import uk.gov.hmrc.exports.movements.models.common.UcrType.Ducr
import uk.gov.hmrc.exports.movements.models.movements.Transport
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.exports.movements.models.notifications.queries._
import uk.gov.hmrc.exports.movements.models.notifications.standard.{EntryStatus, UcrBlock}
import uk.gov.hmrc.exports.movements.models.submissions.IleQuerySubmission

/*
 * Component Tests are Intentionally Explicit with the JSON input, XML & DB output and DONT use TestData helpers.
 * That way these tests act as a "spec" for our API, and we dont get unintentional API changes as a result of Model/TestData refactors etc.
 */
class IleQuerySpec extends ComponentSpec {

  private val ileQuerySubmission = IleQuerySubmission(
    eori = "eori",
    providerId = Some("provider-id"),
    conversationId = "conversation-id",
    ucrBlock = UcrBlock(ucr = "UCR-123", ucrType = "D")
  )

  private val ileQueryResponse = Notification(
    conversationId = "conversation-id",
    responseType = "response-type",
    payload = "",
    timestampReceived = currentInstant,
    data = IleQueryResponseData(
      queriedDucr = Some(
        DucrInfo(
          ucr = "UCR-123",
          parentMucr = Some("parent-mucr"),
          declarationId = "declaration-id",
          entryStatus = Some(EntryStatus(ics = Some("3"), roe = Some("6"), soe = Some("14"))),
          goodsItem = Seq(GoodsItemInfo(totalPackages = Some(13))),
          movements = Seq(
            MovementInfo(
              messageCode = "message-code",
              goodsLocation = "goods-location",
              movementDateTime = Some(Instant.parse("2019-12-23T11:40:00.000Z")),
              movementReference = Some("movement-reference"),
              transportDetails =
                Some(Transport(modeOfTransport = Some("mode"), nationality = Some("nationality"), transportId = Some("transport-id")))
            )
          )
        )
      ),
      parentMucr = Some(
        MucrInfo(
          ucr = "parent-mucr",
          entryStatus = Some(EntryStatus(ics = Some("3"), roe = Some("H"), soe = Some("17"))),
          isShut = Some(true),
          movements = Seq(
            MovementInfo(
              messageCode = "message-code-mucr",
              goodsLocation = "goods-location-mucr",
              movementDateTime = Some(Instant.parse("2019-12-23T12:30:00.000Z")),
              movementReference = Some("movement-reference-mucr"),
              transportDetails =
                Some(Transport(modeOfTransport = Some("mode-mucr"), nationality = Some("nationality-mucr"), transportId = Some("transport-id-mucr")))
            )
          )
        )
      )
    )
  )

  private val ileQueryResponseJson = Json.obj(
    "timestampReceived" -> currentInstant,
    "conversationId" -> "conversation-id",
    "responseType" -> "response-type",
    "data" -> Json.obj(
      "queriedDucr" -> Json.obj(
        "ucr" -> "UCR-123",
        "parentMucr" -> "parent-mucr",
        "declarationId" -> "declaration-id",
        "entryStatus" -> Json.obj("ics" -> "3", "roe" -> "6", "soe" -> "14"),
        "movements" -> Json.arr(
          Json.obj(
            "messageCode" -> "message-code",
            "goodsLocation" -> "goods-location",
            "movementDateTime" -> "2019-12-23T11:40:00Z",
            "movementReference" -> "movement-reference",
            "transportDetails" -> Json.obj("modeOfTransport" -> "mode", "nationality" -> "nationality", "transportId" -> "transport-id")
          )
        ),
        "goodsItem" -> Json.arr(Json.obj("totalPackages" -> 13))
      ),
      "parentMucr" -> Json.obj(
        "ucr" -> "parent-mucr",
        "entryStatus" -> Json.obj("ics" -> "3", "roe" -> "H", "soe" -> "17"),
        "isShut" -> true,
        "movements" -> Json.arr(
          Json.obj(
            "messageCode" -> "message-code-mucr",
            "goodsLocation" -> "goods-location-mucr",
            "movementDateTime" -> "2019-12-23T12:30:00Z",
            "movementReference" -> "movement-reference-mucr",
            "transportDetails" -> Json
              .obj("modeOfTransport" -> "mode-mucr", "nationality" -> "nationality-mucr", "transportId" -> "transport-id-mucr")
          )
        )
      ),
      "childMucrs" -> Json.arr(),
      "typ" -> "SuccessfulResponseExchange",
      "childDucrs" -> Json.arr()
    )
  )

  "POST" should {
    "return 201" in {

      // Given
      givenIleApiAcceptsTheSubmission("conversation-id")

      // When
      val request = post(
        routes.IleQueryController.submitIleQuery(),
        Json.obj("eori" -> "eori", "providerId" -> "provider-id", "ucrBlock" -> Json.obj("ucr" -> "UCR-123", "ucrType" -> "D"))
      )

      // Then
      status(request) mustBe ACCEPTED

      val submissions: Seq[IleQuerySubmission] = theIleQuerySubmissionsFor("eori")
      submissions.size mustBe 1
      submissions.head.providerId mustBe Some("provider-id")
      submissions.head.conversationId mustBe "conversation-id"
      submissions.head.ucrBlock mustBe UcrBlock(ucr = "UCR-123", ucrType = Ducr.codeValue)

      verify(
        postRequestedToILE()
          .withRequestBody(WireMock.equalToXml("""
            |<inventoryLinkingQueryRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
            |      <queryUCR>
            |        <ucr>UCR-123</ucr>
            |        <ucrType>D</ucrType>
            |      </queryUCR>
            |    </inventoryLinkingQueryRequest>
            |""".stripMargin, true))
      )
    }
  }

  "GET /conversationId" should {
    "return 200" when {

      "only Conversation ID is present" in {
        // Given
        givenAnExisting(ileQuerySubmission)
        givenAnExisting(ileQueryResponse)

        // When
        val response = get(routes.IleQueryController.getIleQueryResponses(eori = None, providerId = None, conversationId = "conversation-id"))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(ileQueryResponseJson)
      }

      "Conversation ID and EORI are present" in {
        // Given
        givenAnExisting(ileQuerySubmission)
        givenAnExisting(ileQueryResponse)

        // When
        val response = get(routes.IleQueryController.getIleQueryResponses(eori = Some("eori"), providerId = None, conversationId = "conversation-id"))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(ileQueryResponseJson)
      }

      "Conversation ID and Provider ID are present" in {
        // Given
        givenAnExisting(ileQuerySubmission)
        givenAnExisting(ileQueryResponse)

        // When
        val response =
          get(routes.IleQueryController.getIleQueryResponses(eori = None, providerId = Some("provider-id"), conversationId = "conversation-id"))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(ileQueryResponseJson)
      }
    }
  }

}
