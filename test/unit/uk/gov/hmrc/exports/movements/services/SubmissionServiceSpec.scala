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

package unit.uk.gov.hmrc.exports.movements.services

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.common.UcrType.Ducr
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.{ConsolidationType, MovementType}
import uk.gov.hmrc.exports.movements.models.submissions.{Submission, SubmissionFactory}
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.exports.movements.services.{IleMapper, SubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import testdata.CommonTestData._
import testdata.ConsolidationTestData._
import testdata.MovementsTestData._

import scala.concurrent.{ExecutionContext, Future}

class SubmissionServiceSpec extends WordSpec with MockitoSugar with ScalaFutures with MustMatchers with OptionValues {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

  private trait Test {
    implicit val hc: HeaderCarrier = mock[HeaderCarrier]
    val customsInventoryLinkingExportsConnectorMock = buildCustomsInventoryLinkingExportsConnectorMock
    val submissionRepositoryMock = buildSubmissionRepositoryMock
    val submissionFactoryMock = mock[SubmissionFactory]
    val wcoMapperMock = mock[IleMapper]
    val submissionService =
      new SubmissionService(customsInventoryLinkingExportsConnectorMock, submissionRepositoryMock, submissionFactoryMock, wcoMapperMock)(
        ExecutionContext.global
      )
  }

  "SubmissionService on submitMovement" should {

    "successfully submit movement" in new Test {
      val arrivalSubmission =
        Submission(
          eori = validEori,
          providerId = Some(validProviderId),
          conversationId = conversationId,
          ucrBlocks = Seq(UcrBlock(ucr = ucr, ucrType = Ducr.codeValue)),
          actionType = MovementType.Arrival
        )

      when(wcoMapperMock.buildInventoryLinkingMovementRequestXml(any())).thenReturn(exampleArrivalRequestXML("123"))
      when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))
      when(submissionFactoryMock.buildMovementSubmission(any(), any(), any(), any(), any()))
        .thenReturn(arrivalSubmission)
      when(submissionRepositoryMock.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))

      submissionService.submit(exampleArrivalRequest).futureValue

      verify(wcoMapperMock).buildInventoryLinkingMovementRequestXml(meq(exampleArrivalRequest))
      verify(customsInventoryLinkingExportsConnectorMock)
        .submit(meq(exampleArrivalRequest), meq(exampleArrivalRequestXML("123")))(any())
      verify(submissionFactoryMock).buildMovementSubmission(
        meq(validEori),
        meq(Some(validProviderId)),
        meq(conversationId),
        meq(exampleArrivalRequestXML("123")),
        meq(MovementType.Arrival)
      )
      verify(submissionRepositoryMock).insert(meq(arrivalSubmission))(any())
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

      val shutMucrSubmission =
        Submission(
          eori = validEori,
          providerId = Some(validProviderId),
          conversationId = conversationId,
          ucrBlocks = Seq.empty,
          actionType = ConsolidationType.ShutMucr
        )

      when(wcoMapperMock.buildConsolidationXml(any())).thenReturn(exampleShutMucrConsolidationRequestXML)
      when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))
      when(submissionFactoryMock.buildConsolidationSubmission(any(), any(), any(), any(), any()))
        .thenReturn(shutMucrSubmission)
      when(submissionRepositoryMock.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultSuccess))

      submissionService.submit(shutMucrRequest).futureValue

      verify(wcoMapperMock).buildConsolidationXml(meq(shutMucrRequest))
      verify(customsInventoryLinkingExportsConnectorMock)
        .submit(meq(shutMucrRequest), meq(exampleShutMucrConsolidationRequestXML))(any())
      verify(submissionFactoryMock).buildConsolidationSubmission(
        meq(validEori),
        meq(Some(validProviderId)),
        meq(conversationId),
        meq(exampleShutMucrConsolidationRequestXML),
        meq(ConsolidationType.ShutMucr)
      )
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

      verify(submissionRepositoryMock).findBy(meq(searchParameters))
    }

    "return result of calling SubmissionRepository" in new Test {

      val storedSubmissions = Seq(
        exampleSubmission(),
        exampleSubmission(conversationId = conversationId_2),
        exampleSubmission(conversationId = conversationId_3),
        exampleSubmission(conversationId = conversationId_4)
      )
      when(submissionRepositoryMock.findBy(any[SearchParameters])).thenReturn(Future.successful(storedSubmissions))

      val result: Seq[Submission] = submissionService.getSubmissions(SearchParameters()).futureValue

      result.length must equal(storedSubmissions.length)
      result must equal(storedSubmissions)
    }
  }

  "SubmissionService on getSingleSubmission" should {

    "call SubmissionRepository, passing query parameters provided" in new Test {

      val searchParameters = SearchParameters(eori = Some(validEori), providerId = Some(validProviderId), conversationId = Some(conversationId))

      submissionService.getSingleSubmission(searchParameters)

      verify(submissionRepositoryMock).findBy(meq(searchParameters))
    }

    "return result of calling SubmissionRepository" in new Test {

      val storedSubmission = exampleSubmission()
      when(submissionRepositoryMock.findBy(any[SearchParameters])).thenReturn(Future.successful(Seq(storedSubmission)))

      val result: Option[Submission] = submissionService.getSingleSubmission(SearchParameters()).futureValue

      result.value mustBe storedSubmission
    }
  }

}
