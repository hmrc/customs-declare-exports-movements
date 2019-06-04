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
    extends WordSpec with MustMatchers with GuiceOneAppPerSuite with MovementsTestData with ScalaFutures with BeforeAndAfter {

  val repo = app.injector.instanceOf[MovementsRepository]

  before {
    repo.removeAll().futureValue
    SharedMetricRegistries.clear()
  }

  // TODO: possibly split the tests, as it is too high level
  "Movements repository" should {
    "save movement with EORI, UCR and timestamp" in {
      repo.save(movement).futureValue must be(true)

      // we can now display a list of all the movements belonging to the current user, searching by EORI
      val found = repo.findByEori(validEori).futureValue
      found.length must be(1)
      found.head.eori must be(validEori)
      found.head.conversationId must be(conversationId)
      found.head.ucr must be(ucr)

      // a timestamp has been generated representing "creation time" of case class instance
      found.head.submittedTimestamp must (be >= before).and(be <= System.currentTimeMillis())

      // we can also retrieve the movement individually by conversation ID
      val gotMovement = repo.getByConversationId(conversationId).futureValue.get
      gotMovement.eori must be(validEori)
      gotMovement.conversationId must be(conversationId)
      gotMovement.ucr must be(ucr)

      // or we can retrieve it by eori and MRN
      val gotAgain = repo.getByEoriAndDucr(validEori, ucr).futureValue.get
      gotAgain.eori must be(validEori)
      gotAgain.conversationId must be(conversationId)
      gotAgain.ucr must be(ucr)
    }
  }


}
