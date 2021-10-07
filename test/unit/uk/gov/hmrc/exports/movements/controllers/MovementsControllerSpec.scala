/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Request
import play.api.test.Helpers._
import play.api.test._
import stubs.FakeRequestCSRFSupport._
import testdata.CommonTestData._
import uk.gov.hmrc.exports.movements.base.UnitSpec
import uk.gov.hmrc.exports.movements.models.movements.{ConsignmentReference, MovementDetails, MovementsExchange}
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.MovementType
import uk.gov.hmrc.exports.movements.services.SubmissionService

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class MovementsControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val submissionServiceMock = mock[SubmissionService]

  private val controller =
    new MovementsController(submissionServiceMock, stubControllerComponents())(global)

  private val correctJson = MovementsExchange(
    eori = validEori,
    choice = MovementType.Arrival,
    consignmentReference = ConsignmentReference("reference", "value"),
    movementDetails = Some(MovementDetails("dateTime"))
  )

  override protected def beforeEach(): Unit =
    super.beforeEach()

  override protected def afterEach(): Unit = {
    reset(submissionServiceMock)

    super.afterEach()
  }

  protected def postRequest(body: MovementsExchange): Request[MovementsExchange] =
    FakeRequest("POST", "")
      .withHeaders(JsonContentTypeHeader)
      .withBody(body)
      .withCSRFToken

  "Consolidation Controller" should {

    "return 202 (Accepted)" when {

      "consolidation submission ends with success" in {

        when(submissionServiceMock.submit(any[MovementsExchange]())(any()))
          .thenReturn(Future.successful((): Unit))

        val result = controller.createMovement()(postRequest(correctJson))

        status(result) shouldBe ACCEPTED
      }
    }
  }
}
