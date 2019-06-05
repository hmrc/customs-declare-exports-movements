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
import org.scalatest.BeforeAndAfter
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.exports.movements.repositories.MovementNotificationsRepository
import unit.uk.gov.hmrc.exports.movements.base.CustomsExportsBaseSpec
import utils.MovementsTestData

class MovementNotificationsRepositorySpec extends CustomsExportsBaseSpec with MovementsTestData with BeforeAndAfter {

  before {
    repo.removeAll().futureValue
    SharedMetricRegistries.clear()
  }

  override lazy val app: Application = GuiceApplicationBuilder().build()
  private val repo = app.injector.instanceOf[MovementNotificationsRepository]

  "Movement notifications repository" should {

    "save notification and retrieve it by EORI" in {

      val notification = movementNotification()

      repo.save(notification).futureValue must be(true)
      val found = repo.findByEori(validEori).futureValue

      found.length must be(1)
      found.head.eori must be(validEori)
      found.head.conversationId must be(notification.conversationId)
      found.head.dateTimeReceived.compareTo(now) must be(0)
    }

    "save notification and retrieve it by conversationID" in {

      val notification = movementNotification()

      repo.save(notification).futureValue must be(true)
      val found = repo.getByConversationId(notification.conversationId).futureValue.get

      found.eori must be(validEori)
      found.conversationId must be(notification.conversationId)
      found.dateTimeReceived.compareTo(now) must be(0)
    }

    "save notification and retrieve it by both EORI and conversationID" in {

      val notification = movementNotification()

      repo.save(notification).futureValue must be(true)
      val found = repo.getByEoriAndConversationId(validEori, notification.conversationId).futureValue.get

      found.eori must be(validEori)
      found.conversationId must be(notification.conversationId)
      found.dateTimeReceived.compareTo(now) must be(0)
    }

    "save two notifications and retrieve them by EORI (1) and conversationID (2)" in {

      val notificationOne = movementNotification("GB123")
      val notificationTwo = movementNotification("GB456")

      repo.save(notificationOne).futureValue must be(true)
      repo.save(notificationTwo).futureValue must be(true)

      val findFirst = repo.findByEori("GB123").futureValue

      findFirst.head.eori must be("GB123")
      findFirst.head.conversationId must be(notificationOne.conversationId)
      findFirst.head.dateTimeReceived.compareTo(now) must be(0)

      val findSecond = repo.getByConversationId(notificationTwo.conversationId).futureValue.get

      findSecond.eori must be("GB456")
      findSecond.conversationId must be(notificationTwo.conversationId)
      findSecond.dateTimeReceived.compareTo(now) must be(0)
    }
  }
}
