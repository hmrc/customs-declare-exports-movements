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

package uk.gov.hmrc.exports.movements.models.notifications.parsers

import javax.inject.Singleton
import uk.gov.hmrc.exports.movements.models.XmlTags
import uk.gov.hmrc.exports.movements.models.notifications.standard.{EntryStatus, GoodsItem, UcrBlock}

import scala.xml.Node

@Singleton
class CommonTypesParser {

  private val UcrBlockLabel = "ucrBlock"
  private val EntryStatusLabel = "entryStatus"
  private val GoodsItemLabel = "goodsItem"

  def parseUcrBlock(ucrBlockXml: Node): UcrBlock = {
    if (ucrBlockXml.head.label != UcrBlockLabel)
      throw new IllegalArgumentException(s"UcrBlock parser was provided with incorrect node type: ${ucrBlockXml.head.label}")

    UcrBlock(
      ucr = (ucrBlockXml \ XmlTags.ucr).text,
      ucrPartNo = StringOption((ucrBlockXml \ XmlTags.ucrPartNo).text),
      ucrType = (ucrBlockXml \ XmlTags.ucrType).text
    )
  }

  def parseEntryStatus(entryStatusXml: Node): EntryStatus = {
    if (entryStatusXml.head.label != EntryStatusLabel)
      throw new IllegalArgumentException(s"EntryStatus parser was provided with incorrect node type: ${entryStatusXml.head.label}")

    EntryStatus(
      ics = StringOption((entryStatusXml \ XmlTags.ics).text),
      roe = StringOption((entryStatusXml \ XmlTags.roe).text),
      soe = StringOption((entryStatusXml \ XmlTags.soe).text)
    )
  }

  def parseGoodsItem(goodsItemXml: Node): GoodsItem = {
    if (goodsItemXml.head.label != GoodsItemLabel)
      throw new IllegalArgumentException(s"GoodsItem parser was provided with incorrect node type: ${goodsItemXml.head.label}")

    GoodsItem(
      commodityCode = StringOption((goodsItemXml \ XmlTags.commodityCode).text).map(_.toInt),
      totalPackages = StringOption((goodsItemXml \ XmlTags.totalPackages).text).map(_.toInt),
      totalNetMass = StringOption((goodsItemXml \ XmlTags.totalNetMass).text).map(BigDecimal(_))
    )
  }

}
