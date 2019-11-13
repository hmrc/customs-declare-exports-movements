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

package uk.gov.hmrc.exports.movements.models.consolidation

import play.api.libs.json.Json
import uk.gov.hmrc.exports.movements.controllers.util.JSONResponses
import uk.gov.hmrc.exports.movements.models.UserIdentification
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationType._
import uk.gov.hmrc.play.json.Union

sealed abstract class ConsolidationRequest(
  val consolidationType: ConsolidationType,
  override val eori: String,
  override val providerId: Option[String],
  val mucrOpt: Option[String],
  val ucrOpt: Option[String]
) extends UserIdentification

object ConsolidationRequest extends JSONResponses {
  implicit val associateDucrFormat = Json.format[AssociateDucrRequest]
  implicit val disassociateDucrFormat = Json.format[DisassociateDucrRequest]
  implicit val associateMucrFormat = Json.format[AssociateMucrRequest]
  implicit val disassociateMucrFormat = Json.format[DisassociateMucrRequest]
  implicit val shutMucrFormat = Json.format[ShutMucrRequest]

  implicit val format = Union
    .from[ConsolidationRequest](typeField = "consolidationType")
    .and[AssociateDucrRequest](typeTag = ASSOCIATE_DUCR.toString)
    .and[DisassociateDucrRequest](typeTag = DISASSOCIATE_DUCR.toString)
    .and[AssociateMucrRequest](typeTag = ASSOCIATE_MUCR.toString)
    .and[DisassociateMucrRequest](typeTag = DISASSOCIATE_MUCR.toString)
    .and[ShutMucrRequest](typeTag = SHUT_MUCR.toString)
    .format

  case class AssociateDucrRequest(override val eori: String, override val providerId: Option[String] = None, mucr: String, ucr: String)
      extends ConsolidationRequest(ASSOCIATE_DUCR, eori, providerId, Some(mucr), Some(ucr))

  case class DisassociateDucrRequest(override val eori: String, override val providerId: Option[String] = None, ucr: String)
      extends ConsolidationRequest(DISASSOCIATE_DUCR, eori, providerId, None, Some(ucr))

  case class AssociateMucrRequest(override val eori: String, override val providerId: Option[String] = None, mucr: String, ucr: String)
      extends ConsolidationRequest(ASSOCIATE_MUCR, eori, providerId, Some(mucr), Some(ucr))

  case class DisassociateMucrRequest(override val eori: String, override val providerId: Option[String] = None, ucr: String)
      extends ConsolidationRequest(DISASSOCIATE_MUCR, eori, providerId, None, Some(ucr))

  case class ShutMucrRequest(override val eori: String, override val providerId: Option[String] = None, mucr: String)
      extends ConsolidationRequest(SHUT_MUCR, eori, providerId, Some(mucr), None)
}
