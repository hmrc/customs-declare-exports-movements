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

package uk.gov.hmrc.exports.movements.models.notifications.exchange

import java.time.Instant

import play.api.libs.json.Json
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.exports.movements.models.notifications.standard.{Entry, EntryStatus, StandardNotificationData, UcrBlock}

final case class NotificationFrontendModel(
  timestampReceived: Instant,
  conversationId: String,
  responseType: String,
  entries: Seq[Entry],
  crcCode: Option[String],
  actionCode: Option[String],
  errorCodes: Seq[String],
  messageCode: String
)

object NotificationFrontendModel {
  implicit val format = Json.format[NotificationFrontendModel]

  def apply(notification: Notification): NotificationFrontendModel = notification.data match {
    case standardNotificationData: StandardNotificationData =>
      val mucrEntry = buildMucrEntry(standardNotificationData)

      NotificationFrontendModel(
        timestampReceived = notification.timestampReceived,
        conversationId = notification.conversationId,
        responseType = notification.responseType,
        entries = mucrEntry.toSeq ++ standardNotificationData.entries,
        crcCode = standardNotificationData.crcCode,
        actionCode = standardNotificationData.actionCode,
        errorCodes = standardNotificationData.errorCodes,
        messageCode = standardNotificationData.messageCode.getOrElse("")
      )

    case other => throw new IllegalStateException(s"Cannot build NotificationFrontendModel from ${other.typ} type")
  }

  private def buildMucrEntry(standardNotificationData: StandardNotificationData): Option[Entry] =
    standardNotificationData.masterUcr.map { ucr =>
      Entry(
        ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "M")),
        entryStatus = Some(EntryStatus(roe = standardNotificationData.masterRoe, soe = standardNotificationData.masterSoe, ics = None)),
        goodsItem = Seq.empty
      )
    }
}
