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

package uk.gov.hmrc.exports.movements.services

import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation
import uk.gov.hmrc.exports.movements.models.movements.{MovementsExchange, Transport}
import uk.gov.hmrc.exports.movements.models.notifications.standard
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.MovementType._
import uk.gov.hmrc.exports.movements.services.UcrBlockBuilder.{buildUcrBlock, buildUcrBlockNode}
import uk.gov.hmrc.wco.dec.inventorylinking.common.TransportDetails
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}
import javax.inject.{Inject, Singleton}
import scala.util.Random
import scala.xml.{Node, NodeSeq}

@Singleton
class IleMapper @Inject() (clock: Clock) {

  private val dateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def buildInventoryLinkingMovementRequestXml(request: MovementsExchange): Node =
    xml.XML.loadString(generateInventoryLinkingMovementRequest(request).toXml)

  private def generateInventoryLinkingMovementRequest(request: MovementsExchange): InventoryLinkingMovementRequest = {

    val departureDetails: Option[String] = request.choice match {
      case Departure => request.movementDetails.map(movement => formatOutputDateTime(parseDateTime(movement.dateTime)))
      case _         => None
    }

    val arrivalDetails: Option[String] = request.choice match {
      case Arrival                                => request.movementDetails.map(movement => formatOutputDateTime(parseDateTime(movement.dateTime)))
      case RetrospectiveArrival | CreateEmptyMucr => Some(formatOutputDateTime(Instant.now(clock)))
      case _                                      => None
    }

    val movementReference: Option[String] = request.choice match {
      case Arrival | RetrospectiveArrival | CreateEmptyMucr | Departure => Some(generateRandomReference)
      case _                                                            => None
    }

    InventoryLinkingMovementRequest(
      messageCode = request.choice.ileCode,
      agentDetails = None,
      ucrBlock = buildUcrBlock(request.consignmentReference).toWcoUcrBlock,
      goodsLocation = request.location.map(_.code).getOrElse(""),
      goodsArrivalDateTime = arrivalDetails,
      goodsDepartureDateTime = departureDetails,
      transportDetails = mapTransportDetails(request.transport),
      movementReference = movementReference
    )
  }

  private def parseDateTime(dateTime: String): Instant = Instant.from(dateTimeFormatter.parse(dateTime))

  private def formatOutputDateTime(dateTime: Instant): String = dateTimeFormatter.format(dateTime.truncatedTo(ChronoUnit.SECONDS))

  private def mapTransportDetails(transport: Option[Transport]): Option[TransportDetails] =
    transport.map(data =>
      TransportDetails(transportID = data.transportId, transportMode = data.modeOfTransport, transportNationality = data.nationality)
    )

  private def generateRandomReference: String = Random.alphanumeric.take(25).toList.mkString("")

  def buildConsolidationXml(consolidation: Consolidation): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{buildMessageCode(consolidation.consolidationType)}</messageCode>
        {buildMasterUcrNode(consolidation.mucrOpt)}
        {consolidation.ucrOpt.map(ucr => buildUcrBlockNode(consolidation.consolidationType, ucr)).getOrElse(NodeSeq.Empty)}
      </inventoryLinkingConsolidationRequest>
    }

  private def buildMessageCode(consolidationType: ConsolidationType): String = consolidationType.ileCode

  private def buildMasterUcrNode(mucrOpt: Option[String]): NodeSeq =
    mucrOpt.map(mucr => <masterUCR>{mucr}</masterUCR>).getOrElse(NodeSeq.Empty)

  def buildIleQuery(ucrBlock: standard.UcrBlock): NodeSeq =
    scala.xml.Utility.trim {
      <inventoryLinkingQueryRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <queryUCR>
          <ucr>{ucrBlock.ucr}</ucr>
          <ucrType>{ucrBlock.ucrType}</ucrType>
        </queryUCR>
      </inventoryLinkingQueryRequest>
    }
}
