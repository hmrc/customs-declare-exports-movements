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

package unit.uk.gov.hmrc.exports.movements.models.notifications.parsers

import org.scalatest.MustMatchers
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationData}

object NotificationsComparator extends MustMatchers {

  def assertEquality(actual: Notification, expected: Notification): Unit = {
    actual.conversationId must equal(expected.conversationId)
    actual.responseType must equal(expected.responseType)
    actual.payload must equal(expected.payload)

    assertEquality(actual.data, expected.data)
  }

  def assertEquality(actual: NotificationData, expected: NotificationData): Unit = {
    actual.messageCode must equal(expected.messageCode)
    actual.actionCode must equal(expected.actionCode)
    actual.crcCode must equal(expected.crcCode)
    actual.declarationCount must equal(expected.declarationCount)
    actual.entries must equal(expected.entries)
    actual.errorCode must equal(expected.errorCode)
    actual.goodsArrivalDateTime must equal(expected.goodsArrivalDateTime)
    actual.goodsLocation must equal(expected.goodsLocation)
    actual.masterRoe must equal(expected.masterRoe)
    actual.masterSoe must equal(expected.masterSoe)
    actual.masterUcr must equal(expected.masterUcr)
    actual.movementReference must equal(expected.movementReference)
  }
}
