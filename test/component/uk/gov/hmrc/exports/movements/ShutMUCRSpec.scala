/*
 * Copyright 2020 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock.verify
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.controllers.routes
import uk.gov.hmrc.exports.movements.models.common.UcrType.Mucr
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType
import uk.gov.hmrc.exports.movements.models.submissions.Submission

/*
 * Component Tests are Intentionally Explicit with the JSON input, XML & DB output and DONT use TestData helpers.
 * That way these tests act as a "spec" for our API, and we dont get unintentional API changes as a result of Model/TestData refactors etc.
 */
class ShutMUCRSpec extends ComponentSpec {

  "POST" should {
    "return 201" in {
      // Given
      givenIleApiAcceptsTheSubmission("conversation-id")

      // When
      val response = post(
        routes.ConsolidationController.submitConsolidation(),
        Json.obj("providerId" -> "pid", "eori" -> "eori", "consolidationType" -> "ShutMucr", "mucr" -> "UCR")
      )

      // Then
      status(response) mustBe ACCEPTED

      val submissions: Seq[Submission] = theSubmissionsFor("eori")
      submissions.size mustBe 1
      submissions.head.conversationId mustBe "conversation-id"
      submissions.head.ucrBlocks mustBe Seq(UcrBlock(ucr = "UCR", ucrType = Mucr.codeValue))
      submissions.head.actionType mustBe ConsolidationType.ShutMucr

      verify(
        postRequestedToILE()
          .withRequestBody(equalToXml(<inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
            <messageCode>CST</messageCode>
            <masterUCR>UCR</masterUCR>
          </inventoryLinkingConsolidationRequest>))
      )
    }
  }
}
