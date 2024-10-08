/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.exports.movements.controllers.util.JSONResponses
import uk.gov.hmrc.exports.movements.models.UserIdentification
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType._
import uk.gov.hmrc.play.json.Union

sealed abstract class ConsolidationRequest(
  val consolidationType: ConsolidationType,
  override val eori: String,
  override val providerId: Option[String],
  val mucrOpt: Option[String],
  val ucrOpt: Option[String]
) extends UserIdentification

object ConsolidationRequest extends JSONResponses {
  implicit val associateDucrFormat: OFormat[AssociateDucrRequest] = Json.format[AssociateDucrRequest]
  implicit val disassociateDucrFormat: OFormat[DisassociateDucrRequest] = Json.format[DisassociateDucrRequest]
  implicit val associateMucrFormat: OFormat[AssociateMucrRequest] = Json.format[AssociateMucrRequest]
  implicit val disassociateMucrFormat: OFormat[DisassociateMucrRequest] = Json.format[DisassociateMucrRequest]
  implicit val associateDucrPartFormat: OFormat[AssociateDucrPartRequest] = Json.format[AssociateDucrPartRequest]
  implicit val disassociateDucrPartRequest: OFormat[DisassociateDucrPartRequest] = Json.format[DisassociateDucrPartRequest]
  implicit val shutMucrFormat: OFormat[ShutMucrRequest] = Json.format[ShutMucrRequest]

  implicit val format: OFormat[ConsolidationRequest] = Union
    .from[ConsolidationRequest](typeField = "consolidationType")
    .and[AssociateDucrRequest](typeTag = DucrAssociation.typeName)
    .and[DisassociateDucrRequest](typeTag = DucrDisassociation.typeName)
    .and[AssociateMucrRequest](typeTag = MucrAssociation.typeName)
    .and[DisassociateMucrRequest](typeTag = MucrDisassociation.typeName)
    .and[AssociateDucrPartRequest](typeTag = DucrPartAssociation.typeName)
    .and[DisassociateDucrPartRequest](typeTag = DucrPartDisassociation.typeName)
    .and[ShutMucrRequest](typeTag = ShutMucr.typeName)
    .format

  case class AssociateDucrRequest(override val eori: String, override val providerId: Option[String] = None, mucr: String, ucr: String)
      extends ConsolidationRequest(DucrAssociation, eori, providerId, Some(mucr), Some(ucr))

  case class DisassociateDucrRequest(override val eori: String, override val providerId: Option[String] = None, ucr: String)
      extends ConsolidationRequest(DucrDisassociation, eori, providerId, None, Some(ucr))

  case class AssociateMucrRequest(override val eori: String, override val providerId: Option[String] = None, mucr: String, ucr: String)
      extends ConsolidationRequest(MucrAssociation, eori, providerId, Some(mucr), Some(ucr))

  case class DisassociateMucrRequest(override val eori: String, override val providerId: Option[String] = None, ucr: String)
      extends ConsolidationRequest(MucrDisassociation, eori, providerId, None, Some(ucr))

  case class AssociateDucrPartRequest(override val eori: String, override val providerId: Option[String] = None, mucr: String, ucr: String)
      extends ConsolidationRequest(DucrPartAssociation, eori, providerId, Some(mucr), Some(ucr))

  case class DisassociateDucrPartRequest(override val eori: String, override val providerId: Option[String] = None, ucr: String)
      extends ConsolidationRequest(DucrPartDisassociation, eori, providerId, None, Some(ucr))

  case class ShutMucrRequest(override val eori: String, override val providerId: Option[String] = None, mucr: String)
      extends ConsolidationRequest(ShutMucr, eori, providerId, Some(mucr), None)
}
