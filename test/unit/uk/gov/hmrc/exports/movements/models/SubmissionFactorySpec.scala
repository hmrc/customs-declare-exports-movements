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

package unit.uk.gov.hmrc.exports.movements.models

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.{ActionType, Submission, SubmissionFactory}
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import utils.testdata.CommonTestData._
import utils.testdata.ConsolidationTestData._
import utils.testdata.MovementsTestData.{exampleArrivalRequestXML, exampleDepartureRequestXML}

class SubmissionFactorySpec extends WordSpec with MustMatchers with MockitoSugar {

  private trait Test {
    val submissionFactory = new SubmissionFactory
  }

  "SubmissionFactory on buildMovementSubmission" should {

    "return Submission with provided data" when {

      "provided with Arrival request" in new Test {
        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Arrival,
          requestXml = exampleArrivalRequestXML
        )

        val submission = submissionFactory.buildMovementSubmission(conversationId = conversationId, context = context)

        val expectedSubmission = Submission(
          eori = validEori,
          conversationId = conversationId,
          actionType = ActionType.Arrival,
          ucrBlocks = Seq(UcrBlock(ucr = ucr_2, ucrType = "M"), UcrBlock(ucr = ucr, ucrType = "D"))
        )

        compareSubmissions(submission, expectedSubmission)
      }

      "provided with Departure request" in new Test {
        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Departure,
          requestXml = exampleDepartureRequestXML
        )

        val submission = submissionFactory.buildMovementSubmission(conversationId = conversationId, context = context)

        val expectedSubmission = Submission(
          eori = validEori,
          conversationId = conversationId,
          actionType = ActionType.Departure,
          ucrBlocks = Seq(UcrBlock(ucr = ucr_2, ucrType = "M"), UcrBlock(ucr = ucr, ucrType = "D"))
        )

        compareSubmissions(submission, expectedSubmission)
      }

      "provided with Association request" in new Test {
        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.DucrAssociation,
          requestXml = exampleAssociateDucrConsolidationRequestXML
        )

        val submission = submissionFactory.buildMovementSubmission(conversationId = conversationId, context = context)

        val expectedSubmission = Submission(
          eori = validEori,
          conversationId = conversationId,
          actionType = ActionType.DucrAssociation,
          ucrBlocks = Seq(UcrBlock(ucr = ucr_2, ucrType = "M"), UcrBlock(ucr = ucr, ucrType = "D"))
        )

        compareSubmissions(submission, expectedSubmission)
      }

      "provided with Disassociation request" in new Test {
        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.DucrDisassociation,
          requestXml = exampleDisassociateDucrConsolidationRequestXML
        )

        val submission = submissionFactory.buildMovementSubmission(conversationId = conversationId, context = context)

        val expectedSubmission = Submission(
          eori = validEori,
          conversationId = conversationId,
          actionType = ActionType.DucrDisassociation,
          ucrBlocks = Seq(UcrBlock(ucr = ucr, ucrType = "D"))
        )

        compareSubmissions(submission, expectedSubmission)
      }

      "provided with Shut MUCR request" in new Test {
        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.ShutMucr,
          requestXml = exampleShutMucrConsolidationRequestXML
        )

        val submission = submissionFactory.buildMovementSubmission(conversationId = conversationId, context = context)

        val expectedSubmission = Submission(
          eori = validEori,
          conversationId = conversationId,
          actionType = ActionType.ShutMucr,
          ucrBlocks = Seq(UcrBlock(ucr = ucr_2, ucrType = "M"))
        )

        compareSubmissions(submission, expectedSubmission)
      }
    }
  }

  private def compareSubmissions(actual: Submission, expected: Submission): Unit = {
    actual.eori must equal(expected.eori)
    actual.conversationId must equal(expected.conversationId)
    actual.actionType must equal(expected.actionType)
    actual.ucrBlocks must equal(expected.ucrBlocks)
  }

}
