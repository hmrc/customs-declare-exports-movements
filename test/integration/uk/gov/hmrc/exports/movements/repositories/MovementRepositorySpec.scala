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
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.exports.movements.repositories.MovementsRepository
import utils.MovementsTestData

import scala.concurrent.ExecutionContext.Implicits.global

class MovementRepositorySpec
    extends WordSpec with MustMatchers with GuiceOneAppPerSuite with MovementsTestData with ScalaFutures
    with BeforeAndAfter {

  private val repo = app.injector.instanceOf[MovementsRepository]

  before {
    repo.removeAll().futureValue
    SharedMetricRegistries.clear()
  }

  "Movements repository" should {

    "save movement submission and retrieve it by EORI" in {

      val submission = movementSubmission()
      repo.save(submission).futureValue must be(true)

      val found = repo.findByEori(validEori).futureValue

      found.length must be(1)
      found.head.eori must be(validEori)
      found.head.conversationId must be(conversationId)
      found.head.ucr must be(ucr)
      // a timestamp has been generated representing "creation time" of case class instance
      found.head.submittedTimestamp must (be >= before).and(be <= System.currentTimeMillis())
    }

    "save movement submission and retrieve it by ConversationId" in {

      val submission = movementSubmission()
      repo.save(submission).futureValue must be(true)

      val found = repo.getByConversationId(conversationId).futureValue.get

      found.eori must be(validEori)
      found.conversationId must be(conversationId)
      found.ucr must be(ucr)
      found.submittedTimestamp must (be >= before).and(be <= System.currentTimeMillis())
    }

    "save movement submission and retrieve it by EORI and DUCR" in {

      val submission = movementSubmission()
      repo.save(submission).futureValue must be(true)

      val found = repo.getByEoriAndDucr(validEori, ucr).futureValue.get

      found.eori must be(validEori)
      found.conversationId must be(conversationId)
      found.ucr must be(ucr)
      found.submittedTimestamp must (be >= before).and(be <= System.currentTimeMillis())
    }

    "save two movement submissions and retrieve them by EORI (1) and conversationId (2)" in {

      val submissionOne = movementSubmission(eori = "GB123", convoId = "123", subUcr = "123")
      val submissionTwo = movementSubmission(eori = "GB456", convoId = "456", subUcr = "456")

      repo.save(submissionOne).futureValue must be(true)
      repo.save(submissionTwo).futureValue must be(true)

      val firstSubmission = repo.findByEori("GB123").futureValue

      firstSubmission.head.eori must be("GB123")
      firstSubmission.head.conversationId must be("123")
      firstSubmission.head.ucr must be("123")
      firstSubmission.head.submittedTimestamp must (be >= before).and(be <= System.currentTimeMillis())

      val secondSubmission = repo.getByConversationId(submissionTwo.conversationId).futureValue

      secondSubmission.head.eori must be("GB456")
      secondSubmission.head.conversationId must be("456")
      secondSubmission.head.ucr must be("456")
      secondSubmission.head.submittedTimestamp must (be >= before).and(be <= System.currentTimeMillis())
    }
  }
}
