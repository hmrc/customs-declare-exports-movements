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

import uk.gov.hmrc.exports.movements.models.XmlTags
import uk.gov.hmrc.exports.movements.models.notifications._

import scala.xml.NodeSeq

class MovementTotalsResponseParser extends ResponseParser {

  override def parse(responseXml: NodeSeq): NotificationData = NotificationData(
    messageCode = StringOption((responseXml \ XmlTags.messageCode).text),
    crcCode = StringOption((responseXml \ XmlTags.crc).text),
    declarationCount = StringOption((responseXml \ XmlTags.declarationCount).text).map(_.toInt),
    entries = buildEntriesTotalsResponse(responseXml),
    goodsArrivalDateTime = StringOption((responseXml \ XmlTags.goodsArrivalDateTime).text),
    goodsLocation = StringOption((responseXml \ XmlTags.goodsLocation).text),
    masterRoe = StringOption((responseXml \ XmlTags.masterROE).text),
    masterSoe = StringOption((responseXml \ XmlTags.masterSOE).text),
    masterUcr = StringOption((responseXml \ XmlTags.masterUCR).text),
    movementReference = StringOption((responseXml \ XmlTags.movementReference).text)
  )

  private def buildEntriesTotalsResponse(xml: NodeSeq): Seq[Entry] = (xml \ XmlTags.entry).map { entry =>
    Entry(
      ucrBlock = (entry \ XmlTags.ucrBlock).map { ucrBlockNode =>
        UcrBlock(ucr = (ucrBlockNode \ XmlTags.ucr).text, ucrType = (ucrBlockNode \ XmlTags.ucrType).text)
      }.headOption,
      goodsItem = (entry \ XmlTags.goodsItem).map { goodsItemNode =>
        GoodsItem(
          commodityCode = StringOption((goodsItemNode \ XmlTags.commodityCode).text).map(_.toInt),
          totalPackages = StringOption((goodsItemNode \ XmlTags.totalPackages).text).map(_.toInt),
          totalNetMass = StringOption((goodsItemNode \ XmlTags.totalNetMass).text).map(BigDecimal(_))
        )
      },
      entryStatus = (entry \ XmlTags.entryStatus).map { entryStatusNode =>
        EntryStatus(
          ics = StringOption((entryStatusNode \ XmlTags.ics).text),
          roe = StringOption((entryStatusNode \ XmlTags.roe).text),
          soe = StringOption((entryStatusNode \ XmlTags.soe).text)
        )
      }.headOption
    )
  }

}
