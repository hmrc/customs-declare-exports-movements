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
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationType.SHUT_MUCR
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, Submission, SubmissionFactory}
import uk.gov.hmrc.exports.movements.services.{ILEMapper, SubmissionService, WCOMapper}
import uk.gov.hmrc.http.HeaderCarrier
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import utils.testdata.CommonTestData._
import utils.testdata.ConsolidationTestData._
import utils.testdata.MovementsTestData._

import scala.concurrent.{ExecutionContext, Future}

class SubmissionServiceSpec extends WordSpec with MockitoSugar with ScalaFutures with MustMatchers {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

  private trait Test {
    implicit val hc: HeaderCarrier = mock[HeaderCarrier]
    val customsInventoryLinkingExportsConnectorMock = buildCustomsInventoryLinkingExportsConnectorMock
    val submissionRepositoryMock = buildSubmissionRepositoryMock
    val submissionFactoryMock = mock[SubmissionFactory]
    val ileMapperMock = mock[ILEMapper]
    val wcoMapperMock = mock[WCOMapper]
    val submissionService = new SubmissionService(
      customsInventoryLinkingExportsConnectorMock,
      submissionRepositoryMock,
      submissionFactoryMock,
      ileMapperMock,
      wcoMapperMock
    )(ExecutionContext.global)
  }

  "SubmissionService on submitMovement" should {

    "successfully submit movement" in new Test {
      val arrivalSubmission =
        Submission(eori = validEori, conversationId = conversationId, ucrBlocks = Seq(UcrBlock(ucr, "D")), actionType = ActionType.Arrival)

      when(wcoMapperMock.generateInventoryLinkingMovementRequestXml(any())).thenReturn(exampleArrivalRequestXML)
      when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))
      when(submissionFactoryMock.buildMovementSubmission(any(), any(), any(), any()))
        .thenReturn(arrivalSubmission)
      when(submissionRepositoryMock.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))

      submissionService.submitMovement(validEori, exampleArrivalRequest).futureValue

      verify(wcoMapperMock).generateInventoryLinkingMovementRequestXml(meq(exampleArrivalRequest))
      verify(customsInventoryLinkingExportsConnectorMock)
        .sendInventoryLinkingRequest(meq(validEori), meq(exampleArrivalRequestXML))(any())
      verify(submissionFactoryMock).buildMovementSubmission(
        meq(validEori),
        meq(conversationId),
        meq(exampleArrivalRequestXML),
        meq(exampleArrivalRequest)
      )
      verify(submissionRepositoryMock).insert(meq(arrivalSubmission))(any())
    }

    "return exception when submission failed" in new Test {
      when(wcoMapperMock.generateInventoryLinkingMovementRequestXml(any())).thenReturn(exampleArrivalRequestXML)
      when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(BAD_REQUEST, None)))

      intercept[CustomsInventoryLinkingUpstreamException] {
        await(submissionService.submitMovement(validEori, exampleArrivalRequest))
      }
    }
  }

  "SubmissionService on submitConsolidation" should {

    "successfully submit consolidation" in new Test {

      val shutMucrSubmission = Submission(eori = validEori, conversationId = conversationId, ucrBlocks = Seq.empty, actionType = ActionType.ShutMucr)

      when(ileMapperMock.generateConsolidationXml(any())).thenReturn(exampleShutMucrConsolidationRequestXML)
      when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))
      when(submissionFactoryMock.buildConsolidationSubmission(any(), any(), any(), any()))
        .thenReturn(shutMucrSubmission)
      when(submissionRepositoryMock.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))

      submissionService.submitConsolidation(validEori, shutMucrRequest).futureValue

      verify(ileMapperMock).generateConsolidationXml(meq(shutMucrRequest))
      verify(customsInventoryLinkingExportsConnectorMock)
        .sendInventoryLinkingRequest(meq(validEori), meq(exampleShutMucrConsolidationRequestXML))(any())
      verify(submissionFactoryMock).buildConsolidationSubmission(
        meq(validEori),
        meq(conversationId),
        meq(exampleShutMucrConsolidationRequestXML),
        meq(SHUT_MUCR)
      )
    }

    "return exception when submission failed" in new Test {
      when(ileMapperMock.generateConsolidationXml(any())).thenReturn(exampleShutMucrConsolidationRequestXML)
      when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(BAD_REQUEST, None)))

      intercept[CustomsInventoryLinkingUpstreamException] {
        await(submissionService.submitConsolidation(validEori, shutMucrRequest))
      }
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
