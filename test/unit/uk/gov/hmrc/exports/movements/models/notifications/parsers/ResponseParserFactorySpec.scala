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

package unit.uk.gov.hmrc.exports.movements.models.notifications.parsers

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.parsers.{
  ControlResponseParser,
  MovementResponseParser,
  MovementTotalsResponseParser,
  ResponseParserFactory
}
import utils.NotificationTestData._

class ResponseParserFactorySpec extends WordSpec with MustMatchers {

  private trait Test {
    val parserFactory = new ResponseParserFactory
  }

  "ResponseParserFactory on buildResponseParser" when {

    "provided with inventoryLinkingMovementResponse" should {
      "return MovementResponseParser" in new Test {
        val responseXml = exampleInventoryLinkingMovementResponseXML

        val parser = parserFactory.buildResponseParser(responseXml)

        parser mustBe a[MovementResponseParser]
      }
    }

    "provided with inventoryLinkingMovementTotalsResponse" should {
      "return MovementTotalsResponseParser" in new Test {
        val responseXml = exampleInventoryLinkingMovementTotalsResponseXML

        val parser = parserFactory.buildResponseParser(responseXml)

        parser mustBe a[MovementTotalsResponseParser]
      }
    }

    "provided with inventoryLinkingControlResponse" should {
      "return ControlResponseParser" in new Test {
        val responseXml = exampleRejectInventoryLinkingControlResponseXML

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
        val responseXml = exampleInventoryLinkingMovementResponseXML

        val parserContext = parserFactory.buildResponseParserContext(responseXml)

        parserContext.responseType must equal("inventoryLinkingMovementResponse")
        parserContext.parser mustBe a[MovementResponseParser]
      }
    }

    "provided with inventoryLinkingMovementTotalsResponse" should {
      "return ResponseParserContext with MovementTotalsResponseParser" in new Test {
        val responseXml = exampleInventoryLinkingMovementTotalsResponseXML

        val parserContext = parserFactory.buildResponseParserContext(responseXml)

        parserContext.responseType must equal("inventoryLinkingMovementTotalsResponse")
        parserContext.parser mustBe a[MovementTotalsResponseParser]
      }
    }

    "provided with inventoryLinkingControlResponse" should {
      "return ResponseParserContext with ControlResponseParser" in new Test {
        val responseXml = exampleRejectInventoryLinkingControlResponseXML

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
