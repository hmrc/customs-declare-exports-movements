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

package unit.uk.gov.hmrc.exports.movements.models.notifications

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.{Entry, NotificationFrontendModel, UcrBlock}
import utils.testdata.CommonTestData.ucr
import utils.testdata.notifications.NotificationTestData.notification_1

class NotificationFrontendModelSpec extends WordSpec with MustMatchers {

  "NotificationFrontendModel on apply" when {

    "provided with Notification" which {

      "contains no MUCR or DUCR" should {
        "return NotificationFrontendModel without entries" in {
          val notification = notification_1

          val result = NotificationFrontendModel(notification)

          result.entries mustBe empty
        }
      }

      "contains only DUCR" should {
        "return NotificationFrontendModel with entry for this DUCR" in {
          val notification =
            notification_1.copy(data = notification_1.data.copy(entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "D"))))))

          val result = NotificationFrontendModel(notification)

          result.entries.length mustBe 1

          result.entries.head.ucrBlock mustBe defined
          result.entries.head.ucrBlock.get.ucr mustBe ucr
          result.entries.head.ucrBlock.get.ucrType mustBe "D"
        }
      }

      "contains only MUCR" should {
        "return NotificationFrontendModel with entry for this MUCR" in {
          val notification =
            notification_1.copy(data = notification_1.data.copy(masterUcr = Some(ucr), masterRoe = Some("ROE"), masterSoe = Some("SOE")))

          val result = NotificationFrontendModel(notification)

          result.entries.length mustBe 1

          result.entries.head.ucrBlock mustBe defined
          result.entries.head.ucrBlock.get.ucr mustBe ucr
          result.entries.head.ucrBlock.get.ucrType mustBe "M"
          result.entries.head.entryStatus mustBe defined
          result.entries.head.entryStatus.get.roe mustBe Some("ROE")
          result.entries.head.entryStatus.get.soe mustBe Some("SOE")
        }
      }

      "contains both MUCR and DUCR" should {
        "return NotificationFrontendModel with entries for them" in {
          val notification = notification_1.copy(
            data = notification_1.data.copy(
              entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "D")))),
              masterUcr = Some(ucr),
              masterRoe = Some("ROE"),
              masterSoe = Some("SOE")
            )
          )

          val result = NotificationFrontendModel(notification)

          result.entries.length mustBe 2

          result.entries.head.ucrBlock mustBe defined
          result.entries.head.ucrBlock.get.ucr mustBe ucr
          result.entries.head.ucrBlock.get.ucrType mustBe "M"
          result.entries.head.entryStatus mustBe defined
          result.entries.head.entryStatus.get.roe mustBe Some("ROE")
          result.entries.head.entryStatus.get.soe mustBe Some("SOE")

          result.entries(1).ucrBlock mustBe defined
          result.entries(1).ucrBlock.get.ucr mustBe ucr
          result.entries(1).ucrBlock.get.ucrType mustBe "D"
        }
      }
    }
  }

}
