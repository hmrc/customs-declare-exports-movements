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

import org.bson.types.ObjectId
import testdata.CommonTestData.{conversationId, conversationId_2}
import testdata.notifications.NotificationTestData._

class IleQueryResponseRepositoryISpec extends RepositoryISpec {

  private val repo = app.injector.instanceOf[IleQueryResponseRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll.futureValue
  }

  "IleQueryResponseRepository on insert" when {

    "provided with a Notification" should {
      "result in a success" in {
        repo.insertOne(ileQueryResponse_1).futureValue.isRight must be(true)

        val notificationInDB = repo.findAll.futureValue

        notificationInDB.length must equal(1)
        notificationInDB.head must equalWithoutId(ileQueryResponse_1)
      }
    }

    "trying to insert the same Notification twice" should {
      "result in having both Notifications persisted" in {
        repo.insertOne(ileQueryResponse_1).futureValue.isRight must be(true)
        repo.insertOne(ileQueryResponse_1.copy(_id = ObjectId.get)).futureValue.isRight must be(true)

        val notificationsInDB = repo.findAll.futureValue

        notificationsInDB.length must equal(2)
        notificationsInDB.head must equalWithoutId(ileQueryResponse_1)
        notificationsInDB(1) must equalWithoutId(ileQueryResponse_1)
      }
    }

    "trying to insert different Notifications but with the same Conversation ID" should {
      "result in having all Notifications persisted" in {
        val notificationsToInsert = List(ileQueryResponse_1, ileQueryResponse_2.copy(conversationId = conversationId))
        notificationsToInsert.foreach(repo.insertOne(_).futureValue.isRight must be(true))

        val notificationsInDB = repo.findAll.futureValue

        notificationsInDB.length must equal(2)
        notificationsToInsert.foreach(notificationsInDB must contain(_))
      }
    }
  }

  "IleQueryResponseRepository on findByConversationIds" when {

    "there are no Notifications with given conversationIds" should {
      "return empty list" in {
        repo.findByConversationIds(List(conversationId, conversationId_2)).futureValue must equal(List.empty)
      }
    }

    "there is a single Notification with one of given conversationIds" should {
      "return this Notification only" in {
        repo.insertOne(ileQueryResponse_1).futureValue

        val foundNotifications = repo.findByConversationIds(List(conversationId, conversationId_2)).futureValue

        foundNotifications.length must equal(1)
        foundNotifications.head must equalWithoutId(ileQueryResponse_1)
      }
    }

    "there are multiple Notifications with given conversationIds" should {
      "return all the Notifications" in {
        val notifications = List(ileQueryResponse_1, ileQueryResponse_2.copy(_id = ObjectId.get, conversationId = ileQueryResponse_1.conversationId))
        notifications.map(repo.insertOne(_).futureValue)

        val foundNotifications = repo.findByConversationIds(List(conversationId, conversationId_2)).futureValue

        foundNotifications.length must equal(notifications.length)
        notifications.foreach(foundNotifications must contain(_))
      }
    }
  }
}
