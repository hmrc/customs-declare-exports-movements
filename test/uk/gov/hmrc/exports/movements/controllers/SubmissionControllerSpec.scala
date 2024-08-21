/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.exports.movements.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.testdata.CommonTestData._
import utils.testdata.MovementsTestData.exampleSubmission
import utils.testdata.notifications.NotificationTestData.validHeaders
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.exports.movements.services.SubmissionService

import scala.concurrent.{ExecutionContext, Future}

class SubmissionControllerSpec extends AnyWordSpec with Matchers with ScalaFutures {

  private val requestGet = FakeRequest(GET, "").withHeaders(validHeaders.toSeq: _*)

  private trait Test {
    val submissionService = mock[SubmissionService]

    val controller = new SubmissionController(submissionService, stubControllerComponents())(ExecutionContext.global)
  }

  "SubmissionController on getAllSubmissions" should {

    "call SubmissionService" in new Test {
      when(submissionService.getSubmissions(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))

      controller.getAllSubmissions(Some(validEori), Some(validProviderId))(requestGet)

      verify(submissionService).getSubmissions(any[SearchParameters])
    }

    "return Ok status" in new Test {
      when(submissionService.getSubmissions(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))

      val result = controller.getAllSubmissions(Some(validEori), Some(validProviderId))(requestGet)

      status(result) mustBe OK
    }

    "return what SubmissionService returns in the body" in new Test {
      val serviceResponseContent =
        Seq(exampleSubmission(), exampleSubmission(conversationId = conversationId_2), exampleSubmission(conversationId = conversationId_3))
      when(submissionService.getSubmissions(any[SearchParameters])).thenReturn(Future.successful(serviceResponseContent))

      val result = controller.getAllSubmissions(Some(validEori), Some(validProviderId))(requestGet)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(serviceResponseContent)
    }
  }

  "SubmissionController on getSingleSubmission" should {

    "call SubmissionService" in new Test {
      when(submissionService.getSingleSubmission(any[SearchParameters])).thenReturn(Future.successful(None))

      controller.getSubmission(Some(validEori), Some(validProviderId), conversationId)(requestGet)

      verify(submissionService).getSingleSubmission(any[SearchParameters])
    }

    "return Ok status" in new Test {
      when(submissionService.getSingleSubmission(any[SearchParameters])).thenReturn(Future.successful(None))

      val result = controller.getSubmission(Some(validEori), Some(validProviderId), conversationId)(requestGet)

      status(result) must be(OK)
    }

    "return what SubmissionService returns in the body" in new Test {
      val serviceResponseContent = Some(exampleSubmission())
      when(submissionService.getSingleSubmission(any[SearchParameters])).thenReturn(Future.successful(serviceResponseContent))

      val result = controller.getSubmission(Some(validEori), Some(validProviderId), conversationId)(requestGet)

      status(result) must be(OK)
      contentAsJson(result) must equal(Json.toJson(serviceResponseContent))
    }
  }

}
