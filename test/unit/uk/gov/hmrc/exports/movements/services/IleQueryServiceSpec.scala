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

package unit.uk.gov.hmrc.exports.movements.services

import java.time.{Clock, Instant, ZoneOffset}

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.movements.IleQueryRequest
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.repositories.SubmissionRepository
import uk.gov.hmrc.exports.movements.services.{ILEMapper, IleQueryService}
import uk.gov.hmrc.http.HeaderCarrier
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder.dummyWriteResultSuccess
import utils.testdata.CommonTestData._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class IleQueryServiceSpec extends WordSpec with MockitoSugar with MustMatchers with ScalaFutures with BeforeAndAfterEach {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

  val hc = mock[HeaderCarrier]

  val ileMapper = new ILEMapper(Clock.fixed(Instant.now(), ZoneOffset.UTC))
  val submissionRepository = mock[SubmissionRepository]
  val ileConnector = mock[CustomsInventoryLinkingExportsConnector]

  val ileQueryService = new IleQueryService(ileMapper, submissionRepository, ileConnector)(global)

  val conversationId = "conversationId"
  val ileQueryRequest = IleQueryRequest(validEori, Some(validProviderId), UcrBlock(ucr, "D"))

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(submissionRepository, ileConnector)
  }

  override protected def afterEach(): Unit = {
    reset(submissionRepository, ileConnector)

    super.afterEach()
  }

  "Ile Query Service" should {

    "successful submit ILE Query" in {

      when(ileConnector.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))
      when(submissionRepository.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))

      val result = ileQueryService.submit(ileQueryRequest)(hc).futureValue

      result mustBe conversationId
    }

    "fail when non accepted response from ILE" in {

      when(ileConnector.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(OK, Some(conversationId))))

      intercept[CustomsInventoryLinkingUpstreamException] {
        await(ileQueryService.submit(ileQueryRequest)(hc))
      }
    }
  }
}
