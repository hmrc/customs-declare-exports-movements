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

import play.api.libs.json.Json

final case class NotificationData(
  messageCode: Option[String] = None ,
  crcCode: Option[String] = None,
  declarationCount: Option[Int] = None,
  entries: Seq[Entry] = Seq.empty,
  goodsArrivalDateTime: Option[String] = None,
  goodsLocation: Option[String] = None,
  masterRoe: Option[String] = None,
  masterSoe: Option[String] = None,
  masterUcr: Option[String] = None,
  movementReference: Option[String] = None,

  actionCode: Option[String] = None,
  errorCode: Option[String] = None
)

object NotificationData {
  implicit val format = Json.format[NotificationData]

  def empty = NotificationData()
}
