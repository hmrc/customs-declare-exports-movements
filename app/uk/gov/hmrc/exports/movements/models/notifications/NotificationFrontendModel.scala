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

import java.time.Instant

import play.api.libs.json.Json

final case class NotificationFrontendModel(
  timestampReceived: Instant = Instant.now(),
  conversationId: String,
  responseType: String,
  ucrBlocks: Seq[UcrBlock],
  masterRoe: Option[String],
  masterSoe: Option[String],
  crcCode: Option[String],
  actionCode: Option[String],
  errorCodes: Seq[String]
)

object NotificationFrontendModel {
  implicit val format = Json.format[NotificationFrontendModel]

  def apply(notification: Notification): NotificationFrontendModel =
    NotificationFrontendModel(
      timestampReceived = notification.timestampReceived,
      conversationId = notification.conversationId,
      responseType = notification.responseType,
      ucrBlocks = notification.data.entries.flatMap(_.ucrBlock),
      masterRoe = notification.data.masterRoe,
      masterSoe = notification.data.masterSoe,
      crcCode = notification.data.crcCode,
      actionCode = notification.data.actionCode,
      errorCodes = notification.data.errorCodes
    )
}
