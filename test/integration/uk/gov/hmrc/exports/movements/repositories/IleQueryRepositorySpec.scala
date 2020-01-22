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

package integration.uk.gov.hmrc.exports.movements.repositories

import java.time.{Clock, Instant, ZoneOffset}

import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.exports.movements.repositories.IleQueryRepository
import utils.TestMongoDB
import utils.testdata.CommonTestData.conversationId_2
import utils.testdata.IleQueryResponseTestData.ileQueryResponse_1
import utils.testdata.IleQuerySubmissionTestData._
import utils.testdata.MovementsTestData.dateTimeString

import scala.concurrent.ExecutionContext.Implicits.global

class IleQueryRepositorySpec
    extends WordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with MustMatchers with IntegrationPatience with TestMongoDB {

  private val clock = Clock.fixed(Instant.parse(dateTimeString), ZoneOffset.UTC)

  override def fakeApplication: Application = {
    SharedMetricRegistries.clear()
    GuiceApplicationBuilder()
      .overrides(bind[Clock].to(clock))
      .configure(mongoConfiguration)
      .build()
  }
  private val repo = app.injector.instanceOf[IleQueryRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
  }

  override def afterEach(): Unit = {
    repo.removeAll().futureValue
    super.afterEach()
  }

  "IleQueryRepository on insert" when {

    "the operation was successful" should {

      "result in a success" in {
        val submission = ileQuerySubmission_1

        repo.insert(submission).futureValue.ok mustBe true
      }

      "result in having the document in collection" in {
        val submission = ileQuerySubmission_1

        repo.insert(submission).futureValue

        val submissionsFromDB = repo.findAll().futureValue
        submissionsFromDB.size mustBe 1
        submissionsFromDB.head mustBe submission
      }
    }

    "trying to insert IleQuerySubmission with the same Conversation ID twice" should {

      "throw DatabaseException" in {
        val submission_1 = ileQuerySubmission_1
        val submission_2 = ileQuerySubmission_2.copy(conversationId = submission_1.conversationId)

        repo.insert(submission_1).futureValue.ok mustBe true
        val exc = repo.insert(submission_2).failed.futureValue

        exc mustBe an[DatabaseException]
        exc.getMessage must include(
          "E11000 duplicate key error collection: test-customs-declare-exports-movements.ileQuerySubmissions index: conversationIdIdx dup key"
        )
      }

      "result in having only the first IleQuerySubmission persisted" in {
        val submission_1 = ileQuerySubmission_1
        val submission_2 = ileQuerySubmission_2.copy(conversationId = submission_1.conversationId)

        repo.insert(submission_1).futureValue.ok mustBe true
        repo.insert(submission_2).failed.futureValue

        val submissionsFromDB = repo.findAll().futureValue
        submissionsFromDB.size mustBe 1
        submissionsFromDB.head mustBe submission_1
      }
    }
  }

  "IleQueryRepository on addResponse" when {

    "adding response to existing IleQuerySubmission" should {

      "return the IleQuerySubmission updated" in {
        val submission = ileQuerySubmission_1.copy(responses = Seq.empty)
        val response = ileQueryResponse_1.copy(conversationId = submission.conversationId)
        val expectedUpdatedSubmission = submission.copy(responses = Seq(response))
        repo.insert(submission).futureValue.ok mustBe true

        val updatedSubmission = repo.addResponse(response).futureValue

        updatedSubmission mustBe expectedUpdatedSubmission
      }

      "result in having the IleQuerySubmission updated in the DB" in {
        val submission = ileQuerySubmission_1.copy(responses = Seq.empty)
        val response = ileQueryResponse_1.copy(conversationId = submission.conversationId)
        val expectedUpdatedSubmission = submission.copy(responses = Seq(response))
        repo.insert(submission).futureValue.ok mustBe true

        repo.addResponse(response).futureValue

        val submissionsFromDB = repo.findAll().futureValue
        submissionsFromDB.size mustBe 1
        submissionsFromDB.head mustBe expectedUpdatedSubmission
      }
    }

    "trying to add response for non-existing IleQuerySubmission" should {

      "return failed Future with IllegalStateException" in {
        val submission = ileQuerySubmission_1.copy(responses = Seq.empty)
        val response = ileQueryResponse_1.copy(conversationId = conversationId_2)
        repo.insert(submission).futureValue.ok mustBe true

        val exc = repo.addResponse(response).failed.futureValue

        exc.getMessage must include("IleQuerySubmission must exist before adding a response")
      }
    }

    "trying to add the same response to the IleQuerySubmission twice" should {

      "return the IleQuerySubmission not updated" in {
        val submission = ileQuerySubmission_1.copy(responses = Seq.empty)
        val response = ileQueryResponse_1.copy(conversationId = submission.conversationId)
        repo.insert(submission).futureValue.ok mustBe true

        val updatedSubmission_1 = repo.addResponse(response).futureValue
        val updatedSubmission_2 = repo.addResponse(response).futureValue

        updatedSubmission_1 mustBe updatedSubmission_2
      }

      "result in having the IleQuerySubmission with just a single response in the DB" in {
        val submission = ileQuerySubmission_1.copy(responses = Seq.empty)
        val response = ileQueryResponse_1.copy(conversationId = submission.conversationId)
        val expectedUpdatedSubmission = submission.copy(responses = Seq(response))
        repo.insert(submission).futureValue.ok mustBe true

        repo.addResponse(response).futureValue
        repo.addResponse(response).futureValue

        val submissionsFromDB = repo.findAll().futureValue
        submissionsFromDB.size mustBe 1
        submissionsFromDB.head mustBe expectedUpdatedSubmission
      }
    }
  }

}
