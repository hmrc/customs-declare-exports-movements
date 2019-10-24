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

package uk.gov.hmrc.exports.movements.models.movements

import play.api.libs.json.Json
import uk.gov.hmrc.exports.movements.controllers.util.JSONResponses

case class MovementRequest(
  eori: String,
  providerId: Option[String] = None,
  choice: String,
  consignmentReference: ConsignmentReference,
  movementDetails: MovementDetails,
  location: Option[Location] = None,
  arrivalReference: Option[ArrivalReference] = None,
  transport: Option[Transport] = None
)

object MovementRequest extends JSONResponses {
  implicit val format = Json.format[MovementRequest]
}