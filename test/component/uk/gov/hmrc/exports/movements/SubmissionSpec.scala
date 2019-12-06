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

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.controllers.routes
import uk.gov.hmrc.exports.movements.models.notifications.{Entry, Notification, NotificationData, UcrBlock}
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, Submission}

/*
 * Component Tests are Intentionally Explicit with the JSON input, XML & DB output and DONT use TestData helpers.
 * That way these tests act as a "spec" for our API, and we dont get unintentional API changes as a result of Model/TestData refactors etc.
 */
class SubmissionSpec extends ComponentSpec {

  private val submission =
    Submission(
      uuid = "id",
      eori = "eori",
      providerId = Some("pid"),
      conversationId = "conversation-id",
      ucrBlocks = Seq.empty,
      actionType = ActionType.Arrival,
      requestTimestamp = currentInstant
    )
  private val submissionJson = Json.obj(
    "uuid" -> "id",
    "eori" -> "eori",
    "providerId" -> "pid",
    "conversationId" -> "conversation-id",
    "ucrBlocks" -> Json.arr(),
    "actionType" -> "Arrival",
    "requestTimestamp" -> currentInstant
  )

  "GET" should {
    "return 200" when {
      "No filters" in {
        // Given
        givenAnExisting(submission)

        // When
        val response = get(routes.SubmissionController.getAllSubmissions(eori = None, providerId = None))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr()
      }

      "Eori" in {
        // Given
        givenAnExisting(submission)

        // When
        val response = get(routes.SubmissionController.getAllSubmissions(eori = Some("eori"), providerId = None))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(submissionJson)
      }

      "Provider ID" in {
        // Given
        givenAnExisting(submission)

        // When
        val response = get(routes.SubmissionController.getAllSubmissions(eori = None, providerId = Some("pid")))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(submissionJson)
      }
    }
  }

  "GET /id" should {
    "return 200" when {
      "No filters" in {
        // Given
        givenAnExisting(submission)

        // When
        val response =
          get(routes.SubmissionController.getSubmission(eori = None, providerId = None, conversationId = "conversation-id"))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe submissionJson
      }

      "Eori" in {
        // Given
        givenAnExisting(submission)

        // When
        val response =
          get(routes.SubmissionController.getSubmission(eori = Some("eori"), providerId = None, conversationId = "conversation-id"))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe submissionJson
      }

      "Provider ID" in {
        // Given
        givenAnExisting(submission)

        // When
        val response =
          get(routes.SubmissionController.getSubmission(eori = None, providerId = Some("pid"), conversationId = "conversation-id"))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe submissionJson
      }
    }
  }

}
