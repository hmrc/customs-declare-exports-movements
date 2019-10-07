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
import uk.gov.hmrc.play.json.Union

object ConsolidationType extends Enumeration {
  type ConsolidationType = Value

  val ASSOCIATE_DUCR = Value("EAC")
  val DISASSOCIATE_DUCR = Value("EAC")
  val SHUT_MUCR = Value("CST")

  implicit val format = Format(Reads.enumNameReads(ConsolidationType), Writes.enumNameWrites)
}

import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationType._

sealed abstract class Consolidation(
  val consolidationType: ConsolidationType,
  val mucrOpt: Option[String],
  val ducrOpt: Option[String]
)

case class AssociateDucrRequest(mucr: String, ducr: String)
    extends Consolidation(ASSOCIATE_DUCR, Some(mucr), Some(ducr))

case class DisassiociateDucrRequest(ducr: String) extends Consolidation(DISASSOCIATE_DUCR, None, Some(ducr))

case class ShutMucrRequest(mucr: String) extends Consolidation(SHUT_MUCR, Some(mucr), None)

object Consolidation {
  implicit val associateDucrFormat = Json.format[AssociateDucrRequest]
  implicit val disassiociateDucrFormat = Json.format[DisassiociateDucrRequest]
  implicit val shutMucrFormat = Json.format[ShutMucrRequest]

  implicit val format = Union
    .from[Consolidation](typeField = "type")
    .and[AssociateDucrRequest](typeTag = "associateDucr")
    .and[DisassiociateDucrRequest](typeTag = "disassociateDucr")
    .and[ShutMucrRequest](typeTag = "shutMucr")
    .format
}
