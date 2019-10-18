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
import uk.gov.hmrc.exports.movements.models.consolidation
import uk.gov.hmrc.play.json.Union

object ConsolidationType extends Enumeration {
  type ConsolidationType = Value

  val ASSOCIATE_DUCR, DISASSOCIATE_DUCR, ASSOCIATE_MUCR, DISASSOCIATE_MUCR, SHUT_MUCR = Value

  implicit val format: Format[consolidation.ConsolidationType.Value] = Format(Reads.enumNameReads(ConsolidationType), Writes.enumNameWrites)
}

import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationType._

sealed abstract class Consolidation(val consolidationType: ConsolidationType, val mucrOpt: Option[String], val ucrOpt: Option[String])

case class AssociateDucrRequest(mucr: String, ucr: String) extends Consolidation(ASSOCIATE_DUCR, Some(mucr), Some(ucr))

case class DisassociateDucrRequest(ucr: String) extends Consolidation(DISASSOCIATE_DUCR, None, Some(ucr))

case class AssociateMucrRequest(mucr: String, ucr: String) extends Consolidation(ASSOCIATE_MUCR, Some(mucr), Some(ucr))

case class DisassociateMucrRequest(ucr: String) extends Consolidation(DISASSOCIATE_MUCR, None, Some(ucr))

case class ShutMucrRequest(mucr: String) extends Consolidation(SHUT_MUCR, Some(mucr), None)

object Consolidation {
  implicit val associateDucrFormat = Json.format[AssociateDucrRequest]
  implicit val disassociateDucrFormat = Json.format[DisassociateDucrRequest]
  implicit val associateMucrFormat = Json.format[AssociateMucrRequest]
  implicit val disassociateMucrFormat = Json.format[DisassociateMucrRequest]
  implicit val shutMucrFormat = Json.format[ShutMucrRequest]

  implicit val format = Union
    .from[Consolidation](typeField = "consolidationType")
    .and[AssociateDucrRequest](typeTag = ASSOCIATE_DUCR.toString)
    .and[DisassociateDucrRequest](typeTag = DISASSOCIATE_DUCR.toString)
    .and[AssociateMucrRequest](typeTag = ASSOCIATE_MUCR.toString)
    .and[DisassociateMucrRequest](typeTag = DISASSOCIATE_MUCR.toString)
    .and[ShutMucrRequest](typeTag = SHUT_MUCR.toString)
    .format
}
