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
  entries: Seq[Entry],
  crcCode: Option[String],
  actionCode: Option[String],
  errorCodes: Seq[String],
  messageCode: String
)

object NotificationFrontendModel {
  implicit val format = Json.format[NotificationFrontendModel]

  def apply(notification: Notification): NotificationFrontendModel = {
    val mucrEntry = buildMucrEntry(notification)

    NotificationFrontendModel(
      timestampReceived = notification.timestampReceived,
      conversationId = notification.conversationId,
      responseType = notification.responseType,
      entries = mucrEntry.toSeq ++ notification.entries,
      crcCode = notification.crcCode,
      actionCode = notification.actionCode,
      errorCodes = notification.errorCodes,
      messageCode = notification.messageCode.getOrElse("")
    )
  }

  private def buildMucrEntry(notification: Notification): Option[Entry] =
    notification.masterUcr.map { ucr =>
      Entry(
        ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "M")),
        entryStatus = Some(EntryStatus(roe = notification.masterRoe, soe = notification.masterSoe, ics = None)),
        goodsItem = Seq.empty
      )
    }
}
