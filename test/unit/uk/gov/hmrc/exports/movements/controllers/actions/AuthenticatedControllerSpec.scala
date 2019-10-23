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
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import unit.uk.gov.hmrc.exports.movements.base.{CustomsExportsBaseSpec, UnitTestMockBuilder}
import utils.testdata.CommonTestData.dummyToken
import utils.testdata.MovementsTestData._

import scala.concurrent.Future

class AuthenticatedControllerSpec extends CustomsExportsBaseSpec {
  val uri = "/movements"

  val jsonBody = Json.toJson(exampleArrivalRequestJson)
  val fakeJsonRequest = FakeRequest("POST", uri).withJsonBody(jsonBody)
  val fakeJsonRequestWithHeaders =
    fakeJsonRequest
      .withHeaders(AUTHORIZATION -> dummyToken, CONTENT_TYPE -> ContentTypes.JSON)

  "Export Controller" should {

    "return 401 status when EORI number is missing from request" in {
      userWithoutEori()

      val result = route(app, fakeJsonRequestWithHeaders).get

      status(result) must be(UNAUTHORIZED)
    }

    "return 202 status when a valid request with Enrollments is processed" in {
      withAuthorizedUser()
      withConnectorCall(CustomsInventoryLinkingResponse(ACCEPTED, Some(UUID.randomUUID().toString)))
      when(mockSubmissionRepository.insert(any[Submission])(any()))
        .thenReturn(Future.successful(UnitTestMockBuilder.dummyWriteResultSuccess))

      val result = route(app, fakeJsonRequestWithHeaders).get

      status(result) must be(ACCEPTED)
    }

    "pass error to framework when there is a problem with the service" in {
      withAuthorizedUser()
      withConnectorCall(CustomsInventoryLinkingResponse(ACCEPTED, Some(UUID.randomUUID().toString)))
      when(mockSubmissionRepository.insert(any[Submission])(any()))
        .thenReturn(Future.failed(GenericDatabaseException("Problem with DB", None)))

      an[Exception] mustBe thrownBy {
        await(route(app, fakeJsonRequestWithHeaders).get)

      }
    }

    "handle InsufficientEnrolments error" in {
      unauthorizedUser(InsufficientEnrolments())

      val result = route(app, fakeJsonRequestWithHeaders).get

      status(result) must be(UNAUTHORIZED)
    }

    "handle AuthorisationException error" in {
      unauthorizedUser(InsufficientConfidenceLevel())

      val result = route(app, fakeJsonRequestWithHeaders).get

      status(result) must be(UNAUTHORIZED)
    }

    "handle rest of errors as InternalServerError" in {
      unauthorizedUser(new IllegalArgumentException())

      an[IllegalArgumentException] mustBe thrownBy {
        await(route(app, fakeJsonRequestWithHeaders).get)
      }
    }
  }
}
