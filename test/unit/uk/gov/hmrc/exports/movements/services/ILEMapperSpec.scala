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

import java.time.{Clock, Instant, ZoneOffset}

import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation.AssociateDucrRequest
import uk.gov.hmrc.exports.movements.services.ILEMapper
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec
import utils.testdata.CommonTestData._
import utils.testdata.ConsolidationTestData.exampleAssociateDucrConsolidationRequestXML
import utils.testdata.MovementsTestData._

class ILEMapperSpec extends UnitSpec {

  private val clock = Clock.fixed(dateTime, ZoneOffset.UTC)
  private val ileMapper = new ILEMapper(clock)

  "ILE Mapper" should {

    "create correct XML for Arrival" in {

      val input = exampleArrivalRequest
      val expectedXml = exampleArrivalRequestXML

      ileMapper.generateInventoryLinkingMovementRequestXml(input).toString should equal(expectedXml.toString)
    }

    "create correct XML for Retrospective Arrival" which {
      "contains added goodsArrivalDateTime in correct format" in {

        val input = exampleRetrospectiveArrivalRequest
        val expectedXml = exampleRetrospectiveArrivalRequestXML

        val xml = ileMapper.generateInventoryLinkingMovementRequestXml(input)

        xml.toString shouldEqual expectedXml.toString
      }
    }

    "create correct XML for Departure" in {

      val input = exampleDepartureRequest
      val expectedXml = exampleDepartureRequestXML

      ileMapper.generateInventoryLinkingMovementRequestXml(input).toString should equal(expectedXml.toString)
    }

    "create correct XML based on the consolidation" in {

      val consolidation = AssociateDucrRequest(eori = validEori, mucr = ucr_2, ucr = ucr)
      val expectedXml = scala.xml.Utility.trim(exampleAssociateDucrConsolidationRequestXML)

      ileMapper.generateConsolidationXml(consolidation).toString shouldEqual expectedXml.toString
    }
  }
}
