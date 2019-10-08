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

package unit.uk.gov.hmrc.exports.movements.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Request
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.exports.movements.controllers.MovementsController
import uk.gov.hmrc.exports.movements.controllers.request.MovementRequest
import uk.gov.hmrc.exports.movements.models.movements.{Choice, ConsignmentReference, MovementDetails}
import uk.gov.hmrc.exports.movements.services.SubmissionService
import unit.uk.gov.hmrc.exports.movements.base.AuthTestSupport
import utils.FakeRequestCSRFSupport._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class MovementsControllerUnitSpec extends WordSpec with MustMatchers with MockitoSugar with AuthTestSupport {

  val submissionServiceMock = mock[SubmissionService]

  val controller =
    new MovementsController(mockAuthConnector, submissionServiceMock, stubControllerComponents())(global)

  val correctJson = MovementRequest(
    choice = Choice.Arrival,
    consignmentReference = ConsignmentReference("reference", "value"),
    movementDetails = MovementDetails("dateTime")
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    withAuthorizedUser()
  }

  override protected def afterEach(): Unit = {
    reset(submissionServiceMock)

    super.afterEach()
  }

  protected def postRequest(body: MovementRequest): Request[MovementRequest] =
    FakeRequest("POST", "")
      .withHeaders(("Content-Type", "application/json"))
      .withBody(body)
      .withCSRFToken

  "Consolidation Controller" should {

    "return 202 (Accepted)" when {

      "consolidation submission ends with success" in {

        when(submissionServiceMock.submitConsolidation(any(), any())(any()))
          .thenReturn(Future.successful((): Unit))

        val result = controller.createMovement()(postRequest(correctJson))

        status(result) mustBe ACCEPTED
      }
    }
  }
}
