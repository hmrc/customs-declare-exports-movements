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

package uk.gov.hmrc.exports.movements.repositories

import org.scalatest.BeforeAndAfter
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.exports.movements.base.{CustomsExportsBaseSpec, ExportsTestData}

class MovementNotificationsRepositorySpec extends CustomsExportsBaseSpec with ExportsTestData with BeforeAndAfter {

  before {
    repo.removeAll().futureValue
  }

  override lazy val app: Application = GuiceApplicationBuilder().build()

  val repo = app.injector.instanceOf[MovementNotificationsRepository]

  // TODO: possibly split the tests, as it is too high level
  "Movement notifications repository" should {
    "save notification with EORI, conversationID and timestamp" in {
      repo.save(movementNotification).futureValue must be(true)

      // we can now display a list of all the declarations belonging to the current user, searching by EORI
      val found = repo.findByEori(eori).futureValue
      found.length must be(1)
      found.head.eori must be(eori)
      found.head.conversationId must be(movementNotification.conversationId)

      found.head.dateTimeReceived.compareTo(now) must be(0)

      // we can also retrieve the submission individually by conversation Id
      val got = repo.getByConversationId(movementNotification.conversationId).futureValue.get
      got.eori must be(eori)
      got.conversationId must be(movementNotification.conversationId)

      // or we can retrieve it by eori and conversationId
      val gotAgain = repo.getByEoriAndConversationId(eori, movementNotification.conversationId).futureValue.get
      gotAgain.eori must be(eori)
      gotAgain.conversationId must be(movementNotification.conversationId)
    }
  }
}
