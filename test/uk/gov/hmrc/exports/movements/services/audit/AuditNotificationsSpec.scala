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

package uk.gov.hmrc.exports.movements.services.audit

import org.mockito.Mockito.{reset, verify, verifyNoInteractions}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Json
import uk.gov.hmrc.exports.movements.base.UnitSpec
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.exports.movements.models.notifications.queries.IleQueryResponseData
import uk.gov.hmrc.exports.movements.models.notifications.standard.{Entry, EntryStatus, StandardNotificationData, UcrBlock}
import uk.gov.hmrc.exports.movements.services.audit.AuditTypes.NotificationProcessed

class AuditNotificationsSpec extends UnitSpec with BeforeAndAfterEach {

  private val auditService = mock[AuditService]

  private val conversationId = "conv-id"

  "AuditNotifications" when {

    s"notification data is of type ${IleQueryResponseData.getClass}" should {
      "not call audit service" when {
        val notificationData = IleQueryResponseData(responseType = "inventoryLinkingQueryResponse")

        AuditNotifications.audit(Notification.empty.copy(data = Some(notificationData)), conversationId, auditService)

        verifyNoInteractions(auditService)
      }
    }

    s"notification data is of type ${StandardNotificationData.getClass}" should {
      "call audit service" in {

        val messageCode = "aMessageCode"
        val crcCode = "aCrcCode"
        val goodsLocationCode = "aGoodsLocationCode"
        val actionCode = "anActionCode"
        val masterUcr = "aMasterUcr"
        val errorCodes = Seq("err1", "err2")
        val ucr = "ucr"
        val soe = "soe"
        val roe = "roe"

        val notificationData = StandardNotificationData(
          responseType = "inventoryLinkingMovementResponse",
          messageCode = Option(messageCode),
          crcCode = Option(crcCode),
          goodsLocation = Option(goodsLocationCode),
          actionCode = Option(actionCode),
          masterUcr = Option(masterUcr),
          errorCodes = errorCodes,
          entries = Seq(Entry(ucrBlock = Option(UcrBlock(ucr, None, "D")), entryStatus = Option(EntryStatus(soe = Option(soe), roe = Option(roe)))))
        )

        AuditNotifications.audit(Notification.empty.copy(data = Some(notificationData)), conversationId, auditService)

        verify(auditService).auditNotificationProcessed(
          NotificationProcessed,
          Json.obj(
            "payload" -> "",
            "conversationId" -> conversationId,
            "messageCode" -> messageCode,
            "crc" -> crcCode,
            "goodsLocationCode" -> goodsLocationCode,
            "actionCode" -> actionCode,
            "masterUcr" -> masterUcr,
            "errorCodes" -> errorCodes,
            "ucr" -> ucr,
            "ucrType" -> "DUCR",
            "soe" -> soe,
            "roe" -> roe
          )
        )
      }
    }
  }

  override protected def afterEach(): Unit = {
    reset(auditService)
    super.afterEach()
  }
}
