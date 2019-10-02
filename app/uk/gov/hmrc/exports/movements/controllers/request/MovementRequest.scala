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
import uk.gov.hmrc.exports.movements.models.movements._

case class MovementRequest(
  consignmentReference: ConsignmentReference,
  movementDetails: MovementDetails,
  location: Location,
  arrivalReference: Option[ArrivalReference],
  goodsDeparted: Option[GoodsDeparted]
) {
  def toMovementsDeclaration(id: String, eori: Eori): MovementDeclaration = MovementDeclaration(
    id = id,
    eori = eori,
    consignmentReference = this.consignmentReference,
    movementDetails = this.movementDetails,
    location = this.location,
    arrivalReference = this.arrivalReference,
    goodsDeparted = this.goodsDeparted
  )
}

object MovementRequest {
  implicit val format: OFormat[MovementRequest] = Json.format[MovementRequest]
}
