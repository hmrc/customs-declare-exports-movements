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
import play.api.mvc.{AnyContentAsXml, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.xml.XML

class MovementsSubmissionReceivedSpec extends ComponentTestSpec {

  val endpoint = "/save-movement-submission"

  lazy val validMovementSubmissionRequest: FakeRequest[AnyContentAsXml] = FakeRequest("POST", endpoint)
    .withHeaders(ValidHeaders.toSeq: _*)
    .withXmlBody(XML.loadString(validInventoryLinkingExportRequest.toXml))

  feature("Movements Service should handle submissions when") {

    scenario("an authorised user successfully submits a movements declaration") {

      testScenario(
        primeIleApiStubToReturnStatus = ACCEPTED,
        moveSubRepoMockedResult = true,
        moveSubRepoIsCalled = true,
        expectedResponseStatus = ACCEPTED,
        expectedResponseBody = "Movement Submission submitted and persisted ok"
      )
    }

    scenario("an authorised user successfully submits a movements declaration, but it is not persisted in DB") {

      testScenario(
        primeIleApiStubToReturnStatus = ACCEPTED,
        moveSubRepoMockedResult = false,
        moveSubRepoIsCalled = true,
        expectedResponseStatus = INTERNAL_SERVER_ERROR,
        expectedResponseBody = "Unable to persist data something bad happened"
      )
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 500") {

      testScenario(
        primeIleApiStubToReturnStatus = INTERNAL_SERVER_ERROR,
        moveSubRepoMockedResult = false,
        moveSubRepoIsCalled = false,
        expectedResponseStatus = INTERNAL_SERVER_ERROR,
        expectedResponseBody = "Non Accepted status returned by Customs Declaration Service"
      )
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 400") {

      testScenario(
        primeIleApiStubToReturnStatus = BAD_REQUEST,
        moveSubRepoMockedResult = true,
        moveSubRepoIsCalled = false,
        expectedResponseStatus = INTERNAL_SERVER_ERROR,
        expectedResponseBody = "Non Accepted status returned by Customs Declaration Service"
      )
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 401") {

      testScenario(
        primeIleApiStubToReturnStatus = UNAUTHORIZED,
        moveSubRepoMockedResult = true,
        moveSubRepoIsCalled = false,
        expectedResponseStatus = INTERNAL_SERVER_ERROR,
        expectedResponseBody = "Non Accepted status returned by Customs Declaration Service"
      )
    }

    scenario("an authorised user tries to submit movements declaration, but the movement service returns 404") {

      testScenario(
        primeIleApiStubToReturnStatus = NOT_FOUND,
        moveSubRepoMockedResult = true,
        moveSubRepoIsCalled = false,
        expectedResponseStatus = INTERNAL_SERVER_ERROR,
        expectedResponseBody = "Non Accepted status returned by Customs Declaration Service"
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
      contentAsString(result) shouldBe "Non Accepted status returned by Customs Declaration Service"

      And("the ILE API service was called correctly")
      eventually(
        verifyILEServiceWasCalled(
          requestBody = validInventoryLinkingExportRequest.toXml,
          expectedEori = declarantEoriValue
        )
      )

      And("the movement submission repository was not called")
      eventually(verifyMovementSubmissionRepositoryWasNotCalled())

      And("the request was authorised with AuthService")
      eventually(verifyAuthServiceCalledForNonCsp())
    }

    scenario("an authorised user tries to submit movements declaration, but movement repository is down") {

      startInventoryLinkingService(ACCEPTED)
      val request: FakeRequest[AnyContentAsXml] = validMovementSubmissionRequest

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, request).value

      Then("a response with a 500 (INTERNAL_SERVER_ERROR) status is received")
      status(result) shouldBe INTERNAL_SERVER_ERROR

      And("the response body contains error")
      contentAsString(result) shouldBe "<?xml version='1.0' encoding='UTF-8'?>\n<errorResponse>\n        <code>INTERNAL_SERVER_ERROR</code>\n        <message>Internal server error</message>\n      </errorResponse>"

      And("the ILE API service is called correctly")
      eventually(
        verifyILEServiceWasCalled(
          requestBody = validInventoryLinkingExportRequest.toXml,
          expectedEori = declarantEoriValue
        )
      )

      And("the movement submission repository is called correctly")
      eventually(verifyMovementSubmissionRepositoryIsCorrectlyCalled(declarantEoriValue))

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
      contentAsString(result) shouldBe expectedResponseBody

      And("the ILE API service is called correctly")
      eventually(
        verifyILEServiceWasCalled(
          requestBody = validInventoryLinkingExportRequest.toXml,
          expectedEori = declarantEoriValue
        )
      )

      if (moveSubRepoIsCalled) {
        And("the movements submission repository is called correctly")
        eventually(verifyMovementSubmissionRepositoryIsCorrectlyCalled(declarantEoriValue))
      } else {
        And("the movements submission repository is not called")
        verifyMovementSubmissionRepositoryWasNotCalled()
      }

      And("the request was authorised with AuthService")
      eventually(verifyAuthServiceCalledForNonCsp())
    }
  }
}
