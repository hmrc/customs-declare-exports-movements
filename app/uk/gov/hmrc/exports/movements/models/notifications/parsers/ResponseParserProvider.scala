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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.exports.movements.models.notifications.NotificationData

import scala.xml.NodeSeq

@Singleton
class ResponseParserProvider @Inject()(
  movementResponseParser: MovementResponseParser,
  movementTotalsResponseParser: MovementTotalsResponseParser,
  controlResponseParser: ControlResponseParser
) {

  private val inventoryLinkingMovementResponseLabel = "inventoryLinkingMovementResponse"
  private val inventoryLinkingMovementTotalsResponseLabel = "inventoryLinkingMovementTotalsResponse"
  private val inventoryLinkingControlResponseLabel = "inventoryLinkingControlResponse"

  def provideResponseParserContext(responseXml: NodeSeq): ResponseParserContext[NotificationData] =
    if (responseXml.nonEmpty)
      ResponseParserContext(responseXml.head.label, provideResponseParser(responseXml))
    else
      throw new IllegalArgumentException(s"Cannot find root element in: $responseXml")

  def provideResponseParser(responseXml: NodeSeq): ResponseParser[NotificationData] =
    if (responseXml.nonEmpty) {
      responseXml.head.label match {
        case `inventoryLinkingMovementResponseLabel`       => movementResponseParser
        case `inventoryLinkingMovementTotalsResponseLabel` => movementTotalsResponseParser
        case `inventoryLinkingControlResponseLabel`        => controlResponseParser
        case unknownLabel                                  => throw new IllegalArgumentException(s"Unknown Inventory Linking Response: $unknownLabel")
      }
    } else throw new IllegalArgumentException(s"Cannot find root element in: $responseXml")

}
