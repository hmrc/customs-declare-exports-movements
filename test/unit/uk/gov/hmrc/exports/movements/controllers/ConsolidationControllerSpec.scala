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
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames.XEoriIdentifierHeaderName
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.metrics.MovementsMetrics
import uk.gov.hmrc.exports.movements.models.submissions.Submission.ActionTypes
import uk.gov.hmrc.exports.movements.services.ConsolidationService
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import unit.uk.gov.hmrc.exports.movements.base.AuthTestSupport
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder.buildConsolidationServiceMock
import utils.ConsolidationTestData._

import scala.concurrent.Future
import scala.xml.Elem

class ConsolidationControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with AuthTestSupport with BeforeAndAfterEach with ScalaFutures
    with MustMatchers {

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[AuthConnector].to(mockAuthConnector), bind[ConsolidationService].to(consolidationServiceMock))
    .build()

  private val shutMucrUri = "/consolidations/shut"
  private val associateDucrUri = "/consolidations/associate"
  private val disassociateDucrUri = "/consolidations/disassociate"

  private val consolidationServiceMock = buildConsolidationServiceMock
  private val metrics: MovementsMetrics = app.injector.instanceOf[MovementsMetrics]
  private val headerValidator: HeaderValidator = app.injector.instanceOf[HeaderValidator]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, consolidationServiceMock)
  }

  "MovementConsolidationController" when {
    "on shutMucr" when {

      "everything works correctly" should {

        "return Accepted status" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          val result = routePostSubmitMovementConsolidation(uri = shutMucrUri)

          status(result) must be(ACCEPTED)
        }

        "call ConsolidationService once, passing EORI and request payload" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          routePostSubmitMovementConsolidation(uri = shutMucrUri).futureValue

          val expectedEori = ValidConsolidationRequestHeaders(XEoriIdentifierHeaderName)
          val contextCaptor: ArgumentCaptor[SubmissionRequestContext] =
            ArgumentCaptor.forClass(classOf[SubmissionRequestContext])

          verify(consolidationServiceMock).submitConsolidationRequest(contextCaptor.capture())(any())

          contextCaptor.getValue.eori must equal(expectedEori)
          contextCaptor.getValue.actionType must equal(ActionTypes.ShutMucr)
          contextCaptor.getValue.requestXml must equal(exampleShutMucrConsolidationRequest)
        }
      }

      "ConsolidationService returns Either.Left" should {

        "return InternalServerError" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Left("")))

          val result = routePostSubmitMovementConsolidation(uri = shutMucrUri)

          status(result) must be(INTERNAL_SERVER_ERROR)
        }
      }

      "provided with invalid request format" should {

        "return ErrorResponse for invalid payload" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          val result = route(
            app,
            FakeRequest(POST, shutMucrUri)
              .withHeaders(ValidConsolidationRequestHeaders.toSeq: _*)
              .withJsonBody(exampleShutMucrConsolidationRequestJson)
          ).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include("Invalid payload")
        }

        "not call ConsolidationService" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          route(
            app,
            FakeRequest(POST, shutMucrUri)
              .withHeaders(ValidConsolidationRequestHeaders.toSeq: _*)
              .withJsonBody(exampleShutMucrConsolidationRequestJson)
          ).get.futureValue

          verifyZeroInteractions(consolidationServiceMock)
        }
      }
    }

    "on associateMucr" when {

      "everything works correctly" should {

        "return Accepted status" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          val result = routePostSubmitMovementConsolidation(
            xmlBody = exampleAssociateDucrConsolidationRequest,
            uri = associateDucrUri
          )

          status(result) must be(ACCEPTED)
        }

        "call ConsolidationService once, passing EORI and request payload" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          routePostSubmitMovementConsolidation(
            xmlBody = exampleAssociateDucrConsolidationRequest,
            uri = associateDucrUri
          ).futureValue

          val expectedEori = ValidConsolidationRequestHeaders(XEoriIdentifierHeaderName)
          val contextCaptor: ArgumentCaptor[SubmissionRequestContext] =
            ArgumentCaptor.forClass(classOf[SubmissionRequestContext])

          verify(consolidationServiceMock).submitConsolidationRequest(contextCaptor.capture())(any())

          contextCaptor.getValue.eori must equal(expectedEori)
          contextCaptor.getValue.actionType must equal(ActionTypes.DucrAssociation)
          contextCaptor.getValue.requestXml must equal(exampleAssociateDucrConsolidationRequest)
        }
      }

      "ConsolidationService returns Either.Left" should {

        "return InternalServerError" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Left("")))

          val result = routePostSubmitMovementConsolidation(
            xmlBody = exampleAssociateDucrConsolidationRequest,
            uri = associateDucrUri
          )

          status(result) must be(INTERNAL_SERVER_ERROR)
        }
      }

      "provided with invalid request format" should {

        "return ErrorResponse for invalid payload" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          val result = route(
            app,
            FakeRequest(POST, associateDucrUri)
              .withHeaders(ValidConsolidationRequestHeaders.toSeq: _*)
              .withJsonBody(exampleAssociateDucrConsolidationRequestJson)
          ).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include("Invalid payload")
        }

        "not call ConsolidationService" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          route(
            app,
            FakeRequest(POST, associateDucrUri)
              .withHeaders(ValidConsolidationRequestHeaders.toSeq: _*)
              .withJsonBody(exampleAssociateDucrConsolidationRequestJson)
          ).get.futureValue

          verifyZeroInteractions(consolidationServiceMock)
        }
      }
    }

    "on disassociateMucr" when {

      "everything works correctly" should {

        "return Accepted status" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          val result = routePostSubmitMovementConsolidation(
            xmlBody = exampleDisassociateDucrConsolidationRequest,
            uri = disassociateDucrUri
          )

          status(result) must be(ACCEPTED)
        }

        "call ConsolidationService once, passing EORI and request payload" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          routePostSubmitMovementConsolidation(
            xmlBody = exampleDisassociateDucrConsolidationRequest,
            uri = disassociateDucrUri
          ).futureValue

          val expectedEori = ValidConsolidationRequestHeaders(XEoriIdentifierHeaderName)
          val contextCaptor: ArgumentCaptor[SubmissionRequestContext] =
            ArgumentCaptor.forClass(classOf[SubmissionRequestContext])

          verify(consolidationServiceMock).submitConsolidationRequest(contextCaptor.capture())(any())

          contextCaptor.getValue.eori must equal(expectedEori)
          contextCaptor.getValue.actionType must equal(ActionTypes.DucrDisassociation)
          contextCaptor.getValue.requestXml must equal(exampleDisassociateDucrConsolidationRequest)
        }
      }

      "ConsolidationService returns Either.Left" should {

        "return InternalServerError" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Left("")))

          val result = routePostSubmitMovementConsolidation(
            xmlBody = exampleDisassociateDucrConsolidationRequest,
            uri = disassociateDucrUri
          )

          status(result) must be(INTERNAL_SERVER_ERROR)
        }
      }

      "provided with invalid request format" should {

        "return ErrorResponse for invalid payload" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          val result = route(
            app,
            FakeRequest(POST, disassociateDucrUri)
              .withHeaders(ValidConsolidationRequestHeaders.toSeq: _*)
              .withJsonBody(exampleDisassociateDucrConsolidationRequestJson)
          ).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include("Invalid payload")
        }

        "not call ConsolidationService" in {
          withAuthorizedUser()
          when(consolidationServiceMock.submitConsolidationRequest(any())(any()))
            .thenReturn(Future.successful(Right((): Unit)))

          route(
            app,
            FakeRequest(POST, disassociateDucrUri)
              .withHeaders(ValidConsolidationRequestHeaders.toSeq: _*)
              .withJsonBody(exampleDisassociateDucrConsolidationRequestJson)
          ).get.futureValue

          verifyZeroInteractions(consolidationServiceMock)
        }
      }
    }

  }

  def routePostSubmitMovementConsolidation(
    headers: Map[String, String] = ValidConsolidationRequestHeaders,
    xmlBody: Elem = exampleShutMucrConsolidationRequest,
    uri: String
  ): Future[Result] =
    route(app, FakeRequest(POST, uri).withHeaders(headers.toSeq: _*).withXmlBody(xmlBody)).get

}
