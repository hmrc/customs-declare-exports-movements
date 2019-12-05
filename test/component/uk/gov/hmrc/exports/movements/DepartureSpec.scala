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

import com.github.tomakehurst.wiremock.client.WireMock.verify
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.controllers.routes
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, Submission}

class DepartureSpec extends ComponentSpec {

  "POST" should {
    "return 201" in {
      // Given
      givenIleApiAcceptsTheSubmission("conversation-id")

      // When
      val response = post(routes.MovementsController.createMovement(), Json.obj(
        "eori" -> "eori",
        "choice"-> "EDL",
        "consignmentReference" -> Json.obj(
          "reference" -> "M",
          "referenceValue" -> "UCR"
        ),
        "location" -> Json.obj(
          "code" -> "abc"
        ),
        "movementDetails" -> Json.obj(
          "dateTime" -> "2020-01-01T00:00:00Z"
        ),
        "arrivalReference" -> Json.obj(
          "reference" -> "xyz"
        )
      ))

      // Then
      status(response) mustBe ACCEPTED

      val submissions: Seq[Submission] = theSubmissionsFor("eori")
      submissions.size mustBe 1
      submissions.head.conversationId mustBe "conversation-id"
      submissions.head.ucrBlocks mustBe Seq(UcrBlock("UCR", "M"))
      submissions.head.actionType mustBe ActionType.Departure

      verify(
        postRequestedToILE()
          .withRequestBody(equalToXml(
            <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>EDL</messageCode>
              <ucrBlock>
                <ucr>UCR</ucr>
                <ucrType>M</ucrType>
              </ucrBlock>
              <goodsLocation>abc</goodsLocation>
              <goodsDepartureDateTime>2020-01-01T00:00:00Z</goodsDepartureDateTime>
              <movementReference>xyz</movementReference>
            </inventoryLinkingMovementRequest>
          ))
      )
    }
  }

}
