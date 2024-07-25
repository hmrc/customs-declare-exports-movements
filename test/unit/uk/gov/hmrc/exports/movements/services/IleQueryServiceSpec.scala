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

package uk.gov.hmrc.exports.movements.services

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, verifyZeroInteractions, when}
import org.mockito.{ArgumentCaptor, InOrder, Mockito}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleIleQuerySubmission
import testdata.notifications.NotificationTestData.ileQueryResponse_1
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.misc.IleQueryTimeoutCalculator
import uk.gov.hmrc.exports.movements.models.movements.IleQueryRequest
import uk.gov.hmrc.exports.movements.models.notifications.exchange.IleQueryResponseExchange
import uk.gov.hmrc.exports.movements.models.notifications.exchange.IleQueryResponseExchangeData.SuccessfulResponseExchangeData
import uk.gov.hmrc.exports.movements.models.notifications.queries.IleQueryResponseData
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.IleQuerySubmission
import uk.gov.hmrc.exports.movements.models.{CustomsInventoryLinkingResponse, UserIdentification}
import uk.gov.hmrc.exports.movements.repositories._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Clock
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class IleQueryServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with BeforeAndAfterEach with IntegrationPatience {

  implicit private val hc: HeaderCarrier = mock[HeaderCarrier]

  private val ileQuerySubmissionRepository = mock[IleQuerySubmissionRepository]
  private val ileQueryResponseRepository = mock[IleQueryResponseRepository]
  private val ileConnector = mock[CustomsInventoryLinkingExportsConnector]
  private val ileQueryTimeoutCalculator = mock[IleQueryTimeoutCalculator]
  private val notificationRepository = mock[NotificationRepository]

  private val ileQuerySubmission = Right(exampleIleQuerySubmission())

  private val ileMapper = new IleMapper(Clock.systemUTC())

  private val ileQueryService = new IleQueryService(
    ileMapper,
    ileQuerySubmissionRepository,
    ileQueryResponseRepository,
    ileConnector,
    ileQueryTimeoutCalculator,
    notificationRepository
  )(global)

  private val response = CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))
  private val emptyResponse = Future.successful(List.empty)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(ileQuerySubmissionRepository, ileConnector, ileQueryResponseRepository, ileQueryTimeoutCalculator, notificationRepository)

    when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any())).thenReturn(Future.successful(response))
    when(ileQuerySubmissionRepository.insertOne(any[IleQuerySubmission])).thenReturn(Future.successful(ileQuerySubmission))
    when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(emptyResponse)
    when(ileQueryResponseRepository.findByConversationIds(any[Seq[String]])).thenReturn(emptyResponse)
    when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(false)
    when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(emptyResponse)
  }

  "IleQueryService on submit" when {
    val ileQueryRequest = IleQueryRequest(validEori, Some(validProviderId), UcrBlock(ucr = ucr, ucrType = "D"))

    "everything works correctly" should {

      "call IleConnector and IleQueryRepository in this order" in {
        ileQueryService.submit(ileQueryRequest).futureValue

        val inOrder: InOrder = Mockito.inOrder(ileConnector, ileQuerySubmissionRepository)
        inOrder.verify(ileConnector).submit(any(), any())(any())
        inOrder.verify(ileQuerySubmissionRepository).insertOne(any())
      }

      "call IleConnector once, passing IleQueryRequest and request xml returned from ILEMapper" in {
        ileQueryService.submit(ileQueryRequest).futureValue

        val queryXml = ileMapper.buildIleQuery(ileQueryRequest.ucrBlock)

        verify(ileConnector).submit(meq(ileQueryRequest), meq(queryXml))(any())
      }

      "call IleQueryRepository once, passing constructed IleQuerySubmission with Conversation ID returned from IleConnector" in {
        when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any())).thenReturn(Future.successful(response))

        ileQueryService.submit(ileQueryRequest).futureValue

        val ileQuerySubmissionCaptor: ArgumentCaptor[IleQuerySubmission] = ArgumentCaptor.forClass(classOf[IleQuerySubmission])
        verify(ileQuerySubmissionRepository).insertOne(ileQuerySubmissionCaptor.capture())
        val actualIleQuerySubmission = ileQuerySubmissionCaptor.getValue

        actualIleQuerySubmission.eori mustBe validEori
        actualIleQuerySubmission.providerId mustBe defined
        actualIleQuerySubmission.providerId.get mustBe validProviderId
        actualIleQuerySubmission.conversationId mustBe conversationId
        actualIleQuerySubmission.ucrBlock mustBe UcrBlock(ucr = ucr, ucrType = "D")
      }

      "return successful Future with Conversation ID returned from IleConnector" in {
        when(ileConnector.submit(any[UserIdentification], any[NodeSeq])(any())).thenReturn(Future.successful(response))
        ileQueryService.submit(ileQueryRequest).futureValue mustBe conversationId
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

        verifyZeroInteractions(ileQuerySubmissionRepository)
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

        verifyZeroInteractions(ileQuerySubmissionRepository)
      }
    }
  }

  "IleQueryService on fetchResponses" when {
    val searchParameters = SearchParameters(eori = Some(validEori), providerId = Some(validProviderId), conversationId = Some(conversationId))

    val submission = exampleIleQuerySubmission(providerId = Some(validProviderId))
    val ileQuerySubmissions = Future.successful(Seq(submission))
    val ileQueryResponse1 = Future.successful(Seq(ileQueryResponse_1))

    "everything works correctly" should {

      "call SubmissionRepository, IleQueryTimeoutCalculator and IleQueryResponseRepository in this order" in {
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(false)
        when(ileQueryResponseRepository.findByConversationIds(any[Seq[String]])).thenReturn(ileQueryResponse1)

        ileQueryService.fetchResponses(searchParameters).futureValue

        val inOrder: InOrder = Mockito.inOrder(ileQuerySubmissionRepository, ileQueryResponseRepository)
        inOrder.verify(ileQuerySubmissionRepository).findAll(any())
        inOrder.verify(ileQueryResponseRepository).findByConversationIds(any())
      }

      "call SubmissionRepository, IleQueryTimeoutCalculator, IleQueryResponseRepository and NotificationRepository in this order" when {
        "the lookup in IleQueryResponseRepository returns no Notification" in {
          when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)
          when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(false)

          ileQueryService.fetchResponses(searchParameters).futureValue

          val inOrder: InOrder = Mockito.inOrder(ileQuerySubmissionRepository, ileQueryResponseRepository, notificationRepository)
          inOrder.verify(ileQuerySubmissionRepository).findAll(any())
          inOrder.verify(ileQueryResponseRepository).findByConversationIds(any())
          inOrder.verify(notificationRepository).findByConversationIds(any())
        }
      }

      "call SubmissionRepository, passing SearchParameters provided" in {
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(false)
        when(ileQueryResponseRepository.findByConversationIds(any[Seq[String]])).thenReturn(ileQueryResponse1)

        ileQueryService.fetchResponses(searchParameters).futureValue

        verify(ileQuerySubmissionRepository).findAll(searchParameters)
      }

      "call IleQueryTimeoutCalculator, passing Submission returned by SubmissionRepository" in {
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(false)
        when(ileQueryResponseRepository.findByConversationIds(any[Seq[String]])).thenReturn(ileQueryResponse1)

        ileQueryService.fetchResponses(searchParameters).futureValue

        verify(ileQueryTimeoutCalculator).hasQueryTimedOut(submission)
      }

      "call IleQueryResponseRepository, passing Conversation ID provided" in {
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(false)
        when(ileQueryResponseRepository.findByConversationIds(any[Seq[String]])).thenReturn(ileQueryResponse1)

        ileQueryService.fetchResponses(searchParameters).futureValue

        verify(ileQueryResponseRepository).findByConversationIds(Seq(conversationId))
      }

      "call NotificationRepository, passing Conversation ID provided" in {
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(false)

        ileQueryService.fetchResponses(searchParameters).futureValue

        verify(ileQueryResponseRepository).findByConversationIds(Seq(conversationId))
        verify(notificationRepository).findByConversationIds(Seq(conversationId))
      }

      "return converted IleQueryResponses returned by IleQueryResponseRepository" in {
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(false)
        when(ileQueryResponseRepository.findByConversationIds(any[Seq[String]])).thenReturn(ileQueryResponse1)

        val result = ileQueryService.fetchResponses(searchParameters).futureValue

        result.isRight mustBe true
        result.toOption.get.size mustBe 1

        val ileQueryResponseExchange: IleQueryResponseExchange = result.toOption.get.head
        val expectedIleQueryResponseExchange = IleQueryResponseExchange(
          timestampReceived = ileQueryResponse_1.timestampReceived,
          conversationId = ileQueryResponse_1.conversationId,
          responseType = ileQueryResponse_1.data.get.responseType,
          data = Some(SuccessfulResponseExchangeData(IleQueryResponseData(responseType = ileQueryResponse_1.data.get.responseType)))
        )

        ileQueryResponseExchange mustBe expectedIleQueryResponseExchange
      }

      "return converted IleQueryResponses returned by NotificationRepository" in {
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(false)
        when(notificationRepository.findByConversationIds(any[Seq[String]])).thenReturn(ileQueryResponse1)

        val result = ileQueryService.fetchResponses(searchParameters).futureValue

        result.isRight mustBe true
        result.toOption.get.size mustBe 1

        val ileQueryResponseExchange: IleQueryResponseExchange = result.toOption.get.head
        val expectedIleQueryResponseExchange = IleQueryResponseExchange(
          timestampReceived = ileQueryResponse_1.timestampReceived,
          conversationId = ileQueryResponse_1.conversationId,
          responseType = ileQueryResponse_1.data.get.responseType,
          data = Some(SuccessfulResponseExchangeData(IleQueryResponseData(responseType = ileQueryResponse_1.data.get.responseType)))
        )

        ileQueryResponseExchange mustBe expectedIleQueryResponseExchange
      }
    }

    "there is no Submission with given User Identification and Conversation ID" should {

      "return empty Sequence" in {
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(emptyResponse)

        val result = ileQueryService.fetchResponses(searchParameters).futureValue

        result.isRight mustBe true
        result.toOption.get.isEmpty mustBe true
      }

      "not call TimeComparator, nor IleQueryResponseRepository or NotificationRepository" in {
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(emptyResponse)

        ileQueryService.fetchResponses(searchParameters).futureValue

        verifyZeroInteractions(ileQueryTimeoutCalculator)
        verifyZeroInteractions(ileQueryResponseRepository)
        verifyZeroInteractions(notificationRepository)
      }
    }

    "IleQueryTimeoutCalculator on hasQueryTimedOut returns true" should {

      "return successful Future with Either.left" in {
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(true)
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)

        val result = ileQueryService.fetchResponses(searchParameters).futureValue

        result.isLeft mustBe true
      }

      "not call IleQueryResponseRepository or NotificationRepository" in {
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(true)
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)

        ileQueryService.fetchResponses(searchParameters).futureValue

        verifyZeroInteractions(ileQueryResponseRepository)
        verifyZeroInteractions(notificationRepository)
      }
    }

    "there is no Notification with given Conversation ID" should {
      "return empty Sequence" in {
        when(ileQuerySubmissionRepository.findAll(any[SearchParameters])).thenReturn(ileQuerySubmissions)
        when(ileQueryTimeoutCalculator.hasQueryTimedOut(any[IleQuerySubmission])).thenReturn(false)
        when(ileQueryResponseRepository.findByConversationIds(any[Seq[String]])).thenReturn(emptyResponse)

        val result = ileQueryService.fetchResponses(searchParameters).futureValue

        result.isRight mustBe true
        result.toOption.get.isEmpty mustBe true
      }
    }
  }
}
