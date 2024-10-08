/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.exports.movements.models.notifications.exchange

import java.time.Instant
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.exports.movements.models.notifications.Notification

case class IleQueryResponseExchange(
  timestampReceived: Instant,
  conversationId: String,
  responseType: String,
  data: Option[IleQueryResponseExchangeData]
)

object IleQueryResponseExchange {
  implicit val format: OFormat[IleQueryResponseExchange] = Json.format[IleQueryResponseExchange]

  def apply(notification: Notification): IleQueryResponseExchange = new IleQueryResponseExchange(
    timestampReceived = notification.timestampReceived,
    conversationId = notification.conversationId,
    responseType = notification.data.map(_.responseType).getOrElse(""),
    data = notification.data.map(IleQueryResponseExchangeData(_))
  )

}
