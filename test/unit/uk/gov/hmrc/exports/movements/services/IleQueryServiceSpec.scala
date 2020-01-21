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

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, InOrder, Mockito}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.models.movements.IleQueryRequest
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.IleQuerySubmission
import uk.gov.hmrc.exports.movements.models.{CustomsInventoryLinkingResponse, UserIdentification}
import uk.gov.hmrc.exports.movements.repositories.IleQueryRepository
import uk.gov.hmrc.exports.movements.services.{ILEMapper, IleQueryService}
import uk.gov.hmrc.http.HeaderCarrier
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder.dummyWriteResultSuccess
import utils.testdata.CommonTestData._
import utils.testdata.IleQuerySubmissionTestData.ileQueryXml

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class IleQueryServiceSpec extends WordSpec with MockitoSugar with MustMatchers with ScalaFutures with BeforeAndAfterEach with IntegrationPatience {

  implicit private val hc = mock[HeaderCarrier]

  private val ileMapper = mock[ILEMapper]
  private val ileQueryRepository = mock[IleQueryRepository]
  private val ileConnector = mock[CustomsInventoryLinkingExportsConnector]

  private val ileQueryService = new IleQueryService(ileMapper, ileQueryRepository, ileConnector)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(ileMapper, ileQueryRepository, ileConnector)
    when(ileMapper.generateIleQuery(any[UcrBlock])).thenReturn(ileQueryXml(UcrBlock(ucr = ucr, ucrType = "D")))
    when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any()))
      .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(""))))
    when(ileQueryRepository.insert(any[IleQuerySubmission])(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
  }

  override protected def afterEach(): Unit = {
    reset(ileMapper, ileQueryRepository, ileConnector)

    super.afterEach()
  }

  "IleQueryService on submit" when {

    val ileQueryRequest = IleQueryRequest(validEori, Some(validProviderId), UcrBlock(ucr = ucr, ucrType = "D"))

    "everything works correctly" should {

      "call ILEMapper, IleConnector and IleQueryRepository in this order" in {

        ileQueryService.submit(ileQueryRequest).futureValue

        val inOrder: InOrder = Mockito.inOrder(ileMapper, ileConnector, ileQueryRepository)
        inOrder.verify(ileMapper, times(1)).generateIleQuery(any())
        inOrder.verify(ileConnector, times(1)).submit(any(), any())(any())
        inOrder.verify(ileQueryRepository, times(1)).insert(any())(any())
      }

      "call ILEMapper once, passing UcrBlock from request" in {

        ileQueryService.submit(ileQueryRequest).futureValue

        verify(ileMapper, times(1)).generateIleQuery(ileQueryRequest.ucrBlock)
      }

      "call IleConnector once, passing IleQueryRequest and request xml returned from ILEMapper" in {

        val queryXml = ileQueryXml(UcrBlock(ucr = ucr, ucrType = "D"))
        when(ileMapper.generateIleQuery(any[UcrBlock])).thenReturn(queryXml)

        ileQueryService.submit(ileQueryRequest).futureValue

        verify(ileConnector, times(1)).submit(meq(ileQueryRequest), meq(queryXml))(any())
      }

      "call IleQueryRepository once, passing constructed IleQuerySubmission with Conversation ID returned from IleConnector" in {

        when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))

        ileQueryService.submit(ileQueryRequest).futureValue

        val ileQuerySubmissionCaptor: ArgumentCaptor[IleQuerySubmission] = ArgumentCaptor.forClass(classOf[IleQuerySubmission])
        verify(ileQueryRepository, times(1)).insert(ileQuerySubmissionCaptor.capture())(any())
        val actualIleQuerySubmission = ileQuerySubmissionCaptor.getValue

        actualIleQuerySubmission.eori mustBe validEori
        actualIleQuerySubmission.providerId mustBe defined
        actualIleQuerySubmission.providerId.get mustBe validProviderId
        actualIleQuerySubmission.conversationId mustBe conversationId
        actualIleQuerySubmission.ucrBlock mustBe UcrBlock(ucr = ucr, ucrType = "D")
        actualIleQuerySubmission.responses mustBe Seq.empty
      }

      "return successful Future with Conversation ID returned from IleConnector" in {

        when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))

        ileQueryService.submit(ileQueryRequest).futureValue mustBe conversationId
      }
    }

    "ILEMapper throws an exception" should {

      "return failed Future" in {

        val exceptionMsg = "Test Exception message"
        when(ileMapper.generateIleQuery(any[UcrBlock]))
          .thenThrow(new RuntimeException(exceptionMsg))

        the[Exception] thrownBy {
          ileQueryService.submit(ileQueryRequest).futureValue
        } must have message exceptionMsg
      }

      "not call IleConnector nor IleQueryRepository" in {

        when(ileMapper.generateIleQuery(any[UcrBlock])).thenThrow(new RuntimeException("Test Exception message"))

        an[Exception] mustBe thrownBy {
          ileQueryService.submit(ileQueryRequest).futureValue
        }

        verifyZeroInteractions(ileConnector)
        verifyZeroInteractions(ileQueryRepository)
      }
    }

    "IleConnector returns CustomsInventoryLinkingResponse without Conversation ID" should {

      "return failed Future" in {

        when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, None)))

        an[Exception] mustBe thrownBy {
          ileQueryService.submit(ileQueryRequest).futureValue
        }
      }

      "not call IleQueryRepository" in {

        when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, None)))

        ileQueryService.submit(ileQueryRequest).failed.futureValue

        verifyZeroInteractions(ileQueryRepository)
      }
    }

    "IleConnector returns CustomsInventoryLinkingResponse with InternalServerError status" should {

      "return failed Future" in {

        when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(INTERNAL_SERVER_ERROR, Some(conversationId))))

        an[Exception] mustBe thrownBy {
          ileQueryService.submit(ileQueryRequest).futureValue
        }
      }

      "not call IleQueryRepository" in {

        when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(INTERNAL_SERVER_ERROR, Some(conversationId))))

        ileQueryService.submit(ileQueryRequest).failed.futureValue

        verifyZeroInteractions(ileQueryRepository)
      }
    }

    "IleQueryRepository on insert returns WriteResult with Error" should {

      "return failed Future" in {

        val exceptionMsg = "Test Exception message"
        when(ileQueryRepository.insert(any[IleQuerySubmission])(any()))
          .thenReturn(Future.failed[WriteResult](new Exception(exceptionMsg)))

        val exc = ileQueryService.submit(ileQueryRequest).failed.futureValue
        exc.getMessage must include(exceptionMsg)
      }
    }
  }

}
