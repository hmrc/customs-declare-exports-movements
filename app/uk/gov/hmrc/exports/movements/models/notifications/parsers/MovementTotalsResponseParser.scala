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

import uk.gov.hmrc.exports.movements.models.notifications._

import scala.xml.NodeSeq

class MovementTotalsResponseParser extends ResponseParser {

  override def parse(responseXml: NodeSeq): NotificationData = NotificationData(
    messageCode = stringOption(responseXml \ XmlTags.messageCode),
    crcCode = stringOption(responseXml \ XmlTags.crc),
    declarationCount = stringOption(responseXml \ XmlTags.declarationCount).map(_.toInt),
    entries = buildEntriesTotalsResponse(responseXml),
    goodsArrivalDateTime = stringOption(responseXml \ XmlTags.goodsArrivalDateTime),
    goodsLocation = stringOption(responseXml \ XmlTags.goodsLocation),
    masterRoe = stringOption(responseXml \ XmlTags.masterROE),
    masterSoe = stringOption(responseXml \ XmlTags.masterSOE),
    masterUcr = stringOption(responseXml \ XmlTags.masterUCR),
    movementReference = stringOption(responseXml \ XmlTags.movementReference)
  )

  private def buildEntriesTotalsResponse(xml: NodeSeq): Seq[Entry] = (xml \ XmlTags.entry).map { entry =>
    Entry(
      ucrBlock = (entry \ XmlTags.ucrBlock).map { ucrBlockNode =>
        UcrBlock(ucr = (ucrBlockNode \ XmlTags.ucr).text, ucrType = (ucrBlockNode \ XmlTags.ucrType).text)
      }.headOption,
      goodsItem = (entry \ XmlTags.goodsItem).map { goodsItemNode =>
        GoodsItem(
          commodityCode = stringOption(goodsItemNode \ XmlTags.commodityCode).map(_.toInt),
          totalPackages = stringOption(goodsItemNode \ XmlTags.totalPackages).map(_.toInt),
          totalNetMass = stringOption(goodsItemNode \ XmlTags.totalNetMass).map(BigDecimal(_))
        )
      },
      entryStatus = (entry \ XmlTags.entryStatus).map { entryStatusNode =>
        EntryStatus(
          ics = stringOption(entryStatusNode \ XmlTags.ics),
          roe = stringOption(entryStatusNode \ XmlTags.roe),
          soe = stringOption(entryStatusNode \ XmlTags.soe)
        )
      }.headOption
    )
  }

  private def stringOption(node: NodeSeq): Option[String] = if (node.text.trim.nonEmpty) Some(node.text) else None
}
