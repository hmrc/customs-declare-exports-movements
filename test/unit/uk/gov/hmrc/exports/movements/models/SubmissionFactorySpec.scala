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

package unit.uk.gov.hmrc.exports.movements.models

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.exports.movements.models.common.UcrType.{Ducr, Mucr}
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.{ConsolidationType, MovementType}
import uk.gov.hmrc.exports.movements.models.submissions.{Submission, SubmissionFactory}
import uk.gov.hmrc.exports.movements.services.UcrBlockBuilder
import utils.testdata.CommonTestData.{conversationId, _}
import utils.testdata.ConsolidationTestData._
import utils.testdata.MovementsTestData.{exampleArrivalRequestXML, exampleDepartureRequestXML}

import scala.xml.NodeSeq

class SubmissionFactorySpec extends WordSpec with MustMatchers with MockitoSugar with BeforeAndAfterEach {

  private val ucrBlockBuilder = mock[UcrBlockBuilder]
  private def submissionFactory = new SubmissionFactory(ucrBlockBuilder)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(ucrBlockBuilder)
    when(ucrBlockBuilder.extractUcrBlocksFrom(any[NodeSeq])).thenReturn(Seq.empty)
  }

  override protected def afterEach(): Unit = {
    reset(ucrBlockBuilder)

    super.afterEach()
  }

  "SubmissionFactory on buildMovementSubmission" should {

    "call UcrBlockBuilder" in {

      submissionFactory.buildMovementSubmission(
        validEori,
        Some(validProviderId),
        conversationId,
        exampleArrivalRequestXML("123"),
        MovementType.Arrival
      )

      verify(ucrBlockBuilder).extractUcrBlocksFrom(any[NodeSeq])
    }

    "return Submission with ucrBlocks returned from UcrBlockBuilder" in {

      val testUcrBlocks = Seq(UcrBlock(ucr = ucr_2, ucrType = Mucr.codeValue), UcrBlock(ucr = ucr, ucrType = Ducr.codeValue))
      when(ucrBlockBuilder.extractUcrBlocksFrom(any[NodeSeq])).thenReturn(testUcrBlocks)

      val submission =
        submissionFactory.buildMovementSubmission(validEori, None, conversationId, exampleArrivalRequestXML("123"), MovementType.Arrival)

      submission.ucrBlocks mustBe testUcrBlocks
    }

    "return Submission with provided data" when {

      "provided with Arrival request" in {

        val submission =
          submissionFactory.buildMovementSubmission(
            validEori,
            Some(validProviderId),
            conversationId,
            exampleArrivalRequestXML("123"),
            MovementType.Arrival
          )

        val expectedSubmission = Submission(
          eori = validEori,
          providerId = Some(validProviderId),
          conversationId = conversationId,
          actionType = MovementType.Arrival,
          ucrBlocks = Seq.empty
        )

        compareSubmissions(submission, expectedSubmission)
      }

      "provided with Retrospective Arrival request" in {

        val submission =
          submissionFactory.buildMovementSubmission(
            validEori,
            Some(validProviderId),
            conversationId,
            exampleArrivalRequestXML("123"),
            MovementType.RetrospectiveArrival
          )

        val expectedSubmission = Submission(
          eori = validEori,
          providerId = Some(validProviderId),
          conversationId = conversationId,
          actionType = MovementType.RetrospectiveArrival,
          ucrBlocks = Seq.empty
        )

        compareSubmissions(submission, expectedSubmission)
      }

      "provided with Departure request" in {

        val submission =
          submissionFactory.buildMovementSubmission(
            validEori,
            Some(validProviderId),
            conversationId,
            exampleDepartureRequestXML,
            MovementType.Departure
          )

        val expectedSubmission = Submission(
          eori = validEori,
          providerId = Some(validProviderId),
          conversationId = conversationId,
          actionType = MovementType.Departure,
          ucrBlocks = Seq.empty
        )

        compareSubmissions(submission, expectedSubmission)
      }
    }
  }

  "SubmissionFactory on buildConsolidationSubmission" should {

    "call UcrBlockBuilder" in {

      submissionFactory.buildConsolidationSubmission(
        validEori,
        Some(validProviderId),
        conversationId,
        exampleAssociateDucrConsolidationRequestXML,
        ConsolidationType.DucrAssociation
      )

      verify(ucrBlockBuilder).extractUcrBlocksFrom(any[NodeSeq])
    }

    "return Submission with ucrBlocks returned from UcrBlockBuilder" in {

      val testUcrBlocks = Seq(UcrBlock(ucr = ucr_2, ucrType = Mucr.codeValue), UcrBlock(ucr = ucr, ucrType = Ducr.codeValue))
      when(ucrBlockBuilder.extractUcrBlocksFrom(any[NodeSeq])).thenReturn(testUcrBlocks)

      val submission =
        submissionFactory.buildConsolidationSubmission(
          validEori,
          None,
          conversationId,
          exampleAssociateDucrConsolidationRequestXML,
          ConsolidationType.DucrAssociation
        )

      submission.ucrBlocks mustBe testUcrBlocks
    }

    "return Submission with provided data" when {

      "provided with Association Ducr request" in {

        val submission =
          submissionFactory.buildConsolidationSubmission(
            validEori,
            Some(validProviderId),
            conversationId,
            exampleAssociateDucrConsolidationRequestXML,
            ConsolidationType.DucrAssociation
          )

        val expectedSubmission = Submission(
          eori = validEori,
          providerId = Some(validProviderId),
          conversationId = conversationId,
          actionType = ConsolidationType.DucrAssociation,
          ucrBlocks = Seq.empty
        )

        compareSubmissions(submission, expectedSubmission)
      }

      "provided with Association Mucr request" in {

        val submission =
          submissionFactory.buildConsolidationSubmission(
            validEori,
            Some(validProviderId),
            conversationId,
            exampleAssociateMucrConsolidationRequestXML,
            ConsolidationType.MucrAssociation
          )

        val expectedSubmission = Submission(
          eori = validEori,
          providerId = Some(validProviderId),
          conversationId = conversationId,
          actionType = ConsolidationType.MucrAssociation,
          ucrBlocks = Seq.empty
        )

        compareSubmissions(submission, expectedSubmission)
      }

      "provided with Disassociation Ducr request" in {

        val submission =
          submissionFactory.buildConsolidationSubmission(
            validEori,
            Some(validProviderId),
            conversationId,
            exampleDisassociateDucrConsolidationRequestXML,
            ConsolidationType.DucrDisassociation
          )

        val expectedSubmission = Submission(
          eori = validEori,
          providerId = Some(validProviderId),
          conversationId = conversationId,
          actionType = ConsolidationType.DucrDisassociation,
          ucrBlocks = Seq.empty
        )

        compareSubmissions(submission, expectedSubmission)
      }

      "provided with Disassociation Mucr request" in {

        val submission =
          submissionFactory.buildConsolidationSubmission(
            validEori,
            Some(validProviderId),
            conversationId,
            exampleDisassociateMucrConsolidationRequestXML,
            ConsolidationType.MucrDisassociation
          )

        val expectedSubmission = Submission(
          eori = validEori,
          providerId = Some(validProviderId),
          conversationId = conversationId,
          actionType = ConsolidationType.MucrDisassociation,
          ucrBlocks = Seq.empty
        )

        compareSubmissions(submission, expectedSubmission)
      }

      "provided with Shut MUCR request" in {

        val submission = submissionFactory.buildConsolidationSubmission(
          validEori,
          Some(validProviderId),
          conversationId,
          exampleShutMucrConsolidationRequestXML,
          ConsolidationType.ShutMucr
        )

        val expectedSubmission = Submission(
          eori = validEori,
          providerId = Some(validProviderId),
          conversationId = conversationId,
          actionType = ConsolidationType.ShutMucr,
          ucrBlocks = Seq.empty
        )

        compareSubmissions(submission, expectedSubmission)
      }
    }
  }

  private def compareSubmissions(actual: Submission, expected: Submission): Unit = {
    actual.eori mustBe expected.eori
    actual.providerId mustBe expected.providerId
    actual.conversationId mustBe expected.conversationId
    actual.actionType mustBe expected.actionType
    actual.ucrBlocks mustBe expected.ucrBlocks
  }

}
