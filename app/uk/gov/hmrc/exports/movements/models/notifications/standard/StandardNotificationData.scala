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

package uk.gov.hmrc.exports.movements.models.notifications.standard

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.exports.movements.models.notifications.{NotificationData, NotificationType}

final case class StandardNotificationData(
  messageCode: Option[String] = None,
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
  errorCodes: Seq[String] = Seq.empty
) extends NotificationData {
  override val typ: NotificationType = NotificationType.StandardResponse
}

object StandardNotificationData {
  implicit val format: OFormat[StandardNotificationData] = Json.format[StandardNotificationData]
}
