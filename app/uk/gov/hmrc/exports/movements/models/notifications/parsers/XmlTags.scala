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

package uk.gov.hmrc.exports.movements.models.notifications.parsers

private[parsers] object XmlTags {
  val messageCode = "messageCode"
  val actionCode = "actionCode"
  val crc = "crc"
  val declarationCount = "declarationCount"
  val goodsArrivalDateTime = "goodsArrivalDateTime"
  val goodsLocation = "goodsLocation"
  val movementReference = "movementReference"
  val ucrBlock = "ucrBlock"
  val ucr = "ucr"
  val ucrType = "ucrType"

  val goodsItem = "goodsItem"
  val commodityCode = "commodityCode"
  val totalPackages = "totalPackages"
  val totalNetMass = "totalNetMass"

  val entryStatus = "entryStatus"
  val ics = "ics"
  val roe = "roe"
  val soe = "soe"

  val masterUCR = "masterUCR"
  val masterROE = "masterROE"
  val masterSOE = "masterSOE"

  val entry = "entry"
  val submitRole = "submitRole"

  val error = "error"
  val errorCode = "errorCode"
}
