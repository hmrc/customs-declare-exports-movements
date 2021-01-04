/*
 * Copyright 2021 HM Revenue & Customs
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
import testdata.notifications.NotificationTestData._
import testdata.notifications._
import uk.gov.hmrc.exports.movements.models.notifications.parsers._

class ResponseParserProviderSpec extends WordSpec with MustMatchers {

  private trait Test {
    val errorValidator = new ErrorValidator
    val commonTypesParser = new CommonTypesParser
    val movementResponseParser = new MovementResponseParser(commonTypesParser)
    val movementTotalsResponseParser = new MovementTotalsResponseParser(commonTypesParser)
    val controlResponseParser = new ControlResponseParser(errorValidator)
    val ileQueryResponseParser = new IleQueryResponseParser(commonTypesParser)
    val parserFactory =
      new ResponseParserProvider(movementResponseParser, movementTotalsResponseParser, controlResponseParser, ileQueryResponseParser)
  }

  "ResponseParserFactory on buildResponseParser" when {

    "provided with inventoryLinkingMovementResponse" should {
      "return MovementResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingMovementResponse.Correct.AllElements.asXml

        val parser = parserFactory.provideResponseParser(responseXml)

        parser mustBe a[MovementResponseParser]
      }
    }

    "provided with inventoryLinkingMovementTotalsResponse" should {
      "return MovementTotalsResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asXml

        val parser = parserFactory.provideResponseParser(responseXml)

        parser mustBe a[MovementTotalsResponseParser]
      }
    }

    "provided with inventoryLinkingControlResponse" should {
      "return ControlResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingControlResponse.Correct.Rejected.asXml

        val parser = parserFactory.provideResponseParser(responseXml)

        parser mustBe a[ControlResponseParser]
      }
    }

    "provided with inventoryLinkingQueryResponse" should {
      "return IleQueryResponseParser" in new Test {
        val responseXml = ExampleInventoryLinkingQueryResponse.Correct.QueriedDucr.asXml

        val parser = parserFactory.provideResponseParser(responseXml)

        parser mustBe a[IleQueryResponseParser]
      }
    }

    "provided with unknown XML format" should {
      "throw an IllegalArgumentException" in new Test {
        val responseXml = unknownFormatResponseXML
        val exc = intercept[IllegalArgumentException] {
          parserFactory.provideResponseParser(responseXml)
        }

        exc.getMessage must include("Unknown Inventory Linking Response: UnknownFormat")
      }
    }
  }

}
