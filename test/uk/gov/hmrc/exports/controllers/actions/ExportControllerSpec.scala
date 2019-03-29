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

package uk.gov.hmrc.exports.controllers.actions

import org.scalatest.concurrent.ScalaFutures
import play.api.http.ContentTypes
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.exports.base.{CustomsExportsBaseSpec, ExportsTestData}
import uk.gov.hmrc.exports.controllers.CustomsHeaderNames

class ExportControllerSpec extends CustomsExportsBaseSpec with ExportsTestData with ScalaFutures {
  val uri = "/save-movement-submission"
  val jsonBody: String = Json.toJson(movement).toString()
  val fakeJsonRequest: FakeRequest[String] = FakeRequest("POST", uri).withBody(jsonBody)
  val fakeRequestWithHeaders: FakeRequest[String] =
    fakeJsonRequest
      .withHeaders(
        CustomsHeaderNames.XLrnHeaderName -> declarantLrnValue,
        CustomsHeaderNames.XDucrHeaderName -> declarantDucrValue,
        AUTHORIZATION -> dummyToken,
        CONTENT_TYPE -> ContentTypes.JSON
      )

  "Export Controller" should {

    "return 401 status when EORI number is missing from request" in {
      userWithoutEori()

      val result = route(app, fakeRequestWithHeaders).get

      status(result) must be(UNAUTHORIZED)
    }

    "return 200 status when a valid request with Enrollments is processed" in {
      withAuthorizedUser()
      withDataSaved(true)

      val result = route(app, fakeRequestWithHeaders).get

      status(result) must be(OK)
    }

    "return 500 status when there is a problem with the service" in {
      withAuthorizedUser()
      withDataSaved(false)

      val result = route(app, fakeRequestWithHeaders).get

      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    "handle InsufficientEnrolments error" in {
      unauthorizedUser(InsufficientEnrolments())

      val result = route(app, fakeRequestWithHeaders).get

      status(result) must be(UNAUTHORIZED)
    }

    "handle AuthorisationException error" in {
      unauthorizedUser(InsufficientConfidenceLevel())

      val result = route(app, fakeRequestWithHeaders).get

      status(result) must be(UNAUTHORIZED)
    }

    "handle rest of errors as InternalServerError" in {
      unauthorizedUser(new IllegalArgumentException())

      val result = route(app, fakeRequestWithHeaders).get

      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    "handle request without headers" in {
      unauthorizedUser(new IllegalArgumentException())

      val result = route(app, fakeJsonRequest).get

      status(result) must be(UNSUPPORTED_MEDIA_TYPE)
    }
  }
}
