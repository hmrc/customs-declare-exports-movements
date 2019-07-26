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

package uk.gov.hmrc.exports.movements.models.notifications

import javax.inject.{Inject, Singleton}

import scala.xml.{NodeSeq, Utility, XML}

@Singleton
class NotificationFactory @Inject()() {

  // TODO: Implement Strategy pattern for various response types. This is also occasion to introduce more in depth testing for each parser.
  private val inventoryLinkingMovementResponse = "inventoryLinkingMovementResponse"
  private val inventoryLinkingMovementTotalsResponse = "inventoryLinkingMovementTotalsResponse"
  private val inventoryLinkingControlResponseLabel = "inventoryLinkingControlResponse"

  def buildMovementNotification(conversationId: String, xml: NodeSeq): Notification =
    if (xml.nonEmpty) {
      xml.head.label match {
        case `inventoryLinkingMovementResponse`       => buildFromMovementResponse(conversationId, xml)
        case `inventoryLinkingMovementTotalsResponse` => buildFromMovementTotalsResponse(conversationId, xml)
        case `inventoryLinkingControlResponseLabel`   => buildFromControlResponse(conversationId, xml)
        case unknownLabel                             => throw new IllegalArgumentException(s"Unknown Inventory Linking Response: $unknownLabel")
      }
    } else throw new IllegalArgumentException(s"Cannot find root element in: $xml")

  private def buildFromMovementResponse(conversationId: String, xml: NodeSeq): Notification =
    Notification(
      conversationId = conversationId,
      responseType = inventoryLinkingMovementResponse,
      // TODO: Change it to be faster and more elegant
      payload = Utility.trim(XML.loadString(xml.toString)).toString,
      data = NotificationData(
        messageCode = (xml \ Tags.messageCode).text,
        crcCode = stringOption(xml \ Tags.crc),
        entries = buildEntriesMovementResponse(xml),
        goodsArrivalDateTime = stringOption(xml \ Tags.goodsArrivalDateTime),
        goodsLocation = stringOption(xml \ Tags.goodsLocation),
        movementReference = stringOption(xml \ Tags.movementReference)
      )
    )

  private def buildEntriesMovementResponse(xml: NodeSeq): Seq[Entry] = {
    val ucrBlock =
      if ((xml \ Tags.ucrBlock).nonEmpty)
        Some(UcrBlock(ucr = (xml \ Tags.ucrBlock \ Tags.ucr).text, ucrType = (xml \ Tags.ucrBlock \ Tags.ucrType).text))
      else None

    val goodsItem = (xml \ Tags.goodsItem).map { goodsItemNode =>
      GoodsItem(
        commodityCode = stringOption(goodsItemNode \ Tags.commodityCode).map(_.toInt),
        totalPackages = stringOption(goodsItemNode \ Tags.totalPackages).map(_.toInt),
        totalNetMass = stringOption(goodsItemNode \ Tags.totalNetMass).map(BigDecimal(_))
      )
    }

    val entryStatus = if ((xml \ Tags.entryStatus).nonEmpty) {
      Some(
        EntryStatus(
          ics = stringOption(xml \ Tags.entryStatus \ Tags.ics),
          roe = stringOption(xml \ Tags.entryStatus \ Tags.roe),
          soe = stringOption(xml \ Tags.entryStatus \ Tags.soe)
        )
      )
    } else None

    if (ucrBlock.nonEmpty || goodsItem.nonEmpty || entryStatus.nonEmpty)
      Seq(Entry(ucrBlock = ucrBlock, goodsItem = goodsItem, entryStatus = entryStatus))
    else Seq.empty
  }

  private def buildFromMovementTotalsResponse(conversationId: String, xml: NodeSeq): Notification =
    Notification(
      conversationId = conversationId,
      responseType = inventoryLinkingMovementTotalsResponse,
      payload = Utility.trim(XML.loadString(xml.toString)).toString,
      data = NotificationData(
        messageCode = (xml \ Tags.messageCode).text,
        crcCode = stringOption(xml \ Tags.crc),
        declarationCount = stringOption(xml \ Tags.declarationCount).map(_.toInt),
        entries = buildEntriesTotalsResponse(xml),
        goodsArrivalDateTime = stringOption(xml \ Tags.goodsArrivalDateTime),
        goodsLocation = stringOption(xml \ Tags.goodsLocation),
        masterRoe = stringOption(xml \ Tags.masterROE),
        masterSoe = stringOption(xml \ Tags.masterSOE),
        masterUcr = stringOption(xml \ Tags.masterUCR),
        movementReference = stringOption(xml \ Tags.movementReference)
      )
    )

  private def buildEntriesTotalsResponse(xml: NodeSeq): Seq[Entry] = (xml \ Tags.entry).map { entry =>
    Entry(
      ucrBlock =
        if ((entry \ Tags.ucrBlock).nonEmpty)
          Some(
            UcrBlock(
              ucr = (entry \ Tags.ucrBlock \ Tags.ucr).text,
              ucrType = (entry \ Tags.ucrBlock \ Tags.ucrType).text
            )
          )
        else None,
      goodsItem = (entry \ Tags.goodsItem).map { goodsItemNode =>
        GoodsItem(
          commodityCode = stringOption(goodsItemNode \ Tags.commodityCode).map(_.toInt),
          totalPackages = stringOption(goodsItemNode \ Tags.totalPackages).map(_.toInt),
          totalNetMass = stringOption(goodsItemNode \ Tags.totalNetMass).map(BigDecimal(_))
        )
      },
      entryStatus = if ((entry \ Tags.entryStatus).nonEmpty) {
        Some(
          EntryStatus(
            ics = stringOption(entry \ Tags.entryStatus \ Tags.ics),
            roe = stringOption(entry \ Tags.entryStatus \ Tags.roe),
            soe = stringOption(entry \ Tags.entryStatus \ Tags.soe)
          )
        )
      } else None
    )
  }

  private def buildFromControlResponse(conversationId: String, xml: NodeSeq): Notification =
    Notification(
      conversationId = conversationId,
      payload = Utility.trim(XML.loadString(xml.toString)).toString,
      responseType = inventoryLinkingControlResponseLabel,
      data = NotificationData(
        messageCode = (xml \ Tags.messageCode).text,
        actionCode = stringOption(xml \ Tags.actionCode),
        entries = Seq(
          Entry(
            ucrBlock =
              Some(UcrBlock(ucr = (xml \ Tags.ucr \ Tags.ucr).text, ucrType = (xml \ Tags.ucr \ Tags.ucrType).text))
          )
        ),
        movementReference = stringOption(xml \ Tags.movementReference),
        errorCode = stringOption(xml \ Tags.error \ Tags.errorCode)
      )
    )

  private def stringOption(node: NodeSeq): Option[String] = if (node.text.trim.nonEmpty) Some(node.text) else None

  private object Tags {
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

}
