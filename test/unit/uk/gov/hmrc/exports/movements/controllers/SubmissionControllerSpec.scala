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
import org.mockito.Mockito.{reset, verify, verifyZeroInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames.XEoriIdentifierHeaderName
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.metrics.MovementsMetrics
import uk.gov.hmrc.exports.movements.models.submissions.Submission.ActionTypes
import uk.gov.hmrc.exports.movements.services.SubmissionService
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import unit.uk.gov.hmrc.exports.movements.base.AuthTestSupport
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder.buildSubmissionServiceMock
import utils.CommonTestData.ValidHeaders
import utils.MovementsTestData._

import scala.concurrent.Future
import scala.xml.Elem

class SubmissionControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with AuthTestSupport with BeforeAndAfterEach with ScalaFutures
    with MustMatchers {

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[AuthConnector].to(mockAuthConnector), bind[SubmissionService].to(submissionServiceMock))
    .build()

  private val arrivalUri = "/movements/arrival"
  private val departureUri = "/movements/departure"
  private val getAllSubmissionsUri = "/movements"

  private val submissionServiceMock = buildSubmissionServiceMock
  private val metrics: MovementsMetrics = app.injector.instanceOf[MovementsMetrics]
  private val headerValidator: HeaderValidator = app.injector.instanceOf[HeaderValidator]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, submissionServiceMock)
  }

  private def routePost(headers: Map[String, String] = ValidHeaders, xmlBody: Elem, uri: String): Future[Result] =
    route(app, FakeRequest(POST, uri).withHeaders(headers.toSeq: _*).withXmlBody(xmlBody)).get

  private def routeGet(headers: Map[String, String] = ValidHeaders, uri: String): Future[Result] =
    route(app, FakeRequest(GET, uri).withHeaders(headers.toSeq: _*)).get

  "SubmissionController on submitArrival" when {

    "everything works correctly" should {

      "return Accepted status" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful(Right((): Unit)))

        val result = routePost(xmlBody = exampleArrivalRequestXML, uri = arrivalUri)

        status(result) must be(ACCEPTED)
      }

      "call SubmissionService, passing correctly built RequestContext" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful(Right((): Unit)))

        routePost(xmlBody = exampleArrivalRequestXML, uri = arrivalUri).futureValue

        val expectedEori = ValidHeaders(XEoriIdentifierHeaderName)
        val contextCaptor: ArgumentCaptor[SubmissionRequestContext] =
          ArgumentCaptor.forClass(classOf[SubmissionRequestContext])

        verify(submissionServiceMock).submitRequest(contextCaptor.capture())(any())

        contextCaptor.getValue.eori must equal(expectedEori)
        contextCaptor.getValue.actionType must equal(ActionTypes.Arrival)
        contextCaptor.getValue.requestXml must equal(exampleArrivalRequestXML)
      }
    }

    "SubmissionService returns Either.Left" should {

      "return InternalServerError" in {
        withAuthorizedUser()

        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful(Left("")))

        val result = routePost(xmlBody = exampleArrivalRequestXML, uri = arrivalUri)

        status(result) must be(INTERNAL_SERVER_ERROR)
      }
    }

    "provided with invalid request format" should {

      "return ErrorResponse for invalid payload" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful(Right((): Unit)))

        val result = route(
          app,
          FakeRequest(POST, arrivalUri)
            .withHeaders(ValidHeaders.toSeq: _*)
            .withJsonBody(exampleArrivalRequestJson)
        ).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include("Invalid payload")
      }

      "not call SubmissionService" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful(Right((): Unit)))

        route(
          app,
          FakeRequest(POST, arrivalUri)
            .withHeaders(ValidHeaders.toSeq: _*)
            .withJsonBody(exampleArrivalRequestJson)
        ).get.futureValue

        verifyZeroInteractions(submissionServiceMock)
      }
    }
  }

  "SubmissionController on submitDeparture" when {

    "everything works correctly" should {

      "return Accepted status" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful(Right((): Unit)))

        val result = routePost(xmlBody = exampleDepartureRequestXML, uri = departureUri)

        status(result) must be(ACCEPTED)
      }

      "call SubmissionService, passing correctly built RequestContext" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful(Right((): Unit)))

        routePost(xmlBody = exampleDepartureRequestXML, uri = departureUri).futureValue

        val expectedEori = ValidHeaders(XEoriIdentifierHeaderName)
        val contextCaptor: ArgumentCaptor[SubmissionRequestContext] =
          ArgumentCaptor.forClass(classOf[SubmissionRequestContext])

        verify(submissionServiceMock).submitRequest(contextCaptor.capture())(any())

        contextCaptor.getValue.eori must equal(expectedEori)
        contextCaptor.getValue.actionType must equal(ActionTypes.Departure)
        contextCaptor.getValue.requestXml must equal(exampleDepartureRequestXML)
      }
    }

    "SubmissionService returns Either.Left" should {

      "return InternalServerError" in {
        withAuthorizedUser()

        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful(Left("")))

        val result = routePost(xmlBody = exampleDepartureRequestXML, uri = departureUri)

        status(result) must be(INTERNAL_SERVER_ERROR)
      }
    }

    "provided with invalid request format" should {

      "return ErrorResponse for invalid payload" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful(Right((): Unit)))

        val result = route(
          app,
          FakeRequest(POST, departureUri)
            .withHeaders(ValidHeaders.toSeq: _*)
            .withJsonBody(exampleDepartureRequestJson)
        ).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include("Invalid payload")
      }

      "not call SubmissionService" in {
        withAuthorizedUser()
        when(submissionServiceMock.submitRequest(any())(any()))
          .thenReturn(Future.successful(Right((): Unit)))

        route(
          app,
          FakeRequest(POST, departureUri)
            .withHeaders(ValidHeaders.toSeq: _*)
            .withJsonBody(exampleDepartureRequestJson)
        ).get.futureValue

        verifyZeroInteractions(submissionServiceMock)
      }
    }
  }

  "SubmissionController on getAllSubmissions" should {

    "return Ok status" in {
      withAuthorizedUser()
      when(submissionServiceMock.getSubmissionsByEori(any[String]))
        .thenReturn(Future.successful(Seq(exampleSubmission())))

      val result = routeGet(uri = getAllSubmissionsUri)

      status(result) must be(OK)
    }

    "return what SubmissionService returns in the body" in {
      withAuthorizedUser()
      val serviceResponseContent = Seq(exampleSubmission(), exampleSubmission(), exampleSubmission())
      when(submissionServiceMock.getSubmissionsByEori(any[String]))
        .thenReturn(Future.successful(serviceResponseContent))

      val result = routeGet(uri = getAllSubmissionsUri)

      status(result) must be(OK)
      contentAsJson(result) must equal(Json.toJson(serviceResponseContent))
    }
  }

}
