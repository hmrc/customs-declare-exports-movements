/*
 * Copyright 2024 HM Revenue & Customs
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
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.Request
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.Helpers._
import play.api.test._
import utils.testdata.CommonTestData._
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationRequest
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationRequest._
import uk.gov.hmrc.exports.movements.services.SubmissionService

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ConsolidationControllerSpec extends AnyWordSpec with BeforeAndAfterEach with Matchers {

  private val submissionService = mock[SubmissionService]
  private val controller = new ConsolidationController(submissionService, stubControllerComponents())(global)

  private val mucr = ucr
  private val ducr = ucr_2
  private val correctRequest = AssociateDucrRequest(eori = validEori, mucr = mucr, ucr = ducr)

  override protected def beforeEach(): Unit =
    super.beforeEach()

  override protected def afterEach(): Unit = {
    reset(submissionService)

    super.afterEach()
  }

  protected def postRequest(body: ConsolidationRequest): Request[ConsolidationRequest] =
    FakeRequest("POST", "")
      .withHeaders(("Content-Type", "application/json"))
      .withBody(body)
      .withCSRFToken

  "Consolidation Controller" should {

    "return 202 (Accepted)" when {

      "consolidation submission ends with success" in {
        val conversationId = "conversationId"
        when(submissionService.submit(any[ConsolidationRequest]())(any())).thenReturn(Future.successful(conversationId))

        val result = controller.submitConsolidation()(postRequest(correctRequest))

        status(result) mustBe ACCEPTED
        contentAsString(result) mustBe conversationId
        verify(submissionService).submit(any[ConsolidationRequest]())(any())
      }
    }
  }
}
