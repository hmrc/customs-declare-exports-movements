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

package component.uk.gov.hmrc.exports.movements

import component.uk.gov.hmrc.exports.movements.base.ComponentTestSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.IntegrationPatience
import play.api.mvc.{AnyContentAsJson, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException
import reactivemongo.core.errors.ConnectionException
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import utils.testdata.CommonTestData.{validEori, ValidJsonHeaders}
import utils.testdata.MovementsTestData._

import scala.concurrent.{Await, Future}

class MovementsSubmissionReceivedSpec extends ComponentTestSpec with IntegrationPatience {

  val submitArrivalEndpoint = "/movements"

  lazy val validMovementSubmissionRequest: FakeRequest[AnyContentAsJson] = FakeRequest(POST, submitArrivalEndpoint)
    .withHeaders(ValidJsonHeaders.toSeq: _*)
    .withJsonBody(exampleDepartureRequestJson)

  feature("Movements Service should handle submissions when") {

    scenario("an authorised user successfully submits a movements declaration") {

      testScenario(
        primeIleApiStubToReturnStatus = ACCEPTED,
        moveSubRepoMockedResult = true,
        moveSubRepoIsCalled = true,
        expectedResponseStatus = ACCEPTED,
        expectedResponseBody = exampleDepartureRequestJson.toString
      )
    }

    scenario("an authorised user successfully submits a movements declaration, but it is not persisted in DB") {

      testScenario(
        primeIleApiStubToReturnStatus = ACCEPTED,
        moveSubRepoMockedResult = false,
        moveSubRepoIsCalled = true,
        expectedResponseStatus = INTERNAL_SERVER_ERROR,
        expectedResponseBody = "DatabaseException['ERROR']"
      )
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 500") {

      startInventoryLinkingService(INTERNAL_SERVER_ERROR)

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, validMovementSubmissionRequest).value

      Then(s"a response with a $INTERNAL_SERVER_ERROR status is received")
      val exception = intercept[CustomsInventoryLinkingUpstreamException] {
        await(result)
      }

      exception.getStatus shouldBe INTERNAL_SERVER_ERROR

      And("the ILE API service is called correctly")
      eventually(verifyILEServiceWasCalled(requestBody = exampleDepartureRequestXML.toString, expectedEori = validEori))

      And("the movements submission repository is not called")
      verifyMovementSubmissionRepositoryWasNotCalled()
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 400") {

      startInventoryLinkingService(BAD_REQUEST)

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, validMovementSubmissionRequest).value

      Then(s"a response with a $BAD_REQUEST status is received")
      val exception = intercept[CustomsInventoryLinkingUpstreamException] {
        await(result)
      }

      exception.getStatus shouldBe BAD_REQUEST

      And("the ILE API service is called correctly")
      eventually(verifyILEServiceWasCalled(requestBody = exampleDepartureRequestXML.toString, expectedEori = validEori))

      And("the movements submission repository is not called")
      verifyMovementSubmissionRepositoryWasNotCalled()
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 401") {

      startInventoryLinkingService(UNAUTHORIZED)

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, validMovementSubmissionRequest).value

      Then(s"a response with a $UNAUTHORIZED status is received")
      val exception = intercept[CustomsInventoryLinkingUpstreamException] {
        await(result)
      }

      exception.getStatus shouldBe UNAUTHORIZED

      And("the ILE API service is called correctly")
      eventually(verifyILEServiceWasCalled(requestBody = exampleDepartureRequestXML.toString, expectedEori = validEori))

      And("the movements submission repository is not called")
      verifyMovementSubmissionRepositoryWasNotCalled()
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 404") {

      startInventoryLinkingService(NOT_FOUND)

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, validMovementSubmissionRequest).value

      Then(s"a response with a $NOT_FOUND status is received")
      val exception = intercept[CustomsInventoryLinkingUpstreamException] {
        await(result)
      }

      exception.getStatus shouldBe NOT_FOUND

      And("the ILE API service is called correctly")
      eventually(verifyILEServiceWasCalled(requestBody = exampleDepartureRequestXML.toString, expectedEori = validEori))

      And("the movements submission repository is not called")
      verifyMovementSubmissionRepositoryWasNotCalled()
    }

    scenario("an authorised user tries to submit movements declaration, but ILE service is down") {

      val request: FakeRequest[AnyContentAsJson] = validMovementSubmissionRequest

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      And("submission should be persisted")
      withMovementSubmissionRepository(true)

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, request).value

      val exception = intercept[CustomsInventoryLinkingUpstreamException] {
        await(result)
      }

      exception.getStatus shouldBe NOT_FOUND

      And("the ILE API service was called correctly")
      eventually(verifyILEServiceWasCalled(requestBody = exampleDepartureRequestXML.toString, expectedEori = validEori))

      And("the movement submission repository was not called")
      eventually(verifyMovementSubmissionRepositoryWasNotCalled())

      And("the request was authorised with AuthService")
      eventually(verifyAuthServiceCalledForNonCsp())
    }

    scenario("an authorised user tries to submit movements declaration, but movement repository is down") {

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      And("ILE accept his request")
      startInventoryLinkingService(ACCEPTED)

      And("Database does not work")
      when(movementSubmissionsRepositoryMock.insert(any())(any()))
        .thenReturn(
          Future.failed(
            new PrimaryUnavailableException(
              "Supervisor-1",
              "Connection-1",
              ConnectionException("No primary node is available!")
            )
          )
        )
      val request: FakeRequest[AnyContentAsJson] = validMovementSubmissionRequest

      When("a POST request with data is sent to the movements API")
      an[Exception] mustBe thrownBy {
        await(route(app = app, request).value)
      }

      And("the ILE API service is called correctly")
      eventually(verifyILEServiceWasCalled(requestBody = exampleDepartureRequestXML.toString, expectedEori = validEori))

      And("the movement submission repository is called correctly")
      eventually(verifyMovementSubmissionRepositoryIsCorrectlyCalled(validEori))

      And("the request was authorised with AuthService")
      eventually(verifyAuthServiceCalledForNonCsp())
    }

    scenario("an unauthorised user try to submit movements declaration") {
      stubUnauthorizedForAll()
      startInventoryLinkingService(ACCEPTED)

      val request: FakeRequest[AnyContentAsJson] = validMovementSubmissionRequest

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, request).value

      Then("a response with a Unauthorized status is received")
      status(result) shouldBe UNAUTHORIZED

      And("the response body contains error")
      val outcome = contentAsJson(result)
      (outcome \ "code").as[String] shouldEqual "UNAUTHORIZED"
      (outcome \ "message").as[String] shouldEqual "Unauthorized for exports"

      And("the ILE API service is not called")
      eventually(verifyILEServiceWasNotCalled())

      And("the movement submission repository is not called")
      eventually(verifyMovementSubmissionRepositoryWasNotCalled())

      And("the AuthService is called")
      eventually(verifyAuthServiceCalledForNonCsp())
    }

    def testScenario(
      primeIleApiStubToReturnStatus: Int,
      moveSubRepoMockedResult: Boolean,
      moveSubRepoIsCalled: Boolean,
      expectedResponseStatus: Int,
      expectedResponseBody: String
    ): Unit = {

      startInventoryLinkingService(primeIleApiStubToReturnStatus)
      val request: FakeRequest[AnyContentAsJson] = validMovementSubmissionRequest

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, request).value

      And("movements submission should be handled")
      withMovementSubmissionRepository(moveSubRepoMockedResult)

      if (!moveSubRepoMockedResult) {
        Then(s"a response with a $expectedResponseStatus status is received")
        val exception = the[Exception] thrownBy {
          Await.result(result, patienceConfig.timeout)
        }
        And(s"the response body is $expectedResponseBody")
        exception should have message expectedResponseBody
      } else {
        Then(s"a response with a $expectedResponseStatus status is received")
        status(result) shouldBe expectedResponseStatus

        And(s"the response body is $expectedResponseBody")
        contentAsString(result) should include(expectedResponseBody)
      }

      And("the ILE API service is called correctly")
      eventually(verifyILEServiceWasCalled(requestBody = exampleDepartureRequestXML.toString, expectedEori = validEori))

      if (moveSubRepoIsCalled) {
        And("the movements submission repository is called correctly")
        eventually(verifyMovementSubmissionRepositoryIsCorrectlyCalled(validEori))
      } else {
        And("the movements submission repository is not called")
        verifyMovementSubmissionRepositoryWasNotCalled()
      }

      And("the request was authorised with AuthService")
      eventually(verifyAuthServiceCalledForNonCsp())
    }
  }
}
