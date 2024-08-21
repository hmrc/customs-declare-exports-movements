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
import utils.testdata.notifications.ExampleInventoryLinkingQueryResponse
import utils.testdata.notifications.ExampleXmlAndDomainModelPair.ExampleQueryResponse

class IleQueryResponseParserSpec extends AnyWordSpec with Matchers {

  private val commonTypesParser = new CommonTypesParser
  private val parser = new IleQueryResponseParser(commonTypesParser)

  "IleQueryResponseParser on parse" should {

    def executeTest(testQueryResponse: ExampleQueryResponse): Unit = {
      val inputXml = testQueryResponse.asXml
      val expectedResult = testQueryResponse.asDomainModel

      val result = parser.parse(inputXml)

      result mustBe expectedResult
    }

    "return correct QueryResponseData" when {

      "provided with inventoryLinkingQueryResponse" which {

        "contains only queriedDUCR element" in {
          executeTest(ExampleInventoryLinkingQueryResponse.Correct.QueriedDucr)
        }

        "contains only queriedMUCR element" in {
          executeTest(ExampleInventoryLinkingQueryResponse.Correct.QueriedMucr)
        }

        "contains only parentMUCR element" in {
          executeTest(ExampleInventoryLinkingQueryResponse.Correct.ParentMucr)
        }

        "contains only 2 childDUCR elements" in {
          executeTest(ExampleInventoryLinkingQueryResponse.Correct.ChildDucrs)
        }

        "contains only 2 childMUCR elements" in {
          executeTest(ExampleInventoryLinkingQueryResponse.Correct.ChildMucrs)
        }

        "contains no elements" in {
          executeTest(ExampleInventoryLinkingQueryResponse.Correct.Empty)
        }
      }
    }
  }
}
