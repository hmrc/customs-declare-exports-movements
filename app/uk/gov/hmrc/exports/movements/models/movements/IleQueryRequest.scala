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

package uk.gov.hmrc.exports.movements.models.movements

import play.api.libs.json.Json
import uk.gov.hmrc.exports.movements.controllers.util.JSONResponses
import uk.gov.hmrc.exports.movements.models.UserIdentification
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock

case class IleQueryRequest(override val eori: String, override val providerId: Option[String], ucrBlock: UcrBlock) extends UserIdentification

object IleQueryRequest extends JSONResponses {

  implicit val format = Json.format[IleQueryRequest]
}
