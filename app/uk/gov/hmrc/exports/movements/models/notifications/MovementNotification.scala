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

package uk.gov.hmrc.exports.movements.models.notifications

import java.time.LocalDateTime

import play.api.libs.json.Json

final case class MovementNotification(
  dateTimeReceived: LocalDateTime = LocalDateTime.now(),
  conversationId: String,
  errors: Seq[NotificationError],
  payload: String
) {

  override def equals(other: Any): Boolean = other match {
    case other: MovementNotification =>
      other.canEqual(this) &&
        this.conversationId == other.conversationId &&
        this.errors == other.errors &&
        this.payload == other.payload
    case _ => false
  }

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + conversationId.hashCode
    result = prime * result + errors.hashCode
    result = prime * result + payload.hashCode
    result
  }

}

object MovementNotification {
  implicit val format = Json.format[MovementNotification]

  def empty = MovementNotification(
    conversationId = "",
    errors = Seq.empty,
    payload = ""
  )
}
