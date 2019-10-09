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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.movements.services.SubmissionService
import unit.uk.gov.hmrc.exports.movements.base.AuthTestSupport
import utils.testdata.CommonTestData.ValidJsonHeaders
import utils.testdata.MovementsTestData._

import scala.concurrent.Future

class MovementsControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with AuthTestSupport with BeforeAndAfterEach with ScalaFutures
    with MustMatchers {

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[AuthConnector].to(mockAuthConnector), bind[SubmissionService].to(submissionServiceMock))
    .build()

  private val movementsUri = "/movements"

  private val submissionServiceMock = mock[SubmissionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, submissionServiceMock)
  }

  "Movement Controller" should {

    "return ACCEPTED during posting movement" in {

      withAuthorizedUser()
      when(submissionServiceMock.submitMovement(any(), any())(any())).thenReturn(Future.successful((): Unit))

      val Some(result) = route(
        app,
        FakeRequest(POST, movementsUri)
          .withHeaders(ValidJsonHeaders.toSeq: _*)
          .withJsonBody(exampleDepartureRequestJson)
      )

      status(result) mustBe ACCEPTED
    }
  }
}
