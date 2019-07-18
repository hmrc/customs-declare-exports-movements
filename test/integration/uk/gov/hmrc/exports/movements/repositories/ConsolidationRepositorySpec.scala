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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsString
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.exports.movements.repositories.ConsolidationRepository
import utils.ConsolidationTestData._
import utils.MovementsTestData.conversationId_2

import scala.concurrent.ExecutionContext.Implicits.global

class ConsolidationRepositorySpec
    extends WordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with MustMatchers {

  override lazy val app: Application = GuiceApplicationBuilder().build()
  private val repo = app.injector.instanceOf[ConsolidationRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
  }

  override def afterEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
  }

  "Consolidation Repository on insert" when {

    "the operation was successful" should {
      "result in Submission being stored in DB" in {
        repo.insert(consolidationSubmission).futureValue.ok must be(true)

        val submissionsInDB = repo.find("eori" -> JsString(consolidationSubmission.eori)).futureValue
        submissionsInDB.length must equal(1)
        submissionsInDB.head must equal(consolidationSubmission)
      }
    }

    "trying to insert the same Submission twice" should {
      "throw DatabaseException with duplication error" in {
        repo.insert(consolidationSubmission).futureValue.ok must be(true)

        val exc = repo.insert(consolidationSubmission).failed.futureValue

        exc mustBe an[DatabaseException]
        exc.getMessage must include(
          "E11000 duplicate key error collection: customs-declare-exports-movements.movementsConsolidations index: conversationIdIdx"
        )
      }
    }

    "inserting different Submission with the same EORI" should {
      "result in storing both Submissions in DB" in {
        val consolidationSubmission_2 = consolidationSubmission.copy(conversationId = conversationId_2)
        repo.insert(consolidationSubmission).futureValue.ok must be(true)
        repo.insert(consolidationSubmission_2).futureValue.ok must be(true)

        val submissionsInDB = repo.find("eori" -> JsString(consolidationSubmission.eori)).futureValue
        submissionsInDB.length must equal(2)
        submissionsInDB must contain(consolidationSubmission)
        submissionsInDB must contain(consolidationSubmission_2)
      }
    }
  }

}
