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

package uk.gov.hmrc.exports.movements.models.notifications.parsers

import java.time.Instant

import javax.inject.Inject
import uk.gov.hmrc.exports.movements.models.XmlTags
import uk.gov.hmrc.exports.movements.models.notifications.standard.{Entry, StandardNotificationData}

import scala.xml.NodeSeq

class MovementTotalsResponseParser @Inject()(commonTypesParser: CommonTypesParser) extends ResponseParser[StandardNotificationData] {

  override def parse(responseXml: NodeSeq): StandardNotificationData = StandardNotificationData(
    messageCode = StringOption((responseXml \ XmlTags.messageCode).text),
    crcCode = StringOption((responseXml \ XmlTags.crc).text),
    declarationCount = StringOption((responseXml \ XmlTags.declarationCount).text).map(_.toInt),
    entries = buildEntriesTotalsResponse(responseXml),
    goodsArrivalDateTime = StringOption((responseXml \ XmlTags.goodsArrivalDateTime).text).map(Instant.parse),
    goodsLocation = StringOption((responseXml \ XmlTags.goodsLocation).text),
    masterRoe = StringOption((responseXml \ XmlTags.masterROE).text),
    masterSoe = StringOption((responseXml \ XmlTags.masterSOE).text),
    masterUcr = StringOption((responseXml \ XmlTags.masterUCR).text),
    movementReference = StringOption((responseXml \ XmlTags.movementReference).text)
  )

  private def buildEntriesTotalsResponse(xml: NodeSeq): Seq[Entry] = (xml \ XmlTags.entry).map { entry =>
    Entry(
      ucrBlock = (entry \ XmlTags.ucrBlock).map(commonTypesParser.parseUcrBlock).headOption,
      goodsItem = (entry \ XmlTags.goodsItem).map(commonTypesParser.parseGoodsItem),
      entryStatus = (entry \ XmlTags.entryStatus).map(commonTypesParser.parseEntryStatus).headOption
    )
  }

}
