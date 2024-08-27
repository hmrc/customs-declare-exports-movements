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

package uk.gov.hmrc.exports.movements.models.notifications.parsers

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.testdata.notifications.ExampleResponseCommonTypes
import uk.gov.hmrc.exports.movements.models.notifications.standard.{EntryStatus, GoodsItem, UcrBlock}

class CommonTypesParserSpec extends AnyWordSpec with Matchers {

  private val parser = new CommonTypesParser()

  "CommonTypesParserSpec on parseUcrBlock" when {

    "provided with empty xml" should {
      "return empty UcrBlock" in {

        val inputXml = <ucrBlock></ucrBlock>
        val expectedResult = UcrBlock(ucr = "", ucrType = "")

        val result = parser.parseUcrBlock(inputXml)

        result mustBe expectedResult
      }
    }

    "provided with incorrect xml" should {
      "throw IllegalArgumentException" in {

        val inputXml =
          <incorrectTag>
            <someTag>wrong value</someTag>
          </incorrectTag>

        an[IllegalArgumentException] mustBe thrownBy {
          parser.parseUcrBlock(inputXml)
        }
      }
    }

    "provided with correct xml" should {
      "return UcrBlock with correct values" when {
        "DUCR has no part number" in {

          val inputXml = ExampleResponseCommonTypes.Correct.DucrBlock.asXml
          val expectedResult = ExampleResponseCommonTypes.Correct.DucrBlock.asDomainModel

          val result = parser.parseUcrBlock(inputXml)

          result mustBe expectedResult
        }

        "DUCR has a part number" in {

          val inputXml = ExampleResponseCommonTypes.Correct.DucrPartBlock.asXml
          val expectedResult = ExampleResponseCommonTypes.Correct.DucrPartBlock.asDomainModel

          val result = parser.parseUcrBlock(inputXml)

          result mustBe expectedResult
        }
      }
    }
  }

  "CommonTypesParserSpec on parseEntryStatus" when {

    "provided with empty xml" should {
      "return empty EntryStatus" in {

        val inputXml = <entryStatus></entryStatus>
        val expectedResult = EntryStatus()

        val result = parser.parseEntryStatus(inputXml)

        result mustBe expectedResult
      }
    }

    "provided with incorrect xml" should {
      "throw IllegalArgumentException" in {

        val inputXml =
          <incorrectTag>
            <someTag>wrong value</someTag>
          </incorrectTag>

        an[IllegalArgumentException] mustBe thrownBy {
          parser.parseEntryStatus(inputXml)
        }
      }
    }

    "provided with correct xml" should {
      "return EntryStatus with correct values" in {

        val inputXml = ExampleResponseCommonTypes.Correct.EntryStatusAll.asXml
        val expectedResult = ExampleResponseCommonTypes.Correct.EntryStatusAll.asDomainModel

        val result = parser.parseEntryStatus(inputXml)

        result mustBe expectedResult
      }
    }
  }

  "CommonTypesParserSpec on parseGoodsItem" when {

    "provided with empty xml" should {
      "return empty GoodsItem" in {

        val inputXml = <goodsItem></goodsItem>
        val expectedResult = GoodsItem()

        val result = parser.parseGoodsItem(inputXml)

        result mustBe expectedResult
      }
    }

    "provided with incorrect xml" should {
      "throw IllegalArgumentException" in {

        val inputXml =
          <incorrectTag>
            <someTag>wrong value</someTag>
          </incorrectTag>

        an[IllegalArgumentException] mustBe thrownBy {
          parser.parseGoodsItem(inputXml)
        }
      }
    }

    "provided with correct xml" should {
      "return GoodsItem with correct values" in {

        val inputXml = ExampleResponseCommonTypes.Correct.GoodsItemAll.asXml
        val expectedResult = ExampleResponseCommonTypes.Correct.GoodsItemAll.asDomainModel

        val result = parser.parseGoodsItem(inputXml)

        result mustBe expectedResult
      }
    }
  }

}
