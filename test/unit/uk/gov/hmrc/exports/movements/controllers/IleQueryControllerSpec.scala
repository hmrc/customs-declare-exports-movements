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
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers._
import testdata.CommonTestData._
import testdata.notifications.NotificationTestData.{notificationIleQueryResponse_1, notificationIleQueryResponse_2}
import uk.gov.hmrc.exports.movements.controllers.FakeRequestFactory.{postRequestWithBody, _}
import uk.gov.hmrc.exports.movements.errors.TimeoutError
import uk.gov.hmrc.exports.movements.models.common.UcrType.Ducr
import uk.gov.hmrc.exports.movements.models.movements.IleQueryRequest
import uk.gov.hmrc.exports.movements.models.notifications.exchange.IleQueryResponseExchange
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.exports.movements.services.IleQueryService

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class IleQueryControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  private val ileQueryService = mock[IleQueryService]
  private val controller = new IleQueryController(ileQueryService, stubControllerComponents())(global)

  private implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(ileQueryService)
  }

  override protected def afterEach(): Unit = {
    reset(ileQueryService)

    super.afterEach()
  }

  "Ile Query Controller on submitIleQuery" should {

    "return ACCEPTED" when {

      "query is successfully processed" in {

        when(ileQueryService.submit(any())(any())).thenReturn(Future.successful("conversationId"))
        val ileQueryRequest = IleQueryRequest("GB12345678912345", Some("12345"), UcrBlock(ucr = "9GB025115188654-IAZ1", ucrType = Ducr.codeValue))
        val request = postRequestWithBody(ileQueryRequest).withHeaders(JsonContentTypeHeader)

        val result = controller.submitIleQuery()(request)

        status(result) mustBe ACCEPTED
      }
    }
  }

  "Ile Query Controller on submitIleQuery" when {

    "everything works correctly" should {

      "call IleQueryService, passing SearchParameters" in {

        val ileQueryResponseExchanges =
          Seq(
            IleQueryResponseExchange(notificationIleQueryResponse_1),
            IleQueryResponseExchange(notificationIleQueryResponse_2.copy(conversationId = conversationId))
          )
        when(ileQueryService.fetchResponses(any[SearchParameters])).thenReturn(Future.successful(Right(ileQueryResponseExchanges)))

        controller
          .getIleQueryResponses(eori = Some(validEori), providerId = Some(validProviderId), conversationId = conversationId)(getRequest())
          .futureValue

        verify(ileQueryService, times(1))
          .fetchResponses(SearchParameters(eori = Some(validEori), providerId = Some(validProviderId), conversationId = Some(conversationId)))
      }

      "return Ok status with Sequence containing IleQueryResponseExchange returned by IleQueryService" in {

        val ileQueryResponseExchanges =
          Seq(
            IleQueryResponseExchange(notificationIleQueryResponse_1),
            IleQueryResponseExchange(notificationIleQueryResponse_2.copy(conversationId = conversationId))
          )
        when(ileQueryService.fetchResponses(any[SearchParameters])).thenReturn(Future.successful(Right(ileQueryResponseExchanges)))

        val result = controller
          .getIleQueryResponses(eori = Some(validEori), providerId = Some(validProviderId), conversationId = conversationId)(getRequest())

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(ileQueryResponseExchanges)
      }
    }

    "IleQueryService returns empty Sequence" should {
      "return Ok status with empty Sequence" in {

        when(ileQueryService.fetchResponses(any[SearchParameters])).thenReturn(Future.successful(Right(Seq.empty)))

        val result = controller
          .getIleQueryResponses(eori = Some(validEori), providerId = Some(validProviderId), conversationId = conversationId)(getRequest())

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(Seq.empty[IleQueryResponseExchange])
      }
    }

    "IleQueryService returns Either.Left" should {
      "return FailedDependency (424) status with TimeoutError message from IleQueryService" in {

        when(ileQueryService.fetchResponses(any[SearchParameters])).thenReturn(Future.successful(Left(TimeoutError("TIMEOUT"))))

        val result = controller
          .getIleQueryResponses(eori = Some(validEori), providerId = Some(validProviderId), conversationId = conversationId)(getRequest())

        status(result) mustBe FAILED_DEPENDENCY
        contentAsString(result) mustBe "TIMEOUT"
      }
    }
  }

}
