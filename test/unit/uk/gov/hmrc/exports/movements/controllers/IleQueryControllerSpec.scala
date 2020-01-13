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

package unit.uk.gov.hmrc.exports.movements.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import play.api.test.FakeRequest
import uk.gov.hmrc.exports.movements.controllers.IleQueryController
import uk.gov.hmrc.exports.movements.models.movements.IleQueryRequest
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.services.IleQueryService
import utils.FakeRequestCSRFSupport._
import utils.testdata.CommonTestData.JsonContentTypeHeader

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class IleQueryControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  private val ileQueryService = mock[IleQueryService]

  private val controller = new IleQueryController(ileQueryService, stubControllerComponents())(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(ileQueryService)
  }

  override protected def afterEach(): Unit = {
    reset(ileQueryService)

    super.afterEach()
  }

  "Ile Query Controller" should {

    "return ACCEPTED" when {

      "query is successfully processed" in {

        when(ileQueryService.submit(any())(any())).thenReturn(Future.successful("conversationId"))

        val ileQueryRequest = IleQueryRequest("GB12345678912345", Some("12345"), UcrBlock("9GB025115188654-IAZ1", "D"))

        val postRequest = FakeRequest(POST, "")
          .withHeaders(JsonContentTypeHeader)
          .withBody(ileQueryRequest)
          .withCSRFToken

        val result = controller.submitIleQuery()(postRequest)

        status(result) mustBe ACCEPTED
      }
    }
  }
}
