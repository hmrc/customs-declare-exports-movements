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

package unit.uk.gov.hmrc.exports.movements.controllers.actions

import java.util.UUID

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.ContentTypes
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.core.errors.GenericDatabaseException
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import unit.uk.gov.hmrc.exports.movements.base.{CustomsExportsBaseSpec, UnitTestMockBuilder}
import utils.MovementsTestData._

import scala.concurrent.Future

class AuthenticatedControllerSpec extends CustomsExportsBaseSpec {
  val uri = "/movements/arrival"
  val xmlBody: String = "<iamXml></iamXml>"
  val fakeXmlRequest: FakeRequest[String] = FakeRequest("POST", uri).withBody(xmlBody)
  val fakeXmlRequestWithHeaders: FakeRequest[String] =
    fakeXmlRequest
      .withHeaders(
        CustomsHeaderNames.XUcrHeaderName -> declarantUcrValue,
        CustomsHeaderNames.XMovementTypeHeaderName -> "Arrival",
        AUTHORIZATION -> dummyToken,
        CONTENT_TYPE -> ContentTypes.XML
      )

  val jsonBody: String = Json.toJson(exampleSubmission()).toString()
  val fakeJsonRequest: FakeRequest[String] = FakeRequest("POST", uri).withBody(jsonBody)
  val fakeJsonRequestWithHeaders: FakeRequest[String] =
    fakeJsonRequest
      .withHeaders(
        CustomsHeaderNames.XLrnHeaderName -> declarantLrnValue,
        CustomsHeaderNames.XUcrHeaderName -> declarantUcrValue,
        AUTHORIZATION -> dummyToken,
        CONTENT_TYPE -> ContentTypes.JSON
      )

  "Export Controller" should {

    "return 401 status when EORI number is missing from request" in {
      userWithoutEori()

      val result = route(app, fakeXmlRequestWithHeaders).get

      status(result) must be(UNAUTHORIZED)
    }

    "return 202 status when a valid request with Enrollments is processed" in {
      withAuthorizedUser()
      withConnectorCall(CustomsInventoryLinkingResponse(ACCEPTED, Some(UUID.randomUUID().toString)))
      when(mockMovementsRepository.insert(any[Submission])(any()))
        .thenReturn(Future.successful(UnitTestMockBuilder.dummyWriteResultSuccess))

      val result = route(app, fakeXmlRequestWithHeaders).get

      status(result) must be(ACCEPTED)
    }

    "return 500 status when there is a problem with the service" in {
      withAuthorizedUser()
      withConnectorCall(CustomsInventoryLinkingResponse(ACCEPTED, Some(UUID.randomUUID().toString)))
      when(mockMovementsRepository.insert(any[Submission])(any()))
        .thenReturn(Future.failed(GenericDatabaseException("Problem with DB", None)))

      val result = route(app, fakeXmlRequestWithHeaders).get

      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    "handle InsufficientEnrolments error" in {
      unauthorizedUser(InsufficientEnrolments())

      val result = route(app, fakeXmlRequestWithHeaders).get

      status(result) must be(UNAUTHORIZED)
    }

    "handle AuthorisationException error" in {
      unauthorizedUser(InsufficientConfidenceLevel())

      val result = route(app, fakeXmlRequestWithHeaders).get

      status(result) must be(UNAUTHORIZED)
    }

    "handle rest of errors as InternalServerError" in {
      unauthorizedUser(new IllegalArgumentException())

      val result = route(app, fakeXmlRequestWithHeaders).get

      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    "handle request without headers" in {
      withAuthorizedUser()

      val result = route(app, fakeXmlRequest).get

      status(result) must be(INTERNAL_SERVER_ERROR)
    }
  }
}
