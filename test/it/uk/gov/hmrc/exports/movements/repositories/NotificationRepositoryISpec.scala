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
import org.bson.types.ObjectId
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import stubs.TestMongoDB
import testdata.CommonTestData
import testdata.CommonTestData.{conversationId, conversationId_2}
import testdata.MovementsTestData.dateTimeString
import testdata.notifications.NotificationTestData._
import uk.gov.hmrc.exports.movements.models.notifications.Notification

import java.time.{Clock, Instant, ZoneOffset}

class NotificationRepositoryISpec
    extends AnyWordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with Matchers with IntegrationPatience with TestMongoDB {
  private val clock = Clock.fixed(Instant.parse(dateTimeString), ZoneOffset.UTC)

  override val fakeApplication: Application = {
    SharedMetricRegistries.clear()
    GuiceApplicationBuilder()
      .overrides(bind[Clock].to(clock))
      .configure(mongoConfiguration)
      .build()
  }
  private val repo = app.injector.instanceOf[NotificationRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll.futureValue
  }

  override def afterEach(): Unit = {
    repo.removeAll.futureValue
    super.afterEach()
  }

  "NotificationRepository on insert" when {

    "the operation was successful" should {

      "result in a success" when {

        "provided with StandardNotification" in {
          repo.insertOne(notification_1).futureValue.isRight must be(true)

          val notificationInDB = repo.findAll.futureValue

          notificationInDB.length must equal(1)
          notificationInDB.head must equalWithoutId(notification_1)
        }

        "provided with IleQueryResponseNotification" in {
          repo.insertOne(notificationIleQueryResponse_1).futureValue.isRight must be(true)

          val notificationInDB = repo.findAll.futureValue

          notificationInDB.length must equal(1)
          notificationInDB.head must equalWithoutId(notificationIleQueryResponse_1)
        }
      }
    }

    "trying to insert the same Notification twice" should {
      "result in a success" in {
        repo.insertOne(notification_1).futureValue.isRight must be(true)
        repo.insertOne(notification_1.copy(_id = ObjectId.get)).futureValue.isRight must be(true)
      }

      "result in having both Notifications persisted" in {
        repo.insertOne(notification_1).futureValue.isRight must be(true)
        repo.insertOne(notification_1.copy(_id = ObjectId.get)).futureValue.isRight must be(true)

        val notificationsInDB = repo.findAll.futureValue

        notificationsInDB.length must equal(2)
        notificationsInDB.head must equalWithoutId(notification_1)
        notificationsInDB(1) must equalWithoutId(notification_1)
      }
    }

    "trying to insert different Notifications but with the same Conversation ID" should {
      "result in having all Notifications persisted" in {
        val notificationsToInsert = Seq(notification_1, notification_2.copy(conversationId = conversationId), notificationIleQueryResponse_1)
        notificationsToInsert.foreach { notification =>
          repo.insertOne(notification).futureValue.isRight must be(true)
        }

        val notificationsInDB = repo.findAll.futureValue

        notificationsInDB.length must equal(3)

        notificationsToInsert.foreach { notification =>
          notificationsInDB must contain(notification)
        }
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
        repo.insertOne(notification_1).futureValue

        val foundNotifications = repo.findByConversationIds(Seq(conversationId, conversationId_2)).futureValue

        foundNotifications.length must equal(1)
        foundNotifications.head must equalWithoutId(notification_1)
      }
    }

    "there are multiple Notifications with given conversationIds" should {
      "return all the Notifications" in {
        val notifications =
          Seq(notification_1, notification_2.copy(_id = ObjectId.get, conversationId = notification_1.conversationId), notification_2)
        notifications.map(repo.insertOne(_).futureValue)

        val foundNotifications = repo.findByConversationIds(Seq(conversationId, conversationId_2)).futureValue

        foundNotifications.length must equal(notifications.length)
        notifications.foreach { notification =>
          foundNotifications must contain(notification)
        }
      }
    }
  }

  "Notification Repository on findUnparsedNotifications" when {

    "there are no unparsed Notification" should {
      "return empty list" in {
        repo.insertOne(notification_1).futureValue.isRight must be(true)

        repo.findUnparsedNotifications().futureValue must equal(Seq.empty)
      }
    }

    "there is one unparsed Notification" should {
      "return that Notification" in {
        repo.insertOne(notification_1).futureValue.isRight must be(true)
        repo.insertOne(notificationUnparsed).futureValue.isRight must be(true)

        val foundNotifications = repo.findUnparsedNotifications().futureValue

        foundNotifications.length mustBe 1
        foundNotifications.head must equalWithoutId(notificationUnparsed)
      }
    }

    "there are many unparsed Notification" should {
      "return those Notifications" in {
        val anotherUnparsableNotification =
          notificationUnparsed.copy(_id = ObjectId.get, conversationId = CommonTestData.conversationId_4)
        val unparsedNotifications = Seq(notificationUnparsed, anotherUnparsableNotification)
        repo.insertOne(notification_1).futureValue.isRight must be(true)
        unparsedNotifications.map(repo.insertOne(_).futureValue)

        val foundNotifications = repo.findUnparsedNotifications().futureValue

        foundNotifications.length mustBe unparsedNotifications.length
        unparsedNotifications.foreach { notification =>
          foundNotifications must contain(notification)
        }
      }
    }
  }

  "Notification Repository on update" should {

    "update the document with given _id" when {

      "the update notification is the same" in {
        repo.insertOne(notification_1).futureValue.isRight must be(true)

        val updatedNotification = repo.update(notification_1).futureValue

        updatedNotification mustBe defined
        updatedNotification.get mustBe notification_1
      }

      "the update notification is different" in {
        repo.insertOne(notification_1).futureValue.isRight must be(true)
        val update = notification_2.copy(_id = notification_1._id)

        val updatedNotification = repo.update(update).futureValue

        updatedNotification mustBe defined
        updatedNotification.get mustBe update
      }
    }

    "not create a new document if there is no document matching given _id" in {
      val updatedNotification = repo.update(notification_1).futureValue
      updatedNotification must not be defined
    }
  }

  private def equalWithoutId(notification: Notification): Matcher[Notification] = new Matcher[Notification] {
    def actualContentWas(notif: Notification): String =
      if (notif == null) {
        "Element did not exist"
      } else {
        s"\nActual content is:\n${notif}\n"
      }

    override def apply(left: Notification): MatchResult = {
      def compare: Boolean = {
        val id = ObjectId.get
        val leftNoId = left.copy(_id = id)
        val rightNoId = notification.copy(_id = id)

        leftNoId == rightNoId
      }

      MatchResult(
        left != null && compare,
        s"Notification is not equal to {$notification}\n${actualContentWas(left)}",
        s"Notification is equal to: {$notification}"
      )
    }
  }
}
