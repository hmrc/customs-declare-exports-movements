/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.exports.movements.services

import uk.gov.hmrc.exports.movements.models.XmlTags
import uk.gov.hmrc.exports.movements.models.common.UcrType.{Ducr, DucrPart, Mucr}
import uk.gov.hmrc.exports.movements.models.movements.ConsignmentReference
import uk.gov.hmrc.exports.movements.models.notifications.parsers.StringOption
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType._

import scala.xml.NodeSeq

class UcrBlockBuilder {

  def buildUcrBlock(consignmentReference: ConsignmentReference): UcrBlock =
    if (consignmentReference.is(DucrPart)) {
      val (ducr, ducrPartNo) = splitDucrPart(consignmentReference.referenceValue)
      if (ducrPartNo.isEmpty)
        throw new IllegalArgumentException(s"ConsignmentReference of type DucrPart contains empty DucrPartNo part")

      UcrBlock(ucr = ducr, ucrPartNo = Some(ducrPartNo), ucrType = Ducr.codeValue)

    } else if (consignmentReference.is(Ducr) || consignmentReference.is(Mucr)) {
      UcrBlock(ucr = consignmentReference.referenceValue, ucrType = consignmentReference.reference)

    } else {
      throw new IllegalArgumentException(s"Unknown ConsignmentReference of type: [${consignmentReference.reference}]")
    }

  private def splitDucrPart(ducrPart: String): (String, String) = {
    val DucrPartNoSeparator = "-"

    val separatorIndex = ducrPart.lastIndexOf(DucrPartNoSeparator)
    val (ducr, ducrPartNo) = ducrPart.splitAt(separatorIndex)
    val ducrPartIdWithoutSeparator = ducrPartNo.tail

    (ducr, ducrPartIdWithoutSeparator)
  }

  def buildUcrBlockNode(consolidationType: ConsolidationType, ucr: String): NodeSeq = consolidationType match {
    case ShutMucr =>
      throw new IllegalArgumentException(s"Cannot build UcrBlock instance for request of type: [$consolidationType]")
    case DucrPartAssociation | DucrPartDisassociation => buildDucrPartUcrBlockNode(consolidationType, ucr)
    case _                                            => buildCommonUcrBlockNode(consolidationType, ucr)
  }

  private def buildDucrPartUcrBlockNode(consolidationType: ConsolidationType, ucr: String): NodeSeq = {
    val (ducr, ducrPartNo) = splitDucrPart(ucr)

    <ucrBlock>
      <ucr>{ducr}</ucr>
      <ucrPartNo>{ducrPartNo}</ucrPartNo>
      <ucrType>{ucrType(consolidationType)}</ucrType>
    </ucrBlock>
  }

  private def buildCommonUcrBlockNode(consolidationType: ConsolidationType, ucr: String): NodeSeq =
    <ucrBlock>
      <ucr>{ucr}</ucr>
      <ucrType>{ucrType(consolidationType)}</ucrType>
    </ucrBlock>

  private def ucrType(consolidationType: ConsolidationType): String = consolidationType match {
    case DucrAssociation | DucrDisassociation | DucrPartAssociation | DucrPartDisassociation => "D"
    case MucrAssociation | MucrDisassociation                                                => "M"
  }

  def extractUcrBlocksForSubmissionFrom(nodeSeq: NodeSeq): Seq[UcrBlock] = {
    val ucrBlocks = (nodeSeq \ XmlTags.ucrBlock).map { node =>
      val ucr = (node \ XmlTags.ucr).text
      val ucrPartNo = StringOption((node \ XmlTags.ucrPartNo).text)
      val ucrType = if (ucrPartNo.exists(_.nonEmpty)) DucrPart.codeValue else (node \ XmlTags.ucrType).text
      UcrBlock(ucr = ucr, ucrType = ucrType, ucrPartNo = ucrPartNo)
    }

    val masterUcr = (nodeSeq \ XmlTags.masterUCR).map(node => UcrBlock(ucr = node.text, ucrType = "M"))

    masterUcr ++ ucrBlocks
  }

}
