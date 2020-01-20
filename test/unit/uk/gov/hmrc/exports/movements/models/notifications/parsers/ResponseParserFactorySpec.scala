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

package unit.uk.gov.hmrc.exports.movements.models.notifications.parsers

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.parsers._
import utils.testdata.notifications.NotificationTestData._
import utils.testdata.notifications._

class ResponseParserFactorySpec extends WordSpec with MustMatchers {

  private trait Test {
    val errorValidator = new ErrorValidator
    val commonTypesParser = new CommonTypesParser
    val movementResponseParser = new MovementResponseParser(commonTypesParser)
    val movementTotalsResponseParser = new MovementTotalsResponseParser(commonTypesParser)
    val controlResponseParser = new ControlResponseParser(errorValidator)
    val parserFactory =
      new ResponseParserFactory(movementResponseParser, movementTotalsResponseParser, controlResponseParser)
  }

  "ResponseParserFactory on buildResponseParser" when {

    "provided with inventoryLinkingMovementResponse" should {
      "return MovementResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingMovementResponse.Correct.AllElements.asXml

        val parser = parserFactory.buildResponseParser(responseXml)

        parser mustBe a[MovementResponseParser]
      }
    }

    "provided with inventoryLinkingMovementTotalsResponse" should {
      "return MovementTotalsResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asXml

        val parser = parserFactory.buildResponseParser(responseXml)

        parser mustBe a[MovementTotalsResponseParser]
      }
    }

    "provided with inventoryLinkingControlResponse" should {
      "return ControlResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml

        val parser = parserFactory.buildResponseParser(responseXml)

        parser mustBe a[ControlResponseParser]
      }
    }

    "provided with unknown XML format" should {
      "throw an IllegalArgumentException" in new Test {
        val responseXml = unknownFormatResponseXML
        val exc = intercept[IllegalArgumentException] {
          parserFactory.buildResponseParser(responseXml)
        }

        exc.getMessage must include("Unknown Inventory Linking Response: UnknownFormat")
      }
    }

  }

  "ResponseParserFactory on buildResponseParserContext" when {

    "provided with inventoryLinkingMovementResponse" should {
      "return ResponseParserContext with MovementResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingMovementResponse.Correct.AllElements.asXml

        val parserContext = parserFactory.buildResponseParserContext(responseXml)

        parserContext.responseType must equal("inventoryLinkingMovementResponse")
        parserContext.parser mustBe a[MovementResponseParser]
      }
    }

    "provided with inventoryLinkingMovementTotalsResponse" should {
      "return ResponseParserContext with MovementTotalsResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asXml

        val parserContext = parserFactory.buildResponseParserContext(responseXml)

        parserContext.responseType must equal("inventoryLinkingMovementTotalsResponse")
        parserContext.parser mustBe a[MovementTotalsResponseParser]
      }
    }

    "provided with inventoryLinkingControlResponse" should {
      "return ResponseParserContext with ControlResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml

        val parserContext = parserFactory.buildResponseParserContext(responseXml)

        parserContext.responseType must equal("inventoryLinkingControlResponse")
        parserContext.parser mustBe a[ControlResponseParser]
      }
    }

    "provided with unknown XML format" should {
      "throw an IllegalArgumentException" in new Test {
        val responseXml = unknownFormatResponseXML
        val exc = intercept[IllegalArgumentException] {
          parserFactory.buildResponseParserContext(responseXml)
        }

        exc.getMessage must include("Unknown Inventory Linking Response: UnknownFormat")
      }
    }

  }

}
