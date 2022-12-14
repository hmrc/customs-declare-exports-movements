/*
 * Copyright 2022 HM Revenue & Customs
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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.Helpers._
import testdata.CommonTestData._
import testdata.ConsolidationTestData._
import testdata.MovementsTestData._
import uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.common.UcrType.Ducr
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.MovementType
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubmissionServiceSpec extends AnyWordSpec with ScalaFutures with Matchers with OptionValues {

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

  private trait Test {
    implicit val hc: HeaderCarrier = mock[HeaderCarrier]
    val customsInventoryLinkingExportsConnectorMock = buildCustomsInventoryLinkingExportsConnectorMock
    val submissionRepositoryMock = buildSubmissionRepositoryMock
    val wcoMapperMock = mock[IleMapper]
    val submissionService =
      new SubmissionService(customsInventoryLinkingExportsConnectorMock, submissionRepositoryMock, wcoMapperMock)(ExecutionContext.global)
  }

  "SubmissionService on submitMovement" should {

    "successfully submit movement" in new Test {
      when(wcoMapperMock.buildInventoryLinkingMovementRequestXml(any())).thenReturn(exampleArrivalRequestXML("123"))
      when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))
      when(submissionRepositoryMock.insertOne(any())).thenReturn(Future.successful(Right(exampleSubmission())))

      submissionService.submit(exampleArrivalRequest).futureValue

      verify(wcoMapperMock).buildInventoryLinkingMovementRequestXml(meq(exampleArrivalRequest))
      verify(customsInventoryLinkingExportsConnectorMock)
        .submit(meq(exampleArrivalRequest), meq(exampleArrivalRequestXML("123")))(any())

      val submissionCaptor: ArgumentCaptor[Submission] = ArgumentCaptor.forClass(classOf[Submission])
      verify(submissionRepositoryMock).insertOne(submissionCaptor.capture())

      val arrivalSubmission = submissionCaptor.getValue
      arrivalSubmission mustBe Submission(
        uuid = arrivalSubmission.uuid,
        eori = validEori,
        providerId = Some(validProviderId),
        conversationId = conversationId,
        ucrBlocks = Seq(UcrBlock(ucr = ucr, ucrType = Ducr.codeValue)),
        actionType = MovementType.Arrival,
        requestTimestamp = arrivalSubmission.requestTimestamp
      )
    }

    "return exception when submission failed" in new Test {
      when(wcoMapperMock.buildInventoryLinkingMovementRequestXml(any())).thenReturn(exampleArrivalRequestXML("123"))
      when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(BAD_REQUEST, None)))

      intercept[CustomsInventoryLinkingUpstreamException] {
        await(submissionService.submit(exampleArrivalRequest))
      }
    }
  }

  "SubmissionService on submitConsolidation" should {

    "successfully submit consolidation" in new Test {
      when(wcoMapperMock.buildConsolidationXml(any())).thenReturn(exampleShutMucrConsolidationRequestXML)
      when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))
      when(submissionRepositoryMock.insertOne(any())).thenReturn(Future.successful(Right(exampleSubmission())))

      submissionService.submit(shutMucrRequest).futureValue

      verify(wcoMapperMock).buildConsolidationXml(meq(shutMucrRequest))
      verify(customsInventoryLinkingExportsConnectorMock)
        .submit(meq(shutMucrRequest), meq(exampleShutMucrConsolidationRequestXML))(any())
    }

    "return exception when submission failed" in new Test {
      when(wcoMapperMock.buildConsolidationXml(any())).thenReturn(exampleShutMucrConsolidationRequestXML)
      when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(BAD_REQUEST, None)))

      intercept[CustomsInventoryLinkingUpstreamException] {
        await(submissionService.submit(shutMucrRequest))
      }
    }
  }

  "SubmissionService on getSubmissions" should {

    "call SubmissionRepository, passing query parameters provided" in new Test {
      val searchParameters = SearchParameters(eori = Some(validEori), providerId = Some(validProviderId), conversationId = Some(conversationId))
      submissionService.getSubmissions(searchParameters)

      verify(submissionRepositoryMock).findAll(meq(searchParameters))
    }

    "return result of calling SubmissionRepository" in new Test {
      val storedSubmissions = Seq(
        exampleSubmission(),
        exampleSubmission(conversationId = conversationId_2),
        exampleSubmission(conversationId = conversationId_3),
        exampleSubmission(conversationId = conversationId_4)
      )
      when(submissionRepositoryMock.findAll(any[SearchParameters])).thenReturn(Future.successful(storedSubmissions))

      val result: Seq[Submission] = submissionService.getSubmissions(SearchParameters()).futureValue

      result.length must equal(storedSubmissions.length)
      result must equal(storedSubmissions)
    }
  }

  "SubmissionService on getSingleSubmission" should {

    "call SubmissionRepository, passing query parameters provided" in new Test {
      val searchParameters = SearchParameters(eori = Some(validEori), providerId = Some(validProviderId), conversationId = Some(conversationId))

      submissionService.getSingleSubmission(searchParameters)

      verify(submissionRepositoryMock).findAll(meq(searchParameters))
    }

    "return result of calling SubmissionRepository" in new Test {
      val storedSubmission = exampleSubmission()
      when(submissionRepositoryMock.findAll(any[SearchParameters])).thenReturn(Future.successful(Seq(storedSubmission)))

      val result: Option[Submission] = submissionService.getSingleSubmission(SearchParameters()).futureValue

      result.value mustBe storedSubmission
    }
  }
}
