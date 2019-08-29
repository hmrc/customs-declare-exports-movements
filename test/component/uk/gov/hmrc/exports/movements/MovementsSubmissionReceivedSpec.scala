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
import play.api.mvc.{AnyContentAsXml, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException
import reactivemongo.core.errors.ConnectionException
import utils.testdata.CommonTestData.{validEori, ValidHeaders}
import utils.testdata.MovementsTestData._

import scala.concurrent.Future
import scala.xml.XML

class MovementsSubmissionReceivedSpec extends ComponentTestSpec {

  val submitArrivalEndpoint = "/movements/arrival"

  lazy val validMovementSubmissionRequest: FakeRequest[AnyContentAsXml] = FakeRequest(POST, submitArrivalEndpoint)
    .withHeaders(ValidHeaders.toSeq: _*)
    .withXmlBody(XML.loadString(validInventoryLinkingExportRequest.toXml))

  feature("Movements Service should handle submissions when") {

    scenario("an authorised user successfully submits a movements declaration") {

      testScenario(
        primeIleApiStubToReturnStatus = ACCEPTED,
        moveSubRepoMockedResult = true,
        moveSubRepoIsCalled = true,
        expectedResponseStatus = ACCEPTED,
        expectedResponseBody = "Movement Submission submitted successfully"
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

      testScenario(
        primeIleApiStubToReturnStatus = INTERNAL_SERVER_ERROR,
        moveSubRepoMockedResult = false,
        moveSubRepoIsCalled = false,
        expectedResponseStatus = INTERNAL_SERVER_ERROR,
        expectedResponseBody = "Non Accepted status returned by Customs Inventory Linking Exports"
      )
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 400") {

      testScenario(
        primeIleApiStubToReturnStatus = BAD_REQUEST,
        moveSubRepoMockedResult = true,
        moveSubRepoIsCalled = false,
        expectedResponseStatus = INTERNAL_SERVER_ERROR,
        expectedResponseBody = "Non Accepted status returned by Customs Inventory Linking Exports"
      )
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 401") {

      testScenario(
        primeIleApiStubToReturnStatus = UNAUTHORIZED,
        moveSubRepoMockedResult = true,
        moveSubRepoIsCalled = false,
        expectedResponseStatus = INTERNAL_SERVER_ERROR,
        expectedResponseBody = "Non Accepted status returned by Customs Inventory Linking Exports"
      )
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 404") {

      testScenario(
        primeIleApiStubToReturnStatus = NOT_FOUND,
        moveSubRepoMockedResult = true,
        moveSubRepoIsCalled = false,
        expectedResponseStatus = INTERNAL_SERVER_ERROR,
        expectedResponseBody = "Non Accepted status returned by Customs Inventory Linking Exports"
      )
    }

    scenario("an authorised user tries to submit movements declaration, but ILE service is down") {

      val request: FakeRequest[AnyContentAsXml] = validMovementSubmissionRequest

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, request).value

      And("submission should be persisted")
      withMovementSubmissionRepository(true)

      Then("a response with a 500 (INTERNAL_SERVER_ERROR) status is received")
      status(result) shouldBe INTERNAL_SERVER_ERROR

      And("the response body contains error")
      contentAsString(result) should include("Non Accepted status returned by Customs Inventory Linking Exports")

      And("the ILE API service was called correctly")
      eventually(
        verifyILEServiceWasCalled(requestBody = validInventoryLinkingExportRequest.toXml, expectedEori = validEori)
      )

      And("the movement submission repository was not called")
      eventually(verifyMovementSubmissionRepositoryWasNotCalled())

      And("the request was authorised with AuthService")
      eventually(verifyAuthServiceCalledForNonCsp())
    }

    scenario("an authorised user tries to submit movements declaration, but movement repository is down") {

      startInventoryLinkingService(ACCEPTED)
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
      val request: FakeRequest[AnyContentAsXml] = validMovementSubmissionRequest

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, request).value

      Then("a response with a 500 (INTERNAL_SERVER_ERROR) status is received")
      status(result) shouldBe INTERNAL_SERVER_ERROR

      And("the response body contains error")
      contentAsString(result) should include("<?xml version='1.0' encoding='UTF-8'?>")
      contentAsString(result) should include("<code>INTERNAL_SERVER_ERROR</code>")
      contentAsString(result) should include(
        "<message>MongoError['No primary node is available! (Supervisor-1/Connection-1)']</message>"
      )

      And("the ILE API service is called correctly")
      eventually(
        verifyILEServiceWasCalled(requestBody = validInventoryLinkingExportRequest.toXml, expectedEori = validEori)
      )

      And("the movement submission repository is called correctly")
      eventually(verifyMovementSubmissionRepositoryIsCorrectlyCalled(validEori))

      And("the request was authorised with AuthService")
      eventually(verifyAuthServiceCalledForNonCsp())
    }

    scenario("an unauthorised user try to submit movements declaration") {

      startInventoryLinkingService(ACCEPTED)
      val request: FakeRequest[AnyContentAsXml] = validMovementSubmissionRequest

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, request).value

      And("movements submission should not be persisted")
      withMovementSubmissionRepository(false)

      Then("a response with a 500 (INTERNAL_SERVER_ERROR) status is received")
      status(result) shouldBe INTERNAL_SERVER_ERROR

      And("the response body contains error")
      contentAsString(result) shouldBe "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"Internal server error\"}"

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
      val request: FakeRequest[AnyContentAsXml] = validMovementSubmissionRequest

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, request).value

      And("movements submission should be handled")
      withMovementSubmissionRepository(moveSubRepoMockedResult)

      Then(s"a response with a $expectedResponseStatus status is received")
      status(result) shouldBe expectedResponseStatus

      And(s"the response body is $expectedResponseBody")
      contentAsString(result) should include(expectedResponseBody)

      And("the ILE API service is called correctly")
      eventually(
        verifyILEServiceWasCalled(requestBody = validInventoryLinkingExportRequest.toXml, expectedEori = validEori)
      )

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
