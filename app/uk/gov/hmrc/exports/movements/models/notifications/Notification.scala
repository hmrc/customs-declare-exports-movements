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

final case class Notification(
  timestampReceived: Instant = Instant.now(),
  conversationId: String,
  responseType: String,
  payload: String,
  data: NotificationData
) {
  def crcCode: Option[String] = data.crcCode
  def actionCode: Option[String] = data.actionCode
  def errorCodes: Seq[String] = data.errorCodes
  def entries: Seq[Entry] = data.entries
  def masterUcr: Option[String] = data.masterUcr
  def masterRoe: Option[String] = data.masterRoe
  def masterSoe: Option[String] = data.masterSoe
}

object Notification {
  implicit val format = Json.format[Notification]

  def empty = Notification(conversationId = "", responseType = "", payload = "", data = NotificationData())
}
