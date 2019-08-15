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
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.exports.movements.repositories.NotificationRepository
import utils.testdata.CommonTestData.conversationId
import utils.testdata.NotificationTestData._

import scala.concurrent.ExecutionContext.Implicits.global

class NotificationRepositorySpec
    extends WordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with MustMatchers
    with IntegrationPatience {

  override lazy val app: Application = GuiceApplicationBuilder().build()
  private val repo = app.injector.instanceOf[NotificationRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
  }

  override def afterEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
    SharedMetricRegistries.clear()
  }

  "NotificationRepository on insert" when {

    "the operation was successful" should {
      "result in a success" in {
        repo.insert(notification_1).futureValue.ok must be(true)

        val notificationInDB = repo.findNotificationsByConversationId(conversationId).futureValue
        notificationInDB.length must equal(1)
        notificationInDB.head must equal(notification_1)
      }
    }

    "trying to insert the same Notification twice" should {
      "result in a success" in {
        repo.insert(notification_1).futureValue.ok must be(true)
        repo.insert(notification_1).futureValue.ok must be(true)
      }

      "result in having both Notifications persisted" in {
        repo.insert(notification_1).futureValue.ok must be(true)
        repo.insert(notification_1).futureValue.ok must be(true)

        val notificationsInDB = repo.findNotificationsByConversationId(conversationId).futureValue
        notificationsInDB.length must equal(2)
        notificationsInDB.head must equal(notification_1)
        notificationsInDB(1) must equal(notification_1)
      }
    }
  }

  "Notification Repository on findNotificationsByConversationId" when {

    "there is no Notification with given conversationId" should {
      "return empty list" in {
        repo.findNotificationsByConversationId(conversationId).futureValue must equal(Seq.empty)
      }
    }

    "there is single Notification with given conversationId" should {
      "return this Notification only" in {
        repo.insert(notification_1).futureValue

        val foundNotifications = repo.findNotificationsByConversationId(conversationId).futureValue

        foundNotifications.length must equal(1)
        foundNotifications.head must equal(notification_1)
      }
    }

    "there are multiple Notifications with given conversationId" should {
      "return all the Notifications" in {
        repo.insert(notification_1).futureValue
        val notificationWithSameConversationId = notification_2.copy(conversationId = notification_1.conversationId)
        repo.insert(notificationWithSameConversationId).futureValue

        val foundNotifications = repo.findNotificationsByConversationId(conversationId).futureValue

        foundNotifications.length must equal(2)
        foundNotifications must contain(notification_1)
        foundNotifications must contain(notificationWithSameConversationId)
      }
    }
  }

}
