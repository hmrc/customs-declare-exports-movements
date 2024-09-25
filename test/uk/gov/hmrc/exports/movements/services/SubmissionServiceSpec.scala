/*
 * Copyright 2024 HM Revenue & Customs
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
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import utils.testdata.CommonTestData._
import utils.testdata.ConsolidationTestData._
import utils.testdata.MovementsTestData._
import uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.UcrType.Ducr
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.MovementType
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Clock
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Node

class SubmissionServiceSpec extends AnyWordSpec with ScalaFutures with Matchers with OptionValues {

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(10, Millis))

  private trait Test {
    implicit val hc: HeaderCarrier = mock[HeaderCarrier]
    val customsInventoryLinkingExportsConnectorMock = buildCustomsInventoryLinkingExportsConnectorMock
    val submissionRepositoryMock = buildSubmissionRepositoryMock

    val wcoMapper = new IleMapper(Clock.systemUTC())

    val submissionService = new SubmissionService(customsInventoryLinkingExportsConnectorMock, submissionRepositoryMock, wcoMapper)(
      ExecutionContext.global
    )
  }

  "SubmissionService on submitMovement" should {

    "successfully submit movement" in new Test {
      when(submissionRepositoryMock.insertOne(any())).thenReturn(Future.successful(Right(exampleSubmission())))
      when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))

      submissionService.submit(exampleArrivalRequest).futureValue

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
      when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(BAD_REQUEST, None)))

      intercept[CustomsInventoryLinkingUpstreamException] {
        await(submissionService.submit(exampleArrivalRequest))
      }
    }
  }

  "SubmissionService on submitConsolidation" should {

    "successfully submit consolidation" in new Test {
      when(submissionRepositoryMock.insertOne(any())).thenReturn(Future.successful(Right(exampleSubmission())))
      when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))))

      submissionService.submit(shutMucrRequest).futureValue

      val shutMucrConsolidationRequestXML: Node =
        scala.xml.Utility.trim {
          <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
            <messageCode>CST</messageCode>
            <masterUCR>7GB123456789000-123ABC456DEFQWERT</masterUCR>
          </inventoryLinkingConsolidationRequest>
        }

      verify(customsInventoryLinkingExportsConnectorMock)
        .submit(meq(shutMucrRequest), meq(shutMucrConsolidationRequestXML))(any())
    }

    "return exception when submission failed" in new Test {
      when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
        .thenReturn(Future.successful(CustomsInventoryLinkingResponse(BAD_REQUEST, None)))

      val exc = intercept[CustomsInventoryLinkingUpstreamException] {
        await(submissionService.submit(shutMucrRequest))
      }

      exc.getStatus() mustBe BAD_REQUEST
      exc.getMessage must include("Non Accepted status returned by Customs Inventory Linking Exports")
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
