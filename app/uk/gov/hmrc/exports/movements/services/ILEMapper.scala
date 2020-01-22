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

package uk.gov.hmrc.exports.movements.services

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationType._
import uk.gov.hmrc.exports.movements.models.movements.MovementType.{Arrival, Departure, RetrospectiveArrival}
import uk.gov.hmrc.exports.movements.models.movements.{Movement, Transport}
import uk.gov.hmrc.exports.movements.models.notifications.standard
import uk.gov.hmrc.wco.dec.inventorylinking.common.{TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.xml.{Node, NodeSeq}

@Singleton
class ILEMapper @Inject()(clock: Clock) {

  private val dateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def generateInventoryLinkingMovementRequestXml(request: Movement): Node =
    xml.XML.loadString(generateInventoryLinkingMovementRequest(request).toXml)

  private def generateInventoryLinkingMovementRequest(request: Movement): InventoryLinkingMovementRequest = {

    val departureDetails: Option[String] = request.choice match {
      case Departure => request.movementDetails.map(movement => formatOutputDateTime(parseDateTime(movement.dateTime)))
      case _         => None
    }

    val arrivalDetails: Option[String] = request.choice match {
      case Arrival              => request.movementDetails.map(movement => formatOutputDateTime(parseDateTime(movement.dateTime)))
      case RetrospectiveArrival => Some(formatOutputDateTime(Instant.now(clock)))
      case _                    => None
    }

    InventoryLinkingMovementRequest(
      messageCode = request.choice.value,
      agentDetails = None,
      ucrBlock = UcrBlock(ucr = request.consignmentReference.referenceValue, ucrType = request.consignmentReference.reference),
      goodsLocation = request.location.map(_.code).getOrElse(""),
      goodsArrivalDateTime = arrivalDetails,
      goodsDepartureDateTime = departureDetails,
      transportDetails = mapTransportDetails(request.transport),
      movementReference = request.arrivalReference.flatMap(_.reference)
    )
  }

  private def parseDateTime(dateTime: String): Instant = Instant.from(dateTimeFormatter.parse(dateTime))

  private def formatOutputDateTime(dateTime: Instant): String = dateTimeFormatter.format(dateTime.truncatedTo(ChronoUnit.SECONDS))

  private def mapTransportDetails(transport: Option[Transport]): Option[TransportDetails] =
    transport.map(
      data => TransportDetails(transportID = data.transportId, transportMode = data.modeOfTransport, transportNationality = data.nationality)
    )

  def generateConsolidationXml(consolidation: Consolidation): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{buildMessageCode(consolidation.consolidationType)}</messageCode>
        {buildMasterUcrNode(consolidation.mucrOpt)}
        {buildUcrBlockNode(consolidation.consolidationType, consolidation.ucrOpt)}
      </inventoryLinkingConsolidationRequest>
    }

  private def buildMessageCode(consolidationType: ConsolidationType): String = consolidationType match {
    case ASSOCIATE_DUCR | DISASSOCIATE_DUCR | ASSOCIATE_MUCR | DISASSOCIATE_MUCR => "EAC"
    case SHUT_MUCR                                                               => "CST"
  }

  private def buildMasterUcrNode(mucrOpt: Option[String]): NodeSeq =
    mucrOpt.map(mucr => <masterUCR>{mucr}</masterUCR>).getOrElse(NodeSeq.Empty)

  private def buildUcrBlockNode(consolidationType: ConsolidationType, ucrOpt: Option[String]): NodeSeq =
    ucrOpt.map { ucr =>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>{ucrType(consolidationType)}</ucrType>
      </ucrBlock>
    }.getOrElse(NodeSeq.Empty)

  private def ucrType(consolidationType: ConsolidationType): String = consolidationType match {
    case ASSOCIATE_DUCR | DISASSOCIATE_DUCR => "D"
    case ASSOCIATE_MUCR | DISASSOCIATE_MUCR => "M"
  }

  def generateIleQuery(ucrBlock: standard.UcrBlock): NodeSeq =
    scala.xml.Utility.trim {
      <inventoryLinkingQueryRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <queryUCR>
          <ucr>{ucrBlock.ucr}</ucr>
          <ucrType>{ucrBlock.ucrType}</ucrType>
        </queryUCR>
      </inventoryLinkingQueryRequest>
    }
}
