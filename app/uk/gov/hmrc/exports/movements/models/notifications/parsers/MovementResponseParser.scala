/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.Instant

import javax.inject.Inject
import uk.gov.hmrc.exports.movements.models.XmlTags
import uk.gov.hmrc.exports.movements.models.notifications.standard.{Entry, StandardNotificationData}

import scala.xml.NodeSeq

class MovementResponseParser @Inject() (commonTypesParser: CommonTypesParser) extends ResponseParser[StandardNotificationData] {

  override val responseTypeIle: String = "inventoryLinkingMovementResponse"

  override def parse(responseXml: NodeSeq): StandardNotificationData =
    StandardNotificationData(
      messageCode = StringOption((responseXml \ XmlTags.messageCode).text),
      crcCode = StringOption((responseXml \ XmlTags.crc).text),
      entries = buildEntries(responseXml),
      goodsArrivalDateTime = StringOption((responseXml \ XmlTags.goodsArrivalDateTime).text).map(Instant.parse),
      goodsLocation = StringOption((responseXml \ XmlTags.goodsLocation).text),
      movementReference = StringOption((responseXml \ XmlTags.movementReference).text),
      responseType = responseTypeIle
    )

  private def buildEntries(responseXml: NodeSeq): Seq[Entry] = {
    val ucrBlock =
      (responseXml \ XmlTags.ucrBlock).map(commonTypesParser.parseUcrBlock).headOption

    val goodsItem = (responseXml \ XmlTags.goodsItem).map(commonTypesParser.parseGoodsItem)

    val entryStatus = (responseXml \ XmlTags.entryStatus).map(commonTypesParser.parseEntryStatus).headOption

    (ucrBlock, goodsItem, entryStatus) match {
      case (None, Nil, None) => Seq.empty
      case _                 => Seq(Entry(ucrBlock = ucrBlock, goodsItem = goodsItem, entryStatus = entryStatus))
    }
  }

}
