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

class MovementsNotificationReceivedSpec extends ComponentTestSpec {

  val endpoint = "/customs-declare-exports/notifyMovement"

  lazy val validNotificationRequest: FakeRequest[AnyContentAsXml] = FakeRequest("POST", endpoint)
    .withHeaders(ValidHeaders.toSeq: _*)
    .withXmlBody(movementXml)

  feature("Notification Service should handle notifications when") {

    pending
    scenario("an authorised user is waiting for notification") {

      startInventoryLinkingService(ACCEPTED)
      val request: FakeRequest[AnyContentAsXml] = validNotificationRequest

      Given("user is authorised")
      authServiceAuthorizesWithEoriAndNoRetrievals()

      When("a POST request with data is sent to the movements API")
      val result: Future[Result] = route(app = app, request).value

      And("with notification should be handled")
      withMovementNotificationRepository(true)

      Then(s"a response with a 202 status is received")
      status(result) shouldBe 202

      And(s"the response body is 202")
      contentAsString(result) shouldBe 202

      And("the movements notification repository is called correctly")
      eventually(verifyMovementNotificationRepositoryIsCorrectlyCalled(declarantEoriValue))

      And("the request was authorised with AuthService")
      eventually(verifyAuthServiceCalledForNonCsp())
    }
  }
}
