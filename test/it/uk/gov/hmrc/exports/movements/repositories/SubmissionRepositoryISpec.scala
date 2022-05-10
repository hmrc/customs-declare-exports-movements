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

package uk.gov.hmrc.exports.movements.repositories

import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import stubs.TestMongoDB
import testdata.CommonTestData._
import testdata.MovementsTestData._
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.{ConsolidationType, MovementType}

import java.time.{Clock, Instant, ZoneOffset}

class SubmissionRepositoryISpec
    extends AnyWordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with Matchers with IntegrationPatience with TestMongoDB {

  private val clock = Clock.fixed(Instant.parse(dateTimeString), ZoneOffset.UTC)

  override def fakeApplication: Application = {
    SharedMetricRegistries.clear
    GuiceApplicationBuilder()
      .overrides(bind[Clock].to(clock))
      .configure(mongoConfiguration)
      .build()
  }
  private val repo = app.injector.instanceOf[SubmissionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll.futureValue
  }

  override def afterEach(): Unit = {
    repo.removeAll.futureValue
    super.afterEach()
  }

  "SubmissionRepository on insert" when {

    "the operation was successful" should {
      "result in a success" in {
        val submission = exampleSubmission(eori = validEori)
        repo.insertOne(submission).futureValue.isRight must be(true)

        val submissionFromDB = repo.findAll("eori", validEori).futureValue
        submissionFromDB.length must equal(1)
        submissionFromDB.head must equal(submission)
      }
    }

    "trying to insert Submission with the same ConversationID twice" should {

      "result in having only the first Submission persisted" in {
        val submission_1 = exampleSubmission(conversationId = conversationId, actionType = MovementType.Arrival)
        val submission_2 = exampleSubmission(conversationId = conversationId, actionType = ConsolidationType.ShutMucr)

        repo.insertOne(submission_1).futureValue.isRight must be(true)
        val result = repo.insertOne(submission_2).futureValue

        result.isLeft mustBe true
        result.left.get mustBe a[DuplicateKey]

        val submissionFromDB = repo.findAll("eori", validEori).futureValue
        submissionFromDB.length must equal(1)
        submissionFromDB.head must equal(submission_1)
      }
    }
  }

  "SubmissionRepository on findBy" when {

    "querying by EORI only" when {

      "there is no Submission with given EORI" should {
        "return empty list" in {
          val query = SearchParameters(eori = Some(validEori))

          repo.findAll(query).futureValue mustBe Seq.empty
        }
      }

      "there is single Submission with given EORI" should {
        "return this Submission only" in {
          val submission = exampleSubmission(eori = validEori)
          repo.insertOne(submission).futureValue.isRight must be(true)

          val query = SearchParameters(eori = Some(validEori))

          val foundSubmissions = repo.findAll(query).futureValue

          foundSubmissions.length mustBe 1
          foundSubmissions.head mustBe submission
        }
      }

      "there are multiple Submissions with given EORI" should {
        "return all the Submissions" in {
          val submission =
            exampleSubmission(eori = validEori, conversationId = conversationId, actionType = MovementType.Arrival)
          val submission_2 =
            exampleSubmission(eori = validEori, conversationId = conversationId_2, actionType = MovementType.Departure)
          val submission_3 =
            exampleSubmission(eori = validEori, conversationId = conversationId_3, actionType = ConsolidationType.ShutMucr)
          val submission_4 =
            exampleSubmission(eori = validEori, conversationId = conversationId_4, actionType = ConsolidationType.DucrAssociation)
          val submission_5 =
            exampleSubmission(eori = validEori, conversationId = conversationId_5, actionType = ConsolidationType.DucrDisassociation)
          repo.insertOne(submission).futureValue.isRight must be(true)
          repo.insertOne(submission_2).futureValue.isRight must be(true)
          repo.insertOne(submission_3).futureValue.isRight must be(true)
          repo.insertOne(submission_4).futureValue.isRight must be(true)
          repo.insertOne(submission_5).futureValue.isRight must be(true)

          val query = SearchParameters(eori = Some(validEori))

          val foundSubmissions = repo.findAll(query).futureValue

          foundSubmissions.length mustBe 5
          foundSubmissions must contain(submission)
          foundSubmissions must contain(submission_2)
          foundSubmissions must contain(submission_3)
          foundSubmissions must contain(submission_4)
          foundSubmissions must contain(submission_5)
        }
      }
    }

    "querying by Provider ID only" when {

      "there is no Submission with given Provider ID" should {
        "return empty list" in {
          val query = SearchParameters(providerId = Some(validProviderId))

          repo.findAll(query).futureValue mustBe Seq.empty
        }
      }

      "there is single Submission with given Provider ID" should {
        "return this Submission only" in {
          val submission = exampleSubmission(providerId = Some(validProviderId))
          repo.insertOne(submission).futureValue.isRight must be(true)

          val query = SearchParameters(providerId = Some(validProviderId))

          val foundSubmissions = repo.findAll(query).futureValue

          foundSubmissions.length mustBe 1
          foundSubmissions.head mustBe submission
        }
      }

      "there are multiple Submissions with given Provider ID" should {
        "return all the Submissions" in {
          val submission =
            exampleSubmission(providerId = Some(validProviderId), conversationId = conversationId, actionType = MovementType.Arrival)
          val submission_2 =
            exampleSubmission(providerId = Some(validProviderId), conversationId = conversationId_2, actionType = MovementType.Departure)
          val submission_3 =
            exampleSubmission(providerId = Some(validProviderId), conversationId = conversationId_3, actionType = ConsolidationType.ShutMucr)
          val submission_4 =
            exampleSubmission(providerId = Some(validProviderId), conversationId = conversationId_4, actionType = ConsolidationType.DucrAssociation)
          val submission_5 =
            exampleSubmission(
              providerId = Some(validProviderId),
              conversationId = conversationId_5,
              actionType = ConsolidationType.DucrDisassociation
            )
          repo.insertOne(submission).futureValue.isRight must be(true)
          repo.insertOne(submission_2).futureValue.isRight must be(true)
          repo.insertOne(submission_3).futureValue.isRight must be(true)
          repo.insertOne(submission_4).futureValue.isRight must be(true)
          repo.insertOne(submission_5).futureValue.isRight must be(true)

          val query = SearchParameters(providerId = Some(validProviderId))

          val foundSubmissions = repo.findAll(query).futureValue

          foundSubmissions.length mustBe 5
          foundSubmissions must contain(submission)
          foundSubmissions must contain(submission_2)
          foundSubmissions must contain(submission_3)
          foundSubmissions must contain(submission_4)
          foundSubmissions must contain(submission_5)
        }
      }
    }

    "querying by Conversation ID only" when {

      "there is no Submission with given Conversation ID" should {
        "return empty list" in {
          val query = SearchParameters(conversationId = Some(conversationId))

          repo.findAll(query).futureValue mustBe Seq.empty
        }
      }

      "there is single Submission with given Conversation ID" should {
        "return this Submission" in {
          val submission = exampleSubmission(conversationId = conversationId)
          repo.insertOne(submission).futureValue.isRight must be(true)

          val query = SearchParameters(conversationId = Some(conversationId))

          val foundSubmissions = repo.findAll(query).futureValue

          foundSubmissions.length mustBe 1
          foundSubmissions.head mustBe submission
        }
      }
    }

    "querying by EORI and Conversation ID" should {

      "return empty list" when {

        "there is no Submission with given EORI and Conversation ID" in {
          val query = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

          repo.findAll(query).futureValue mustBe Seq.empty
        }

        "there is Submission with given EORI but not Conversation ID" in {
          val submission = exampleSubmission(eori = validEori, conversationId = conversationId_2)
          repo.insertOne(submission).futureValue.isRight must be(true)

          val query = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

          repo.findAll(query).futureValue mustBe Seq.empty
        }

        "there is Submission with given Conversation ID but not EORI" in {
          val submission = exampleSubmission(eori = validEori_2, conversationId = conversationId)
          repo.insertOne(submission).futureValue.isRight must be(true)

          val query = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

          repo.findAll(query).futureValue mustBe Seq.empty
        }
      }

      "return single-element list with Submission" when {

        "there is single Submission with given EORI and Conversation ID" in {
          val submission = exampleSubmission(eori = validEori, conversationId = conversationId)
          repo.insertOne(submission).futureValue.isRight must be(true)

          val query = SearchParameters(eori = Some(validEori), conversationId = Some(conversationId))

          val foundSubmissions = repo.findAll(query).futureValue

          foundSubmissions.length mustBe 1
          foundSubmissions.head mustBe submission
        }
      }
    }

    "querying by Provider ID and Conversation ID" should {

      "return empty list" when {

        "there is no Submission with given Provider ID and Conversation ID" in {
          val query = SearchParameters(providerId = Some(validProviderId), conversationId = Some(conversationId))

          repo.findAll(query).futureValue mustBe Seq.empty
        }

        "there is Submission with given Provider ID but not Conversation ID" in {
          val submission = exampleSubmission(providerId = Some(validProviderId), conversationId = conversationId_2)
          repo.insertOne(submission).futureValue.isRight must be(true)

          val query = SearchParameters(providerId = Some(validProviderId), conversationId = Some(conversationId))

          repo.findAll(query).futureValue mustBe Seq.empty
        }

        "there is Submission with given Conversation ID but not Provider ID" in {
          val submission = exampleSubmission(providerId = Some(validProviderId_2), conversationId = conversationId)
          repo.insertOne(submission).futureValue.isRight must be(true)

          val query = SearchParameters(providerId = Some(validProviderId), conversationId = Some(conversationId))

          repo.findAll(query).futureValue mustBe Seq.empty
        }
      }

      "return single-element list with Submission" when {
        "there is single Submission with given Provider ID and Conversation ID" in {
          val submission = exampleSubmission(providerId = Some(validProviderId), conversationId = conversationId)
          repo.insertOne(submission).futureValue.isRight must be(true)

          val query = SearchParameters(providerId = Some(validProviderId), conversationId = Some(conversationId))

          val foundSubmissions = repo.findAll(query).futureValue

          foundSubmissions.length mustBe 1
          foundSubmissions.head mustBe submission
        }
      }
    }

    "querying with empty SearchParameters" should {
      "return empty list" in {
        val submission = exampleSubmission(providerId = Some(validProviderId), conversationId = conversationId)
        repo.insertOne(submission).futureValue.isRight must be(true)

        val query = SearchParameters(None, None, None)

        repo.findAll(query).futureValue mustBe Seq.empty
      }
    }
  }
}
