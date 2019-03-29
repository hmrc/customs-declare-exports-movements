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

package uk.gov.hmrc.exports.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.exports.base.{CustomsExportsBaseSpec, ExportsTestData}
import uk.gov.hmrc.exports.models._

class SubmissionControllerSpec extends CustomsExportsBaseSpec with ExportsTestData {
  val saveMovementUri = "/save-movement-submission"

  val jsonBody: JsValue = Json.toJson[MovementResponse](submissionMovementResponse)
  val fakeRequest: FakeRequest[JsValue] = FakeRequest("POST", saveMovementUri).withBody(jsonBody)

  val xmlBody: String = randomSubmitDeclaration.toXml

  def fakeRequestWithPayload(uri: String, payload: String): FakeRequest[String] =
    FakeRequest("POST", uri).withBody(payload)

  "Actions for submission" when {

    "POST to /save-movement-submission" should {

      "return 200 status when movement has been saved" in {
        withAuthorizedUser()
        withDataSaved(true)

        val result = route(app, fakeRequest).get

        status(result) must be(OK)
      }

      "return 500 status when something goes wrong" in {
        withAuthorizedUser()
        withDataSaved(false)

        val failedResult = route(app, fakeRequest).get

        status(failedResult) must be(INTERNAL_SERVER_ERROR)
      }
    }

    "GET from /movements/:eori" should {

      "return 200 status with movements as response body" in {
        withAuthorizedUser()
        withMovements(Seq(movement))

        val result = route(app, FakeRequest("GET", "/movements")).get

        status(result) must be(OK)
        contentAsJson(result) must be(Json.toJson(Seq(movement)))
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
