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

package unit.uk.gov.hmrc.exports.movements.services

import uk.gov.hmrc.exports.movements.models.common.UcrType.{Ducr, DucrPart, Mucr}
import uk.gov.hmrc.exports.movements.models.movements.ConsignmentReference
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType._
import uk.gov.hmrc.exports.movements.services.UcrBlockBuilder
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec
import utils.testdata.{CommonTestData, ConsolidationTestData}

import scala.xml.NodeSeq

class UcrBlockBuilderSpec extends UnitSpec {

  private val ucrBlockBuilder = new UcrBlockBuilder()

  private def assertEqual(actual: NodeSeq, expected: NodeSeq): Unit =
    xml.Utility.trim(actual.head) shouldBe xml.Utility.trim(expected.head)


  "UcrBlockBuilder on buildWcoUcrBlock" should {

    "build correct UcrBlock" when {

      "provided with ConsignmentReferences for DUCR" in {

        val consignmentReference = ConsignmentReference(reference = Ducr.codeValue, referenceValue = CommonTestData.ucr)

        val result = ucrBlockBuilder.buildUcrBlock(consignmentReference)

        val expectedResult = UcrBlock(ucr = CommonTestData.ucr, ucrType = Ducr.codeValue)
        result shouldBe expectedResult
      }

      "provided with ConsignmentReferences for MUCR" in {

        val consignmentReference = ConsignmentReference(reference = Mucr.codeValue, referenceValue = CommonTestData.mucr)

        val result = ucrBlockBuilder.buildUcrBlock(consignmentReference)

        val expectedResult = UcrBlock(ucr = CommonTestData.mucr, ucrType = Mucr.codeValue)
        result shouldBe expectedResult
      }

      "provided with ConsignmentReferences for DUCR Part" in {

        val consignmentReference = ConsignmentReference(reference = DucrPart.codeValue, referenceValue = CommonTestData.validWholeDucrPart)

        val result = ucrBlockBuilder.buildUcrBlock(consignmentReference)

        val expectedResult = UcrBlock(ucr = CommonTestData.ucr, ucrPartNo = Some(CommonTestData.validUcrPartId), ucrType = Ducr.codeValue)
        result shouldBe expectedResult
      }
    }

    "throw IllegalArgumentException" when {

      "provided with ConsignmentReferences for unknown type" in {

        val consignmentReference = ConsignmentReference(reference = "UNKNOWN", referenceValue = CommonTestData.ucr)

        intercept[IllegalArgumentException](ucrBlockBuilder.buildUcrBlock(consignmentReference))

      }

      "provided with ConsignmentReferences for DUCR Part, without ucrPartId part" in {

        val consignmentReference = ConsignmentReference(reference = DucrPart.codeValue, referenceValue = s"${CommonTestData.ucr}-")

        intercept[IllegalArgumentException](ucrBlockBuilder.buildUcrBlock(consignmentReference))
      }
    }
  }

  "UcrBlockBuilder on buildUcrBlockNode" should {

    "build correct UcrBlock Node" when {

      "provided with ConsolidationType DucrAssociation" in {

        val consolidationType = DucrAssociation
        val testUcr = CommonTestData.ucr

        val result = ucrBlockBuilder.buildUcrBlockNode(consolidationType, testUcr)

        assertEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Ducr.codeValue))
      }

      "provided with ConsolidationType MucrAssociation" in {

        val consolidationType = MucrAssociation
        val testUcr = CommonTestData.mucr

        val result = ucrBlockBuilder.buildUcrBlockNode(consolidationType, testUcr)

        assertEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Mucr.codeValue))
      }

      "provided with ConsolidationType DucrPartAssociation" in {

        val consolidationType = DucrPartAssociation
        val testUcr = CommonTestData.ucr
        val testDucrPartNo = CommonTestData.validUcrPartId

        val result = ucrBlockBuilder.buildUcrBlockNode(consolidationType, CommonTestData.validWholeDucrPart)

        assertEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Ducr.codeValue, ucrPartNo = testDucrPartNo))
      }

      "provided with ConsolidationType DucrDisassociation" in {

        val consolidationType = DucrDisassociation
        val testUcr = CommonTestData.ucr

        val result = ucrBlockBuilder.buildUcrBlockNode(consolidationType, testUcr)

        assertEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Ducr.codeValue))
      }

      "provided with ConsolidationType MucrDisassociation" in {

        val consolidationType = MucrDisassociation
        val testUcr = CommonTestData.mucr

        val result = ucrBlockBuilder.buildUcrBlockNode(consolidationType, testUcr)

        assertEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Mucr.codeValue))
      }

      "provided with ConsolidationType DucrPartDisassociation" in {

        val consolidationType = DucrPartDisassociation
        val testUcr = CommonTestData.ucr
        val testDucrPartNo = CommonTestData.validUcrPartId

        val result = ucrBlockBuilder.buildUcrBlockNode(consolidationType, CommonTestData.validWholeDucrPart)

        assertEqual(result, ConsolidationTestData.buildUcrBlockNode(ucr = testUcr, ucrType = Ducr.codeValue, ucrPartNo = testDucrPartNo))
      }
    }

    "throw IllegalArgumentException" when {

      "provided with ConsolidationType ShutMucr" in {

        val consolidationType = ShutMucr
        val testUcr = CommonTestData.mucr

        intercept[IllegalArgumentException](ucrBlockBuilder.buildUcrBlockNode(consolidationType, testUcr))
      }
    }

  }

}
