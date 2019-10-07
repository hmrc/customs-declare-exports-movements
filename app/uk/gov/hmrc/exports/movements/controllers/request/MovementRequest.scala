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

package uk.gov.hmrc.exports.movements.controllers.request

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.exports.movements.models.Eori
import uk.gov.hmrc.exports.movements.models.movements.Choice.{Arrival, Departure}
import uk.gov.hmrc.exports.movements.models.movements._
import uk.gov.hmrc.wco.dec.inventorylinking.common.{TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

case class MovementRequest(
  choice: String,
  consignmentReference: ConsignmentReference,
  movementDetails: MovementDetails,
  location: Option[Location] = None,
  arrivalReference: Option[ArrivalReference] = None,
  goodsDeparted: Option[GoodsDeparted] = None,
  transport: Option[Transport] = None
) {
  def createMovementRequest(eori: Eori): InventoryLinkingMovementRequest = {

    val departureDetails = choice match {
      case Departure => Some(movementDetails)
      case _         => None
    }

    val arrivalDetails: Option[MovementDetails] = choice match {
      case Arrival => Some(movementDetails)
      case _       => None
    }

    InventoryLinkingMovementRequest(
      messageCode = choice,
      agentDetails = None,
      ucrBlock = UcrBlock(ucr = consignmentReference.referenceValue, ucrType = consignmentReference.reference),
      goodsLocation = location.map(_.asString).getOrElse(""),
      goodsArrivalDateTime = arrivalDetails.map(_.dateTime),
      goodsDepartureDateTime = departureDetails.map(_.dateTime),
      transportDetails = mapTransportDetails(transport),
      movementReference = arrivalReference.map(_.reference)
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

object MovementRequest {
  implicit val format: OFormat[MovementRequest] = Json.format[MovementRequest]
}
