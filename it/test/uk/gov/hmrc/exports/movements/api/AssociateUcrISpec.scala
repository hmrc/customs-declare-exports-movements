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

package uk.gov.hmrc.exports.movements.api

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.verify
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.base.ApiSpec
import uk.gov.hmrc.exports.movements.controllers.routes
import uk.gov.hmrc.exports.movements.models.UcrType.{Ducr, Mucr}
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType
import uk.gov.hmrc.exports.movements.models.submissions.Submission

/*
 * API Tests are Intentionally Explicit with the JSON input, XML & DB output and DONT use TestData helpers.
 * That way these tests act as a "spec" for our API, and we dont get unintentional API changes as a result of Model/TestData refactors etc.
 */
class AssociateUcrISpec extends ApiSpec {

  "POST" should {
    "return 201" when {

      "used for DUCR Association" in {
        // Given
        givenIleApiAcceptsTheSubmission()

        // When
        val response = post(
          routes.ConsolidationController.submitConsolidation(),
          Json.obj("providerId" -> "pid", "eori" -> "eori", "consolidationType" -> "DucrAssociation", "mucr" -> "MUCR", "ucr" -> "DUCR")
        )

        // Then
        status(response) mustBe ACCEPTED

        val submissions: Seq[Submission] = theSubmissionsFor("eori")
        submissions.size mustBe 1
        submissions.head.conversationId mustBe "conversation-id"
        submissions.head.ucrBlocks mustBe Seq(UcrBlock(ucr = "MUCR", ucrType = Mucr.codeValue), UcrBlock(ucr = "DUCR", ucrType = Ducr.codeValue))
        submissions.head.actionType mustBe ConsolidationType.DucrAssociation

        val body =
          s"""<inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
             |    <messageCode>EAC</messageCode>
             |    <masterUCR>MUCR</masterUCR>
             |    <ucrBlock>
             |        <ucr>DUCR</ucr>
             |        <ucrType>D</ucrType>
             |    </ucrBlock>
             |</inventoryLinkingConsolidationRequest>
             |""".stripMargin.replaceAll("\\n *", "")

        verify(postRequestedToILE().withRequestBody(WireMock.equalTo(body)))
      }

      "used for MUCR Association" in {
        // Given
        givenIleApiAcceptsTheSubmission()

        // When
        val response = post(
          routes.ConsolidationController.submitConsolidation(),
          Json.obj("providerId" -> "pid", "eori" -> "eori", "consolidationType" -> "MucrAssociation", "mucr" -> "MUCR", "ucr" -> "MUCR_2")
        )

        // Then
        status(response) mustBe ACCEPTED

        val submissions: Seq[Submission] = theSubmissionsFor("eori")
        submissions.size mustBe 1
        submissions.head.conversationId mustBe "conversation-id"
        submissions.head.ucrBlocks mustBe Seq(UcrBlock(ucr = "MUCR", ucrType = Mucr.codeValue), UcrBlock(ucr = "MUCR_2", ucrType = Mucr.codeValue))
        submissions.head.actionType mustBe ConsolidationType.MucrAssociation

        val body =
          s"""<inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
             |  <messageCode>EAC</messageCode>
             |  <masterUCR>MUCR</masterUCR>
             |  <ucrBlock>
             |    <ucr>MUCR_2</ucr>
             |    <ucrType>M</ucrType>
             |  </ucrBlock>
             |</inventoryLinkingConsolidationRequest>
             |""".stripMargin.replaceAll("\\n *", "")

        verify(postRequestedToILE().withRequestBody(WireMock.equalTo(body)))
      }
    }
  }
}
