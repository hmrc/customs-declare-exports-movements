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

package unit.uk.gov.hmrc.exports.movements.services

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, verifyZeroInteractions, when}
import org.mockito.{ArgumentCaptor, InOrder, Mockito}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.http.Status.{ACCEPTED, BAD_REQUEST}
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import uk.gov.hmrc.exports.movements.models.submissions.Submission.ActionTypes
import uk.gov.hmrc.exports.movements.services.SubmissionService
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import uk.gov.hmrc.http.HeaderCarrier
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import utils.CommonTestData.{conversationId, validEori}
import utils.ConsolidationTestData._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import scala.concurrent.{ExecutionContext, Future}

class SubmissionServiceSpec extends WordSpec with MockitoSugar with ScalaFutures with MustMatchers {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

  private trait Test {
    implicit val hc: HeaderCarrier = mock[HeaderCarrier]
    val customsInventoryLinkingExportsConnectorMock = buildCustomsInventoryLinkingExportsConnectorMock
    val submissionRepositoryMock = buildSubmissionRepositoryMock
    val submissionFactoryMock = buildSubmissionFactoryMock
    val consolidationService = new SubmissionService(
      customsInventoryLinkingExportsConnector = customsInventoryLinkingExportsConnectorMock,
      submissionRepository = submissionRepositoryMock,
      submissionFactory = submissionFactoryMock
    )(ExecutionContext.global)
  }

  "ConsolidationService on submitConsolidationRequest" when {

    "everything works correctly" should {

      "return Either.Right" in new HappyPathSaveTest {

        val submissionResult =
          consolidationService.submitRequest(exampleShutMucrContext).futureValue

        submissionResult must equal(Right((): Unit))
      }

      "call CustomsInventoryLinkingExportsConnector, SubmissionFactory and ConsolidationRepository" in new HappyPathSaveTest {

        consolidationService.submitRequest(exampleShutMucrContext).futureValue

        val inOrder: InOrder =
          Mockito.inOrder(customsInventoryLinkingExportsConnectorMock, submissionFactoryMock, submissionRepositoryMock)
        inOrder.verify(customsInventoryLinkingExportsConnectorMock).sendInventoryLinkingRequest(any(), any())(any())
        inOrder.verify(submissionFactoryMock).buildMovementSubmission(any(), any())
        inOrder.verify(submissionRepositoryMock).insert(any())(any())
      }

      "call CustomsInventoryLinkingExportsConnector with EORI and XML provided" in new HappyPathSaveTest {

        consolidationService.submitRequest(exampleShutMucrContext).futureValue

        verify(customsInventoryLinkingExportsConnectorMock)
          .sendInventoryLinkingRequest(meq(validEori), meq(exampleShutMucrConsolidationRequestXML))(any())
      }

      "call SubmissionFactory with ConversationID and context" in new HappyPathSaveTest {

        consolidationService.submitRequest(exampleShutMucrContext).futureValue

        verify(submissionFactoryMock).buildMovementSubmission(meq(conversationId), meq(exampleShutMucrContext))
      }

      "call ConsolidationRepository with Submission returned from SubmissionFactory" in new HappyPathSaveTest {

        consolidationService.submitRequest(exampleShutMucrContext).futureValue

        val consolidationSubmissionCaptor: ArgumentCaptor[Submission] =
          ArgumentCaptor.forClass(classOf[Submission])
        verify(submissionRepositoryMock).insert(consolidationSubmissionCaptor.capture())(any())
        val actualConsolidationSubmission = consolidationSubmissionCaptor.getValue

        actualConsolidationSubmission.uuid mustNot be(empty)
        actualConsolidationSubmission.eori must equal(validEori)
        actualConsolidationSubmission.conversationId must equal(conversationId)
        actualConsolidationSubmission.ucrBlocks.head.ucr must equal("5GB123456789000-123ABC456DEFIIIII")
      }

    }

    "CustomsInventoryLinkingExportsConnector returns status other than ACCEPTED" should {

      "return Either.Left with proper message" in new Test {
        when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(status = BAD_REQUEST, None)))

        val submissionResult =
          consolidationService.submitRequest(exampleShutMucrContext).futureValue

        submissionResult must equal(Left("Non Accepted status returned by Customs Inventory Linking Exports"))
      }

      "not call SubmissionFactory" in new Test {
        when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(status = BAD_REQUEST, None)))

        consolidationService.submitRequest(exampleShutMucrContext).futureValue

        verifyZeroInteractions(submissionFactoryMock)
      }

      "not call ConsolidationRepository" in new Test {
        when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(status = BAD_REQUEST, None)))

        consolidationService.submitRequest(exampleShutMucrContext).futureValue

        verifyZeroInteractions(submissionRepositoryMock)
      }
    }

    "ConsolidationRepository returns WriteResult with error" should {

      "return Either.Left with the error's message" in new Test {
        when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
          .thenReturn(
            Future.successful(CustomsInventoryLinkingResponse(status = ACCEPTED, conversationId = Some(conversationId)))
          )
        val exceptionMsg = "Test Exception message"
        when(submissionRepositoryMock.insert(any())(any()))
          .thenReturn(Future.failed[WriteResult](new Exception(exceptionMsg) with NoStackTrace))

        val submissionResult =
          consolidationService.submitRequest(exampleShutMucrContext).futureValue

        submissionResult must equal(Left(exceptionMsg))
      }
    }

    trait HappyPathSaveTest extends Test {
      when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
        .thenReturn(
          Future.successful(CustomsInventoryLinkingResponse(status = ACCEPTED, conversationId = Some(conversationId)))
        )

      when(submissionFactoryMock.buildMovementSubmission(any[String], any[SubmissionRequestContext]))
        .thenReturn(
          Submission(
            eori = validEori,
            conversationId = conversationId,
            actionType = ActionTypes.ShutMucr,
            ucrBlocks = Seq(UcrBlock(ucr = "5GB123456789000-123ABC456DEFIIIII", ucrType = "M"))
          )
        )

      when(submissionRepositoryMock.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
    }
  }

}
