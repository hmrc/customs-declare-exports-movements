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
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{ACCEPTED, BAD_REQUEST}
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationType.SHUT_MUCR
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ShutMucr
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, Submission}
import uk.gov.hmrc.exports.movements.services.{ILEMapper, SubmissionService}
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import uk.gov.hmrc.http.HeaderCarrier
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import utils.testdata.CommonTestData._
import utils.testdata.ConsolidationTestData._
import utils.testdata.MovementsTestData.exampleSubmission

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.NoStackTrace

class SubmissionServiceSpec extends WordSpec with MockitoSugar with ScalaFutures with MustMatchers {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

  private trait Test {
    implicit val hc: HeaderCarrier = mock[HeaderCarrier]
    val customsInventoryLinkingExportsConnectorMock = buildCustomsInventoryLinkingExportsConnectorMock
    val submissionRepositoryMock = buildSubmissionRepositoryMock
    val submissionFactoryMock = buildSubmissionFactoryMock
    val wcoMapperMock = mock[ILEMapper]
    val submissionService = new SubmissionService(
      customsInventoryLinkingExportsConnector = customsInventoryLinkingExportsConnectorMock,
      submissionRepository = submissionRepositoryMock,
      submissionFactory = submissionFactoryMock,
      wcoMapperMock
    )(ExecutionContext.global)
  }

  val exampleShutMucrRequest: Consolidation = Consolidation(SHUT_MUCR, Some("mucr"), None, ShutMucr)

  "SubmissionService on submitRequest" when {

    "everything works correctly" should {

      "return Either.Right" in new HappyPathSaveTest {
        submissionService.submitRequest(exampleShutMucrContext).futureValue
      }

      "return Either.right for submit consolidation request" in new HappyPathSaveTest {

        submissionService.submitConsolidation(validEori, exampleShutMucrRequest).futureValue
      }

      "call CustomsInventoryLinkingExportsConnector, SubmissionFactory and SubmissionRepository" in new HappyPathSaveTest {

        submissionService.submitRequest(exampleShutMucrContext).futureValue

        val inOrder: InOrder =
          Mockito.inOrder(customsInventoryLinkingExportsConnectorMock, submissionFactoryMock, submissionRepositoryMock)
        inOrder.verify(customsInventoryLinkingExportsConnectorMock).sendInventoryLinkingRequest(any(), any())(any())
        inOrder.verify(submissionFactoryMock).buildMovementSubmission(any(), any())
        inOrder.verify(submissionRepositoryMock).insert(any())(any())
      }

      "call CustomsInventoryLinkingExportsConnector, passing EORI and XML provided" in new HappyPathSaveTest {

        submissionService.submitRequest(exampleShutMucrContext).futureValue

        verify(customsInventoryLinkingExportsConnectorMock)
          .sendInventoryLinkingRequest(meq(validEori), meq(exampleShutMucrConsolidationRequestXML))(any())
      }

      "call SubmissionFactory, passing ConversationID and context" in new HappyPathSaveTest {

        submissionService.submitRequest(exampleShutMucrContext).futureValue

        verify(submissionFactoryMock).buildMovementSubmission(meq(conversationId), meq(exampleShutMucrContext))
      }

      "call SubmissionRepository, passing Submission returned from SubmissionFactory" in new HappyPathSaveTest {

        submissionService.submitRequest(exampleShutMucrContext).futureValue

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

        a[CustomsInventoryLinkingUpstreamException] mustBe thrownBy {
          Await.result(submissionService.submitRequest(exampleShutMucrContext), defaultPatience.timeout)
        }
      }

      "not call SubmissionFactory" in new Test {
        when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(status = BAD_REQUEST, None)))

        Await.ready(submissionService.submitRequest(exampleShutMucrContext), defaultPatience.timeout)

        verifyZeroInteractions(submissionFactoryMock)
      }

      "not call SubmissionRepository" in new Test {
        when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
          .thenReturn(Future.successful(CustomsInventoryLinkingResponse(status = BAD_REQUEST, None)))

        Await.ready(submissionService.submitRequest(exampleShutMucrContext), defaultPatience.timeout)

        verifyZeroInteractions(submissionRepositoryMock)
      }
    }

    "SubmissionRepository returns WriteResult with error" should {

      "return Either.Left with the error's message" in new Test {
        when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
          .thenReturn(
            Future.successful(CustomsInventoryLinkingResponse(status = ACCEPTED, conversationId = Some(conversationId)))
          )
        val exceptionMsg = "Test Exception message"
        private val exception = new Exception(exceptionMsg) with NoStackTrace
        when(submissionRepositoryMock.insert(any())(any())).thenReturn(Future.failed[WriteResult](exception))

        an[Exception] mustBe thrownBy {
          submissionService.submitRequest(exampleShutMucrContext).futureValue
        }
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
            actionType = ActionType.ShutMucr,
            ucrBlocks = Seq(UcrBlock(ucr = "5GB123456789000-123ABC456DEFIIIII", ucrType = "M"))
          )
        )

      when(submissionRepositoryMock.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
    }
  }

  "SubmissionService on getSubmissionsByEori" should {

    "call SubmissionRepository, passing conversationId provided" in new Test {

      submissionService.getSubmissionsByEori(validEori).futureValue

      verify(submissionRepositoryMock).findByEori(meq(validEori))
    }

    "return result of calling SubmissionRepository" in new Test {
      val expectedSubmissions = Seq(
        exampleSubmission(),
        exampleSubmission(conversationId = conversationId_2),
        exampleSubmission(conversationId = conversationId_3),
        exampleSubmission(conversationId = conversationId_4)
      )
      when(submissionRepositoryMock.findByEori(any[String])).thenReturn(Future.successful(expectedSubmissions))

      val result: Seq[Submission] = submissionService.getSubmissionsByEori(validEori).futureValue

      result.length must equal(expectedSubmissions.length)
      result must equal(expectedSubmissions)
    }
  }

  "SubmissionService on getSubmissionByConversationId" should {

    "call SubmissionRepository, passing conversationId provided" in new Test {

      submissionService.getSubmissionByConversationId(conversationId).futureValue

      verify(submissionRepositoryMock).findByConversationId(meq(conversationId))
    }

    "return result of calling SubmissionRepository" in new Test {
      val expectedSubmission = exampleSubmission()
      when(submissionRepositoryMock.findByConversationId(any[String]))
        .thenReturn(Future.successful(Some(expectedSubmission)))

      val result: Option[Submission] = submissionService.getSubmissionByConversationId(conversationId).futureValue

      result must be(defined)
      result.get must equal(expectedSubmission)
    }
  }

}
