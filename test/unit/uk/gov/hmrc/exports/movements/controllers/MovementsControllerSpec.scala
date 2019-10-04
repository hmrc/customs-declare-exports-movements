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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames.XEoriIdentifierHeaderName
import uk.gov.hmrc.exports.movements.models.submissions.ActionType
import uk.gov.hmrc.exports.movements.services.SubmissionService
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import unit.uk.gov.hmrc.exports.movements.base.AuthTestSupport
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder.buildSubmissionServiceMock
import utils.testdata.CommonTestData.ValidJsonHeaders
import utils.testdata.MovementsTestData._

import scala.concurrent.Future

class MovementsControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with AuthTestSupport with BeforeAndAfterEach with ScalaFutures
    with MustMatchers {

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[AuthConnector].to(mockAuthConnector), bind[SubmissionService].to(submissionServiceMock))
    .build()

  private val movementsUri = "/movements"

  private val submissionServiceMock = buildSubmissionServiceMock

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, submissionServiceMock)
  }

  private def routePost(headers: Map[String, String] = ValidJsonHeaders, body: JsValue, uri: String): Future[Result] =
    route(app, FakeRequest(POST, uri).withHeaders(headers.toSeq: _*).withJsonBody(body)).get

  "SubmissionController on submitArrival" when {

    "everything works correctly" should {

      "return Accepted status" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful((): Unit))

        val result = routePost(body = exampleArrivalRequestJson, uri = movementsUri)

        status(result) must be(ACCEPTED)
      }

      "call SubmissionService, passing correctly built RequestContext" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful((): Unit))

        routePost(body = exampleArrivalRequestJson, uri = movementsUri).futureValue

        val expectedEori = ValidJsonHeaders(XEoriIdentifierHeaderName)
        val contextCaptor: ArgumentCaptor[SubmissionRequestContext] =
          ArgumentCaptor.forClass(classOf[SubmissionRequestContext])

        verify(submissionServiceMock).submitRequest(contextCaptor.capture())(any())

        contextCaptor.getValue.eori must equal(expectedEori)
        contextCaptor.getValue.actionType must equal(ActionType.Arrival)
        contextCaptor.getValue.requestXml must equal(exampleArrivalRequestXML)
      }
    }

    "SubmissionService returns failure" should {

      "return InternalServerError" in {
        withAuthorizedUser()

        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.failed(new Exception("")))

        val response = routePost(body = exampleArrivalRequestJson, uri = movementsUri)

        an[Exception] mustBe thrownBy {
          await(response)
        }
      }
    }
  }

  "SubmissionController on submitDeparture" when {

    "everything works correctly" should {

      "return Accepted status" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful((): Unit))

        val result = routePost(body = exampleDepartureRequestJson, uri = movementsUri)

        status(result) must be(ACCEPTED)
      }

      "call SubmissionService, passing correctly built RequestContext" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful((): Unit))

        routePost(body = exampleDepartureRequestJson, uri = movementsUri).futureValue

        val expectedEori = ValidJsonHeaders(XEoriIdentifierHeaderName)
        val contextCaptor: ArgumentCaptor[SubmissionRequestContext] =
          ArgumentCaptor.forClass(classOf[SubmissionRequestContext])

        verify(submissionServiceMock).submitRequest(contextCaptor.capture())(any())

        contextCaptor.getValue.eori must equal(expectedEori)
        contextCaptor.getValue.actionType must equal(ActionType.Departure)
        contextCaptor.getValue.requestXml must equal(exampleDepartureRequestXML)
      }
    }

    "SubmissionService returns failure" should {

      "return InternalServerError" in {
        withAuthorizedUser()

        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.failed(new Exception("")))

        val result = routePost(body = exampleDepartureRequestJson, uri = movementsUri)

        an[Exception] shouldBe thrownBy {
          await(result)
        }
      }
    }
  }
}
