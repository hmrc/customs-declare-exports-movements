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
import uk.gov.hmrc.exports.movements.models.notifications.exchange.IleQueryResponseExchange
import uk.gov.hmrc.exports.movements.models.notifications.exchange.IleQueryResponseExchangeData.SuccessfulResponseExchangeData
import uk.gov.hmrc.exports.movements.models.notifications.queries.IleQueryResponseData
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, Submission}
import uk.gov.hmrc.exports.movements.models.{CustomsInventoryLinkingResponse, UserIdentification}
import uk.gov.hmrc.exports.movements.repositories.{NotificationRepository, SearchParameters, SubmissionRepository}
import uk.gov.hmrc.exports.movements.services.{ILEMapper, IleQueryService, IleQueryTimeoutCalculator}
import uk.gov.hmrc.http.HeaderCarrier
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder.dummyWriteResultSuccess
import utils.testdata.CommonTestData._
import utils.testdata.IleQueryTestData.ileQueryXml
import utils.testdata.MovementsTestData.exampleSubmission
import utils.testdata.notifications.NotificationTestData.notificationIleQueryResponse_1

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class IleQueryServiceSpec extends WordSpec with MockitoSugar with MustMatchers with ScalaFutures with BeforeAndAfterEach with IntegrationPatience {

  implicit private val hc = mock[HeaderCarrier]

  private val ileMapper = mock[ILEMapper]
  private val submissionRepository = mock[SubmissionRepository]
  private val notificationRepository = mock[NotificationRepository]
  private val ileConnector = mock[CustomsInventoryLinkingExportsConnector]
  private val ileQueryTimeoutCalculator = mock[IleQueryTimeoutCalculator]

  private val ileQueryService =
    new IleQueryService(ileMapper, submissionRepository, notificationRepository, ileConnector, ileQueryTimeoutCalculator)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(ileMapper, submissionRepository, ileConnector, notificationRepository, ileQueryTimeoutCalculator)

    when(ileMapper.generateIleQuery(any[UcrBlock])).thenReturn(ileQueryXml(UcrBlock(ucr = ucr, ucrType = "D")))
    when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any()))
      .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(""))))
    when(submissionRepository.insert(any[Submission])(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
    when(submissionRepository.findBy(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))
    when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(Future.successful(Seq.empty))
    when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[Submission])).thenReturn(false)
  }

  override protected def afterEach(): Unit = {
    reset(ileMapper, submissionRepository, ileConnector, notificationRepository, ileQueryTimeoutCalculator)

    super.afterEach()
  }

  "IleQueryService on submit" when {

    val ileQueryRequest = IleQueryRequest(validEori, Some(validProviderId), UcrBlock(ucr = ucr, ucrType = "D"))

    "everything works correctly" should {

      "call ILEMapper, IleConnector and IleQueryRepository in this order" in {

        ileQueryService.submit(ileQueryRequest).futureValue

        val inOrder: InOrder = Mockito.inOrder(ileMapper, ileConnector, submissionRepository)
        inOrder.verify(ileMapper, times(1)).generateIleQuery(any())
        inOrder.verify(ileConnector, times(1)).submit(any(), any())(any())
        inOrder.verify(submissionRepository, times(1)).insert(any())(any())
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

        val ileQuerySubmissionCaptor: ArgumentCaptor[Submission] = ArgumentCaptor.forClass(classOf[Submission])
        verify(submissionRepository, times(1)).insert(ileQuerySubmissionCaptor.capture())(any())
        val actualIleQuerySubmission = ileQuerySubmissionCaptor.getValue

        actualIleQuerySubmission.eori mustBe validEori
        actualIleQuerySubmission.providerId mustBe defined
        actualIleQuerySubmission.providerId.get mustBe validProviderId
        actualIleQuerySubmission.conversationId mustBe conversationId
        actualIleQuerySubmission.ucrBlocks mustBe Seq(UcrBlock(ucr = ucr, ucrType = "D"))
        actualIleQuerySubmission.actionType mustBe ActionType.IleQuery
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
        verifyZeroInteractions(submissionRepository)
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

        verifyZeroInteractions(submissionRepository)
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

        verifyZeroInteractions(submissionRepository)
      }
    }

    "IleQueryRepository on insert returns WriteResult with Error" should {

      "return failed Future" in {

        val exceptionMsg = "Test Exception message"
        when(submissionRepository.insert(any[Submission])(any()))
          .thenReturn(Future.failed[WriteResult](new Exception(exceptionMsg)))

        val exc = ileQueryService.submit(ileQueryRequest).failed.futureValue
        exc.getMessage must include(exceptionMsg)
      }
    }
  }

  "IleQueryService on fetchResponses" when {

    val searchParameters = SearchParameters(eori = Some(validEori), providerId = Some(validProviderId), conversationId = Some(conversationId))

    "everything works correctly" should {

      "call SubmissionRepository, IleQueryTimeoutCalculator and NotificationRepository in this order" in {

        when(submissionRepository.findBy(any[SearchParameters]))
          .thenReturn(Future.successful(Seq(exampleSubmission(providerId = Some(validProviderId)))))
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[Submission])).thenReturn(false)
        when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(Future.successful(Seq(notificationIleQueryResponse_1)))

        ileQueryService.fetchResponses(searchParameters).futureValue

        val inOrder: InOrder = Mockito.inOrder(submissionRepository, notificationRepository)
        inOrder.verify(submissionRepository, times(1)).findBy(any())
        inOrder.verify(notificationRepository, times(1)).findByConversationIds(any())
      }

      "call SubmissionRepository, passing SearchParameters provided" in {

        when(submissionRepository.findBy(any[SearchParameters]))
          .thenReturn(Future.successful(Seq(exampleSubmission(providerId = Some(validProviderId)))))
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[Submission])).thenReturn(false)
        when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(Future.successful(Seq(notificationIleQueryResponse_1)))

        ileQueryService.fetchResponses(searchParameters).futureValue

        verify(submissionRepository, times(1)).findBy(searchParameters)
      }

      "call IleQueryTimeoutCalculator, passing Submission returned by SubmissionRepository" in {

        val submission = exampleSubmission(providerId = Some(validProviderId))
        when(submissionRepository.findBy(any[SearchParameters])).thenReturn(Future.successful(Seq(submission)))
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[Submission])).thenReturn(false)
        when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(Future.successful(Seq(notificationIleQueryResponse_1)))

        ileQueryService.fetchResponses(searchParameters).futureValue

        verify(ileQueryTimeoutCalculator, times(1)).hasQueryTimedOut(submission)
      }

      "call NotificationRepository, passing Conversation ID provided" in {

        when(submissionRepository.findBy(any[SearchParameters]))
          .thenReturn(Future.successful(Seq(exampleSubmission(providerId = Some(validProviderId)))))
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[Submission])).thenReturn(false)
        when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(Future.successful(Seq(notificationIleQueryResponse_1)))

        ileQueryService.fetchResponses(searchParameters).futureValue

        verify(notificationRepository, times(1)).findByConversationIds(Seq(conversationId))
      }

      "return converted IleQueryResponses returned by NotificationRepository" in {

        when(submissionRepository.findBy(any[SearchParameters]))
          .thenReturn(Future.successful(Seq(exampleSubmission(providerId = Some(validProviderId)))))
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[Submission])).thenReturn(false)
        when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(Future.successful(Seq(notificationIleQueryResponse_1)))

        val result = ileQueryService.fetchResponses(searchParameters).futureValue

        result.isRight mustBe true
        result.right.get.size mustBe 1

        val ileQueryResponseExchange: IleQueryResponseExchange = result.right.get.head
        val expectedIleQueryResponseExchange = IleQueryResponseExchange(
          timestampReceived = notificationIleQueryResponse_1.timestampReceived,
          conversationId = notificationIleQueryResponse_1.conversationId,
          responseType = notificationIleQueryResponse_1.responseType,
          data = SuccessfulResponseExchangeData(IleQueryResponseData())
        )

        ileQueryResponseExchange mustBe expectedIleQueryResponseExchange
      }
    }

    "there is no Submission with given User Identification and Conversation ID" should {

      "return empty Sequence" in {

        when(submissionRepository.findBy(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))

        val result = ileQueryService.fetchResponses(searchParameters).futureValue

        result.isRight mustBe true
        result.right.get.isEmpty mustBe true
      }

      "not call TimeComparator, nor NotificationRepository" in {

        when(submissionRepository.findBy(any[SearchParameters])).thenReturn(Future.successful(Seq.empty))

        ileQueryService.fetchResponses(searchParameters).futureValue

        verifyZeroInteractions(ileQueryTimeoutCalculator)
        verifyZeroInteractions(notificationRepository)
      }
    }

    "IleQueryTimeoutCalculator on hasQueryTimedOut returns true" should {

      "return successful Future with Either.left" in {

        when(submissionRepository.findBy(any[SearchParameters]))
          .thenReturn(Future.successful(Seq(exampleSubmission(providerId = Some(validProviderId)))))
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[Submission])).thenReturn(true)

        val result = ileQueryService.fetchResponses(searchParameters).futureValue

        result.isLeft mustBe true
      }

      "not call NotificationRepository" in {

        when(submissionRepository.findBy(any[SearchParameters]))
          .thenReturn(Future.successful(Seq(exampleSubmission(providerId = Some(validProviderId)))))
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[Submission])).thenReturn(true)

        ileQueryService.fetchResponses(searchParameters).futureValue

        verifyZeroInteractions(notificationRepository)
      }
    }

    "there is no Notification with given Conversation ID" should {
      "return empty Sequence" in {

        when(submissionRepository.findBy(any[SearchParameters]))
          .thenReturn(Future.successful(Seq(exampleSubmission(providerId = Some(validProviderId)))))
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[Submission])).thenReturn(false)
        when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(Future.successful(Seq.empty))

        val result = ileQueryService.fetchResponses(searchParameters).futureValue

        result.isRight mustBe true
        result.right.get.isEmpty mustBe true
      }
    }
  }

}
