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
import uk.gov.hmrc.exports.movements.repositories.NotificationRepository
import utils.TestMongoDB
import utils.testdata.CommonTestData.{conversationId, conversationId_2}
import utils.testdata.MovementsTestData.dateTimeString
import utils.testdata.notifications.NotificationTestData._

import scala.concurrent.ExecutionContext.Implicits.global

class NotificationRepositorySpec
    extends WordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with MustMatchers with IntegrationPatience with TestMongoDB {

  private val clock = Clock.fixed(Instant.parse(dateTimeString), ZoneOffset.UTC)

  override def fakeApplication: Application = {
    SharedMetricRegistries.clear()
    GuiceApplicationBuilder()
      .overrides(bind[Clock].to(clock))
      .configure(mongoConfiguration)
      .build()
  }
  private val repo = app.injector.instanceOf[NotificationRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
  }

  override def afterEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
  }

  "NotificationRepository on insert" when {

    "the operation was successful" should {
      "result in a success" in {
        repo.insert(notification_1).futureValue.ok must be(true)

        val notificationInDB = repo.findByConversationIds(Seq(conversationId)).futureValue

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

        val notificationsInDB = repo.findByConversationIds(Seq(conversationId)).futureValue

        notificationsInDB.length must equal(2)
        notificationsInDB.head must equal(notification_1)
        notificationsInDB(1) must equal(notification_1)
      }
    }
  }

  "Notification Repository on findByConversationIds" when {

    "there is no Notification with given conversationIds" should {
      "return empty list" in {
        repo.findByConversationIds(Seq(conversationId, conversationId_2)).futureValue must equal(Seq.empty)
      }
    }

    "there is single Notification with one of given conversationIds" should {
      "return this Notification only" in {
        repo.insert(notification_1).futureValue

        val foundNotifications = repo.findByConversationIds(Seq(conversationId, conversationId_2)).futureValue

        foundNotifications.length must equal(1)
        foundNotifications.head must equal(notification_1)
      }
    }

    "there are multiple Notifications with given conversationIds" should {
      "return all the Notifications" in {
        val notifications = Seq(notification_1, notification_2.copy(conversationId = notification_1.conversationId), notification_2)
        notifications.map(repo.insert(_).futureValue)

        val foundNotifications = repo.findByConversationIds(Seq(conversationId, conversationId_2)).futureValue

        foundNotifications.length must equal(notifications.length)
        notifications.foreach { notification =>
          foundNotifications must contain(notification)
        }
      }
    }
  }

}
