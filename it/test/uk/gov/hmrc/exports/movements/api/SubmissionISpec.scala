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

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.base.ApiSpec
import uk.gov.hmrc.exports.movements.controllers.routes.SubmissionController
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.MovementType
import uk.gov.hmrc.exports.movements.models.submissions.Submission

/*
 * API Tests are Intentionally Explicit with the JSON input, XML & DB output and DONT use TestData helpers.
 * That way these tests act as a "spec" for our API, and we dont get unintentional API changes as a result of Model/TestData refactors etc.
 */
class SubmissionISpec extends ApiSpec {

  val conversationId = "conversation-id"

  private val submission =
    Submission(
      uuid = "id",
      eori = "eori",
      providerId = Some("pid"),
      conversationId = conversationId,
      ucrBlocks = Seq.empty,
      actionType = MovementType.Arrival,
      requestTimestamp = currentInstant
    )

  private val submissionJson = Json.toJson(submission)

  "GET" should {
    "return 200" when {
      "No filters" in {
        // Given
        givenAnExisting(submission)

        // When
        val response = get(SubmissionController.getAllSubmissions(eori = None, providerId = None))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr()
      }

      "Eori" in {
        // Given
        givenAnExisting(submission)

        // When
        val response = get(SubmissionController.getAllSubmissions(eori = Some("eori"), providerId = None))

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe Json.arr(submissionJson)
      }

      "Provider ID" in {
        // Given
        givenAnExisting(submission)

        // When
        val response = get(SubmissionController.getAllSubmissions(eori = None, providerId = Some("pid")))

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
        val call = SubmissionController.getSubmission(eori = None, providerId = None, conversationId = conversationId)
        val response = get(call)

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe submissionJson
      }

      "Eori" in {
        // Given
        givenAnExisting(submission)

        // When
        val call = SubmissionController.getSubmission(eori = Some("eori"), providerId = None, conversationId = conversationId)
        val response = get(call)

        // Then
        status(response) mustBe OK
        contentAsJson(response) mustBe submissionJson
      }

      "Provider ID" in {
        // Given
        givenAnExisting(submission)

        // When
        val call = SubmissionController.getSubmission(eori = None, providerId = Some("pid"), conversationId = conversationId)
        val result = get(call)

        // Then
        status(result) mustBe OK
        contentAsJson(result) mustBe submissionJson
      }
    }
  }
}
