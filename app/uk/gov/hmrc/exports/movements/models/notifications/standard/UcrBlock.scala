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

package uk.gov.hmrc.exports.movements.models.notifications.standard

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.wco.dec.inventorylinking.common.{UcrBlock => WcoUcrBlock}

final case class UcrBlock(ucr: String, ucrPartNo: Option[String] = None, ucrType: String) {

  def toWcoUcrBlock: WcoUcrBlock = WcoUcrBlock(ucr = ucr, ucrPartNo = ucrPartNo, ucrType = ucrType)
}

object UcrBlock {
  implicit val format: OFormat[UcrBlock] = Json.format[UcrBlock]
}
