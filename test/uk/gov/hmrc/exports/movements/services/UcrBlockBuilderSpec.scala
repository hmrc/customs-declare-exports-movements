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

package uk.gov.hmrc.exports.movements.services

import utils.testdata.CommonTestData._
import utils.testdata.{CommonTestData, ConsolidationTestData}
import uk.gov.hmrc.exports.movements.base.UnitSpec
import uk.gov.hmrc.exports.movements.models.UcrType._
import uk.gov.hmrc.exports.movements.models.movements.ConsignmentReference
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType._
import uk.gov.hmrc.exports.movements.services.UcrBlockBuilder._
import uk.gov.hmrc.exports.movements.services.UcrBlockBuilderSpec._

import scala.xml.NodeSeq

class UcrBlockBuilderSpec extends UnitSpec {

  private def assertNodeSequencesEqual(actual: NodeSeq, expected: NodeSeq): Unit =
    xml.Utility.trim(actual.head) shouldBe xml.Utility.trim(expected.head)

  "UcrBlockBuilder on buildWcoUcrBlock" should {

    "build correct UcrBlock" when {

      "provided with ConsignmentReferences for DUCR" in {

        val consignmentReference = ConsignmentReference(reference = Ducr.codeValue, referenceValue = ucr)

        val result = buildUcrBlock(consignmentReference)

        val expectedResult = UcrBlock(ucr = ucr, ucrType = Ducr.codeValue)
        result shouldBe expectedResult
      }

      "provided with ConsignmentReferences for MUCR" in {

        val consignmentReference = ConsignmentReference(reference = Mucr.codeValue, referenceValue = CommonTestData.mucr)

        val result = buildUcrBlock(consignmentReference)

        val expectedResult = UcrBlock(ucr = CommonTestData.mucr, ucrType = Mucr.codeValue)
        result shouldBe expectedResult
      }

      "provided with ConsignmentReferences for DUCR Part" in {

        val consignmentReference = ConsignmentReference(reference = DucrPart.codeValue, referenceValue = CommonTestData.validWholeDucrPart)

        val result = buildUcrBlock(consignmentReference)

        val expectedResult = UcrBlock(ucr = ucr, ucrPartNo = Some(CommonTestData.validUcrPartNo), ucrType = Ducr.codeValue)
        result shouldBe expectedResult
      }
    }

    "throw IllegalArgumentException" when {

      "provided with ConsignmentReferences for unknown type" in {

        val consignmentReference = ConsignmentReference(reference = "UNKNOWN", referenceValue = ucr)

        intercept[IllegalArgumentException](buildUcrBlock(consignmentReference))

      }

      "provided with ConsignmentReferences for DUCR Part, without ucrPartId part" in {

        val consignmentReference = ConsignmentReference(reference = DucrPart.codeValue, referenceValue = s"${ucr}-")

        intercept[IllegalArgumentException](buildUcrBlock(consignmentReference))
      }
    }
  }

  "UcrBlockBuilder on buildUcrBlockNode" should {

    "build correct UcrBlock Node" when {

      "provided with ConsolidationType DucrAssociation" in {

        val consolidationType = DucrAssociation
        val testUcr = ucr

        val result = buildUcrBlockNode(consolidationType, testUcr)

        assertNodeSequencesEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Ducr.codeValue))
      }

      "provided with ConsolidationType MucrAssociation" in {

        val consolidationType = MucrAssociation
        val testUcr = CommonTestData.mucr

        val result = buildUcrBlockNode(consolidationType, testUcr)

        assertNodeSequencesEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Mucr.codeValue))
      }

      "provided with ConsolidationType DucrPartAssociation" in {

        val consolidationType = DucrPartAssociation
        val testUcr = ucr
        val testDucrPartNo = CommonTestData.validUcrPartNo

        val result = buildUcrBlockNode(consolidationType, CommonTestData.validWholeDucrPart)

        assertNodeSequencesEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Ducr.codeValue, ucrPartNo = testDucrPartNo))
      }

      "provided with ConsolidationType DucrDisassociation" in {

        val consolidationType = DucrDisassociation
        val testUcr = ucr

        val result = buildUcrBlockNode(consolidationType, testUcr)

        assertNodeSequencesEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Ducr.codeValue))
      }

      "provided with ConsolidationType MucrDisassociation" in {

        val consolidationType = MucrDisassociation
        val testUcr = CommonTestData.mucr

        val result = buildUcrBlockNode(consolidationType, testUcr)

        assertNodeSequencesEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Mucr.codeValue))
      }

      "provided with ConsolidationType DucrPartDisassociation" in {

        val consolidationType = DucrPartDisassociation
        val testUcr = ucr
        val testDucrPartNo = CommonTestData.validUcrPartNo

        val result = buildUcrBlockNode(consolidationType, CommonTestData.validWholeDucrPart)

        assertNodeSequencesEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Ducr.codeValue, ucrPartNo = testDucrPartNo))
      }
    }

    "throw IllegalArgumentException" when {

      "provided with ConsolidationType ShutMucr" in {

        val consolidationType = ShutMucr
        val testUcr = CommonTestData.mucr

        intercept[IllegalArgumentException](buildUcrBlockNode(consolidationType, testUcr))
      }
    }
  }

  "UcrBlockBuilder on extractUcrBlocksFrom" should {

    "build empty List" when {

      "provided with XML" which {

        "contains no UcrBlock" in {

          val input = EmptyUcrBlockXml
          val expectedResult = Seq.empty[UcrBlock]

          extractUcrBlocksForSubmissionFrom(input) shouldBe expectedResult
        }
      }
    }

    "build correct List of UcrBlocks" when {

      "provided with XML" which {

        "contains single UcrBlock" in {

          val input = singleUcrBlockXml
          val expectedResult = Seq(UcrBlock(ucr = ucr, ucrType = Ducr.codeValue))

          extractUcrBlocksForSubmissionFrom(input) shouldBe expectedResult
        }

        "contains single UcrBlock with UcrPartNo element" in {

          val input = singleUcrBlockWithUcrPartNoXml
          val expectedResult = Seq(UcrBlock(ucr = ucr, ucrPartNo = Some(validUcrPartNo), ucrType = DucrPart.codeValue))

          extractUcrBlocksForSubmissionFrom(input) shouldBe expectedResult
        }

        "contains multiple UcrBlocks" in {

          val input = doubleUcrBlocksXml
          val expectedResult = Seq(UcrBlock(ucr = ucr, ucrType = Ducr.codeValue), UcrBlock(ucr = ucr_2, ucrType = Ducr.codeValue))

          extractUcrBlocksForSubmissionFrom(input) shouldBe expectedResult
        }

        "contains masterUcr element" in {

          val input = masterUcrOnlyXml
          val expectedResult = Seq(UcrBlock(ucr = mucr, ucrType = Mucr.codeValue))

          extractUcrBlocksForSubmissionFrom(input) shouldBe expectedResult
        }

        "contains masterUcr element and UcrBlock" in {

          val input = singleUcrBlockWithMasterUcrXml
          val expectedResult = Seq(UcrBlock(ucr = mucr, ucrType = Mucr.codeValue), UcrBlock(ucr = ucr, ucrType = Ducr.codeValue))

          extractUcrBlocksForSubmissionFrom(input) shouldBe expectedResult
        }
      }
    }
  }

}

object UcrBlockBuilderSpec {

  private val EmptyUcrBlockXml =
    <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
    </inventoryLinkingMovementRequest>

  private val singleUcrBlockXml =
    <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>{Ducr.codeValue}</ucrType>
      </ucrBlock>
    </inventoryLinkingMovementRequest>

  private val singleUcrBlockWithUcrPartNoXml =
    <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrPartNo>{validUcrPartNo}</ucrPartNo>
        <ucrType>{Ducr.codeValue}</ucrType>
      </ucrBlock>
    </inventoryLinkingMovementRequest>

  private val singleUcrBlockWithMasterUcrXml =
    <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>{Ducr.codeValue}</ucrType>
      </ucrBlock>
      <masterUCR>{mucr}</masterUCR>
    </inventoryLinkingMovementRequest>

  private val doubleUcrBlocksXml =
    <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>{Ducr.codeValue}</ucrType>
      </ucrBlock>
      <ucrBlock>
        <ucr>{ucr_2}</ucr>
        <ucrType>{Ducr.codeValue}</ucrType>
      </ucrBlock>
    </inventoryLinkingMovementRequest>

  private val masterUcrOnlyXml =
    <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <masterUCR>{mucr}</masterUCR>
    </inventoryLinkingMovementRequest>

}
