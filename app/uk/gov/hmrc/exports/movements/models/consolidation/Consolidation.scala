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

import play.api.libs.json.{Format, Json, Reads, Writes}
import uk.gov.hmrc.exports.movements.models.submissions.ActionType
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.{DucrAssociation, DucrDisassociation, ShutMucr}
import uk.gov.hmrc.play.json.Union

object ConsolidationType extends Enumeration {
  type ConsolidationType = Value

  val ASSOCIATE_DUCR = Value("EAC")
  val DISASSOCIATE_DUCR = Value("EAC")
  val SHUT_MUCR = Value("CST")

  implicit val format = Format(Reads.enumNameReads(ConsolidationType), Writes.enumNameWrites)
}

import ConsolidationType._

sealed trait ConsolidationRequest {

  def consolidation(): Consolidation
}

case class AssociateDucrRequest(mucr: String, ducr: String) extends ConsolidationRequest {

  override def consolidation(): Consolidation = Consolidation(ASSOCIATE_DUCR, Some(mucr), Some(ducr), DucrAssociation)
}
case class DisassiociateDucrRequest(ducr: String) extends ConsolidationRequest {

  override def consolidation(): Consolidation = Consolidation(DISASSOCIATE_DUCR, None, Some(ducr), DucrDisassociation)
}
case class ShutMucrRequest(mucr: String) extends ConsolidationRequest {

  override def consolidation(): Consolidation = Consolidation(SHUT_MUCR, Some(mucr), None, ShutMucr)
}

object ConsolidationRequest {
  implicit val associateDucrFormat = Json.format[AssociateDucrRequest]
  implicit val disassiociateDucrFormat = Json.format[DisassiociateDucrRequest]
  implicit val shutMucrFormat = Json.format[ShutMucrRequest]

  implicit val format = Union
    .from[ConsolidationRequest](typeField = "type")
    .and[AssociateDucrRequest](typeTag = "associateDucr")
    .and[DisassiociateDucrRequest](typeTag = "disassociateDucr")
    .and[ShutMucrRequest](typeTag = "shutMucr")
    .format
}

case class Consolidation(
  consolidationType: ConsolidationType,
  mucr: Option[String],
  ducr: Option[String],
  actionType: ActionType
)

object Consolidation {

  implicit val format = Json.format[Consolidation]
}
