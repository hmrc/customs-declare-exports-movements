/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.exports.movements.services.audit

import play.api.libs.json.{JsObject, Json}
import play.api.Logging
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.exports.movements.models.notifications.standard.StandardNotificationData
import uk.gov.hmrc.exports.movements.services.audit.AuditTypes.NotificationProcessed

object AuditNotifications extends Logging {

  private val empty = Map.empty[String, String]

  def audit(notification: Notification, conversationId: String, auditService: AuditService): Unit =
    notification.data.map {
      case standardNoteData: StandardNotificationData =>
        val baseData: Map[String, String] =
          Map(EventData.payload.toString -> notification.payload, EventData.conversationId.toString -> conversationId)

        val messageCode = standardNoteData.messageCode.fold(empty)(v => Map(EventData.messageCode.toString -> v))
        val crc = standardNoteData.crcCode.fold(empty)(v => Map(EventData.crc.toString -> v))
        val goodsLocation = standardNoteData.goodsLocation.fold(empty)(v => Map(EventData.goodsLocationCode.toString -> v))
        val actionCode = standardNoteData.actionCode.fold(empty)(v => Map(EventData.actionCode.toString -> v))
        val masterUcr = standardNoteData.masterUcr.fold(empty)(v => Map(EventData.masterUcr.toString -> v))
        val soeAndRoe = getSoeANdRoe(standardNoteData)
        val ucrData = getUcr(standardNoteData)

        val auditData = baseData ++ messageCode ++ crc ++ goodsLocation ++ actionCode ++ masterUcr ++ soeAndRoe ++ ucrData
        auditService.auditNotificationProcessed(
          NotificationProcessed,
          mergeErrorCodes(standardNoteData.errorCodes, Json.toJson(auditData).as[JsObject])
        )

      case x =>
        logger.debug("Query response notification. Not logging these type of events")
    }

  private def mergeErrorCodes(errorCodes: Seq[String], payload: JsObject) =
    if (errorCodes.isEmpty) payload
    else {
      val arrayField = Json.toJson(Map(EventData.errorCodes.toString -> errorCodes)).as[JsObject]
      payload.deepMerge(arrayField)
    }

  private def getSoeANdRoe(standardNoteData: StandardNotificationData): Map[String, String] = {
    val soeAndRoe = for {
      entry <- standardNoteData.entries.headOption
      entryStatus <- entry.entryStatus
      soe <- entryStatus.soe
      roe <- entryStatus.roe
    } yield (Map(EventData.soe.toString -> soe, EventData.roe.toString -> roe))

    soeAndRoe.getOrElse(empty)
  }

  private def getUcr(standardNoteData: StandardNotificationData): Map[String, String] = {

    val ucrData = for {
      entry <- standardNoteData.entries.headOption
      ucrBlock <- entry.ucrBlock
    } yield (
      Map(
        EventData.ucrType.toString -> getUCRTypeString(ucrBlock.ucrType),
        EventData.ucr.toString -> s"${ucrBlock.ucr} ${ucrBlock.ucrPartNo.getOrElse("")}".trim
      )
    )

    ucrData.getOrElse(empty)
  }

  private def getUCRTypeString(ucrType: String): String =
    ucrType match {
      case "D" => "DUCR"
      case "M" => "MUCR"
    }
}
