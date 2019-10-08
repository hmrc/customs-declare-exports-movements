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

package uk.gov.hmrc.exports.movements.services

import javax.inject.Singleton
import uk.gov.hmrc.exports.movements.controllers.request.MovementRequest
import uk.gov.hmrc.exports.movements.models.movements.Choice.{Arrival, Departure}
import uk.gov.hmrc.exports.movements.models.movements.{MovementDetails, Transport}
import uk.gov.hmrc.wco.dec.inventorylinking.common.{TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.xml.Node

@Singleton
class WCOMapper {

  def generateInventoryLinkingMovementRequestXml(request: MovementRequest): Node =
    xml.XML.loadString(generateInventoryLinkingMovementRequest(request).toXml)

  private def generateInventoryLinkingMovementRequest(request: MovementRequest): InventoryLinkingMovementRequest = {
    val departureDetails = request.choice match {
      case Departure => Some(request.movementDetails)
      case _         => None
    }

    val arrivalDetails: Option[MovementDetails] = request.choice match {
      case Arrival => Some(request.movementDetails)
      case _       => None
    }

    InventoryLinkingMovementRequest(
      messageCode = request.choice,
      agentDetails = None,
      ucrBlock =
        UcrBlock(ucr = request.consignmentReference.referenceValue, ucrType = request.consignmentReference.reference),
      goodsLocation = request.location.map(_.asString).getOrElse(""),
      goodsArrivalDateTime = arrivalDetails.map(_.dateTime),
      goodsDepartureDateTime = departureDetails.map(_.dateTime),
      transportDetails = mapTransportDetails(request.transport),
      movementReference = request.arrivalReference.flatMap(_.reference)
    )
  }

  private def mapTransportDetails(transport: Option[Transport]): Option[TransportDetails] =
    transport.map(
      data =>
        TransportDetails(
          transportID = Some(data.transportId),
          transportMode = Some(data.modeOfTransport),
          transportNationality = Some(data.nationality)
      )
    )
}
