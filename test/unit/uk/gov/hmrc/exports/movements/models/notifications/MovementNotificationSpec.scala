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

package unit.uk.gov.hmrc.exports.movements.models.notifications

import java.time.LocalDateTime

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.{MovementNotification, NotificationError}

class MovementNotificationSpec extends WordSpec with MustMatchers {

  import MovementNotificationSpec._

  "MovementNotification on equals" should {

    "be Reflexive" in {
      (notification_1 == notification_1) must be(true)
    }

    "be Symmetrical" in {
      (notification_1 == notification_2) must be(true)
      (notification_2 == notification_1) must be(true)
    }

    "be Transitive" in {
      (notification_1 == notification_2) must be(true)
      (notification_2 == notification_3) must be(true)
      (notification_1 == notification_3) must be(true)
    }

    "be Consistent" in {
      (notification_1 == notification_2) must be(true)
      (notification_1 == notification_2) must be(true)
      (notification_1 == notification_2) must be(true)
    }

    "return false" when {

      "comparing different Notifications" in {
        val differentNotification =
          MovementNotification(conversationId = "conversationId_XYZ", errors = Seq.empty, payload = "")
        (notification_1 == differentNotification) must be(false)
      }

      "return false when comparing " in {
        //noinspection ComparingUnrelatedTypes
        (notification_1 == "any string") must be(false)
      }
    }
  }

  "MovementNotification on hashCode" should {

    "return same hash code if two notifications are equal" in {
      notification_1.hashCode() must equal(notification_2.hashCode())
    }
  }

}

object MovementNotificationSpec {

  val notification_1 = MovementNotification(
    dateTimeReceived = LocalDateTime.of(1,1,1,1,1),
    conversationId = "conversationId",
    errors = Seq(NotificationError("01"), NotificationError("13")),
    payload = "PAYLOAD"
  )
  val notification_2 = MovementNotification(
    dateTimeReceived = LocalDateTime.of(1,1,1,2,2),
    conversationId = "conversationId",
    errors = Seq(NotificationError("01"), NotificationError("13")),
    payload = "PAYLOAD"
  )
  val notification_3 = MovementNotification(
    dateTimeReceived = LocalDateTime.of(1,3,3,3,2),
    conversationId = "conversationId",
    errors = Seq(NotificationError("01"), NotificationError("13")),
    payload = "PAYLOAD"
  )
}
