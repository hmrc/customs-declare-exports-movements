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

import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Codec
import uk.gov.hmrc.exports.movements.models.movements._

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

object MovementRequest {

  implicit val format = Json.format[MovementRequest]

  implicit def jsonWritable[A](implicit writes: Writes[A], codec: Codec): Writeable[A] = {
    implicit val contentType: ContentTypeOf[A] = ContentTypeOf[A](Some(ContentTypes.JSON))
    val transform = Writeable.writeableOf_JsValue.transform compose writes.writes
    Writeable(transform)
  }

}
