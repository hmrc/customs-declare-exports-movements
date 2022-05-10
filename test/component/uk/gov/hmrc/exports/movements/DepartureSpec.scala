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
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.MovementType
import uk.gov.hmrc.exports.movements.models.submissions.Submission

/*
 * Component Tests are Intentionally Explicit with the JSON input, XML & DB output and DONT use TestData helpers.
 * That way these tests act as a "spec" for our API, and we dont get unintentional API changes as a result of Model/TestData refactors etc.
 */
class DepartureSpec extends ComponentSpec {

  "POST" should {
    "return 201" in {
      // Given
      givenIleApiAcceptsTheSubmission("conversation-id")

      // When
      val response = post(
        routes.MovementsController.createMovement(),
        Json.obj(
          "eori" -> "eori",
          "choice" -> "Departure",
          "consignmentReference" -> Json.obj("reference" -> "M", "referenceValue" -> "UCR"),
          "location" -> Json.obj("code" -> "abc"),
          "movementDetails" -> Json.obj("dateTime" -> "2020-01-01T00:00:00Z"),
          "transport" -> Json.obj("modeOfTransport" -> "mode", "nationality" -> "nationality", "transportId" -> "transportId")
        )
      )

      // Then
      status(response) mustBe ACCEPTED

      val submissions: Seq[Submission] = theSubmissionsFor("eori")
      submissions.size mustBe 1
      submissions.head.conversationId mustBe "conversation-id"
      submissions.head.ucrBlocks mustBe Seq(UcrBlock(ucr = "UCR", ucrType = Mucr.codeValue))
      submissions.head.actionType mustBe MovementType.Departure

      verify(
        postRequestedToILE()
          .withRequestBody(equalToXml(<inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>EDL</messageCode>
              <ucrBlock>
                <ucr>UCR</ucr>
                <ucrType>M</ucrType>
              </ucrBlock>
              <goodsLocation>abc</goodsLocation>
              <goodsDepartureDateTime>2020-01-01T00:00:00Z</goodsDepartureDateTime>
              <transportDetails>
                <transportID>transportId</transportID>
                <transportMode>mode</transportMode>
                <transportNationality>nationality</transportNationality>
              </transportDetails>
            </inventoryLinkingMovementRequest>))
      )
    }
  }
}
