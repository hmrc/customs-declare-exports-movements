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

package integration.uk.gov.hmrc.exports.movements.repositories

import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsString
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.exports.movements.models.submissions.Submission.ActionTypes
import uk.gov.hmrc.exports.movements.repositories.SubmissionRepository
import utils.MovementsTestData._

import scala.concurrent.ExecutionContext.Implicits.global

class SubmissionRepositorySpec
    extends WordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with MustMatchers {

  override lazy val app: Application = GuiceApplicationBuilder().build()
  private val repo = app.injector.instanceOf[SubmissionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
    SharedMetricRegistries.clear()
  }

  override def afterEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
  }

  "SubmissionRepository on insert" when {

    "the operation was successful" should {
      "result in a success" in {
        val submission = exampleSubmission(eori = validEori)
        repo.insert(submission).futureValue.ok must be(true)

        val submissionFromDB = repo.find("eori" -> JsString(validEori)).futureValue
        submissionFromDB.length must equal(1)
        submissionFromDB.head must equal(submission)
      }
    }

    "trying to insert Submission with the same ConversationID twice" should {

      "throw DatabaseException" in {
        val submission_1 = exampleSubmission(conversationId = conversationId, actionType = ActionTypes.Arrival)
        val submission_2 = exampleSubmission(conversationId = conversationId, actionType = ActionTypes.ShutMucr)

        repo.insert(submission_1).futureValue.ok must be(true)
        val exc = repo.insert(submission_2).failed.futureValue

        exc mustBe an[DatabaseException]
        exc.getMessage must include(
          "E11000 duplicate key error collection: customs-declare-exports-movements.movementSubmissions index: conversationIdIdx dup key"
        )
      }

      "result in having only the first Submission persisted" in {
        val submission_1 = exampleSubmission(conversationId = conversationId, actionType = ActionTypes.Arrival)
        val submission_2 = exampleSubmission(conversationId = conversationId, actionType = ActionTypes.ShutMucr)

        repo.insert(submission_1).futureValue.ok must be(true)
        repo.insert(submission_2).failed.futureValue

        val submissionFromDB = repo.find("eori" -> JsString(validEori)).futureValue
        submissionFromDB.length must equal(1)
        submissionFromDB.head must equal(submission_1)
      }
    }
  }

  "SubmissionRepository on findByEori" when {

    "there is no Submission with given EORI" should {
      "return empty list" in {
        repo.findByEori(validEori).futureValue must equal(Seq.empty)
      }
    }

    "there is single Submission with given EORI" should {
      "return this Submission only" in {
        val submission = exampleSubmission(eori = validEori)
        repo.insert(submission).futureValue.ok must be(true)

        val foundSubmissions = repo.findByEori(validEori).futureValue

        foundSubmissions.length must equal(1)
        foundSubmissions.head must equal(submission)
      }
    }

    "there are multiple Submissions with given EORI" should {
      "return all the Submissions" in {
        val submission =
          exampleSubmission(eori = validEori, conversationId = conversationId, actionType = ActionTypes.Arrival)
        val submission_2 =
          exampleSubmission(eori = validEori, conversationId = conversationId_2, actionType = ActionTypes.Departure)
        val submission_3 =
          exampleSubmission(eori = validEori, conversationId = conversationId_3, actionType = ActionTypes.ShutMucr)
        val submission_4 =
          exampleSubmission(
            eori = validEori,
            conversationId = conversationId_4,
            actionType = ActionTypes.DucrAssociation
          )
        val submission_5 =
          exampleSubmission(
            eori = validEori,
            conversationId = conversationId_5,
            actionType = ActionTypes.DucrDisassociation
          )
        repo.insert(submission).futureValue.ok must be(true)
        repo.insert(submission_2).futureValue.ok must be(true)
        repo.insert(submission_3).futureValue.ok must be(true)
        repo.insert(submission_4).futureValue.ok must be(true)
        repo.insert(submission_5).futureValue.ok must be(true)

        val foundSubmissions = repo.findByEori(validEori).futureValue

        foundSubmissions.length must equal(5)
        foundSubmissions must contain(submission)
        foundSubmissions must contain(submission_2)
        foundSubmissions must contain(submission_3)
        foundSubmissions must contain(submission_4)
        foundSubmissions must contain(submission_5)
      }
    }
  }

}
