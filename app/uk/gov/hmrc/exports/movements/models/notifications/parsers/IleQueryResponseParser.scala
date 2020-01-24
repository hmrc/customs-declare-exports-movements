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
import uk.gov.hmrc.exports.movements.models.movements.Transport
import uk.gov.hmrc.exports.movements.models.notifications.queries._

import scala.xml.{Node, NodeSeq}

class IleQueryResponseParser @Inject()(commonTypesParser: CommonTypesParser) extends ResponseParser[IleQueryResponseData] {

  override def parse(responseXml: NodeSeq): IleQueryResponseData = IleQueryResponseData(
    queriedDucr = buildQueriedDucr(responseXml \ XmlTags.queriedDucr),
    queriedMucr = buildQueriedMucr(responseXml \ XmlTags.queriedMucr),
    parentMucr = buildParentMucr(responseXml \ XmlTags.parentMucr),
    childDucrs = buildChildDucrs(responseXml \ XmlTags.childDucr),
    childMucrs = buildChildMucrs(responseXml \ XmlTags.childMucr)
  )

  private def buildQueriedDucr(queriedDucrXml: NodeSeq): Option[DucrInfo] = queriedDucrXml.map(parseDucrObject).headOption
  private def buildQueriedMucr(queriedMucrXml: NodeSeq): Option[MucrInfo] = queriedMucrXml.map(parseMucrObject).headOption
  private def buildParentMucr(parentMucrXml: NodeSeq): Option[MucrInfo] = parentMucrXml.map(parseMucrObject).headOption
  private def buildChildDucrs(childDucrsXml: NodeSeq): Seq[DucrInfo] = childDucrsXml.map(parseDucrObject)
  private def buildChildMucrs(childMucrsXml: NodeSeq): Seq[MucrInfo] = childMucrsXml.map(parseMucrObject)

  private def parseDucrObject(ducrObjectXml: Node): DucrInfo = DucrInfo(
    ucr = (ducrObjectXml \ XmlTags.ucr.toUpperCase).text,
    parentMucr = StringOption((ducrObjectXml \ XmlTags.parentMucr).text),
    declarationId = (ducrObjectXml \ XmlTags.declarationId).text,
    entryStatus = (ducrObjectXml \ XmlTags.entryStatus).map(commonTypesParser.parseEntryStatus).headOption,
    movements = (ducrObjectXml \ XmlTags.movement).map(parseMovement),
    goodsItem = (ducrObjectXml \ XmlTags.goodsItem).map(parseGoodsItemInfo)
  )

  private def parseMucrObject(mucrObjectXml: Node): MucrInfo = MucrInfo(
    ucr = (mucrObjectXml \ XmlTags.ucr.toUpperCase).text,
    parentMucr = StringOption((mucrObjectXml \ XmlTags.parentMucr).text),
    entryStatus = (mucrObjectXml \ XmlTags.entryStatus).map(commonTypesParser.parseEntryStatus).headOption,
    isShut = StringOption((mucrObjectXml \ XmlTags.shut).text).map(_.toBoolean),
    movements = (mucrObjectXml \ XmlTags.movement).map(parseMovement)
  )

  private def parseMovement(movementXml: Node): MovementInfo = MovementInfo(
    messageCode = (movementXml \ XmlTags.messageCode).text,
    goodsLocation = (movementXml \ XmlTags.goodsLocation).text,
    goodsArrivalDateTime = StringOption((movementXml \ XmlTags.goodsArrivalDateTime).text).map(Instant.parse),
    goodsDepartureDateTime = StringOption((movementXml \ XmlTags.goodsDepartureDateTime).text).map(Instant.parse),
    movementReference = StringOption((movementXml \ XmlTags.movementReference).text),
    transportDetails = (movementXml \ XmlTags.transportDetails).map { transportDetailsNode =>
      Transport(
        modeOfTransport = StringOption((transportDetailsNode \ XmlTags.transportMode).text),
        nationality = StringOption((transportDetailsNode \ XmlTags.transportNationality).text),
        transportId = StringOption((transportDetailsNode \ XmlTags.transportId).text)
      )
    }.headOption
  )

  private def parseGoodsItemInfo(goodsItemXml: Node): GoodsItemInfo =
    GoodsItemInfo(totalPackages = (goodsItemXml \ XmlTags.totalPackages).map { totalPackagesNode =>
      totalPackagesNode.text.toInt
    }.headOption)

}
