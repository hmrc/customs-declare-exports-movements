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

package unit.uk.gov.hmrc.exports.movements.controllers

import java.util.UUID

import play.api.http.ContentTypes
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.MovementSubmissions._
import unit.uk.gov.hmrc.exports.movements.base.CustomsExportsBaseSpec
import utils.MovementsTestData

class MovementsSubmissionControllerSpec extends CustomsExportsBaseSpec with MovementsTestData {
  val saveMovementUri = "/save-movement-submission"

  val xmlBody: String = randomSubmitDeclaration.toXml

  val fakeXmlRequest: FakeRequest[String] = FakeRequest("POST", saveMovementUri).withBody(xmlBody)
  val fakeXmlRequestWithHeaders: FakeRequest[String] =
    fakeXmlRequest
      .withHeaders(
        CustomsHeaderNames.XUcrHeaderName -> declarantUcrValue,
        CustomsHeaderNames.XMovementTypeHeaderName -> "Arrival",
        AUTHORIZATION -> dummyToken,
        CONTENT_TYPE -> ContentTypes.XML
      )

  def fakeRequestWithPayload(uri: String, payload: String): FakeRequest[String] =
    FakeRequest("POST", uri).withBody(payload)

  "Actions for submission" when {

    "POST to /save-movement-submission" should {

      "return 202 status when movement has been saved" in {
        withAuthorizedUser()
        withDataSaved(true)
        withConnectorCall(CustomsInventoryLinkingResponse(ACCEPTED, Some(UUID.randomUUID().toString)))

        val result = route(app, fakeXmlRequestWithHeaders).get

        status(result) must be(ACCEPTED)
      }

      "return 400 status when non XML is received" in {
        withAuthorizedUser()

        val result = route(app, fakeXmlRequestWithHeaders.withBody("{json}")).get

        status(result) must be(BAD_REQUEST)
      }

      "return 400 status when non XML contentType is received" in {
        withAuthorizedUser()

        val result = route(
          app,
          fakeXmlRequest
            .withHeaders(AUTHORIZATION -> dummyToken, CONTENT_TYPE -> ContentTypes.JSON)
        ).get

        status(result) must be(BAD_REQUEST)
      }

      "return 500 status when something goes wrong" in {
        withAuthorizedUser()
        withConnectorCall(CustomsInventoryLinkingResponse(BAD_REQUEST, None))

        val failedResult = route(app, fakeXmlRequestWithHeaders).get

        status(failedResult) must be(INTERNAL_SERVER_ERROR)
      }
    }

    "GET from /movements/:eori" should {

      "return 200 status with movements as response body" in {
        val submission = movementSubmission()

        withAuthorizedUser()
        withMovements(Seq(submission))

        val result = route(app, FakeRequest("GET", "/movements")).get

        status(result) must be(OK)
        contentAsJson(result) must be(Json.toJson(Seq(submission)))
      }

      // TODO: 204 is safe response
      "return 200 status without empty response" in {
        withAuthorizedUser()
        withMovements(Seq.empty)

        val result = route(app, FakeRequest("GET", "/movements")).get

        status(result) must be(OK)
      }
    }
  }
}
