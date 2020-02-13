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
import uk.gov.hmrc.exports.movements.models.notifications.standard.UcrBlock
import uk.gov.hmrc.exports.movements.services.IleMapper
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec
import utils.testdata.CommonTestData._
import utils.testdata.ConsolidationTestData._
import utils.testdata.MovementsTestData._

class IleMapperSpec extends UnitSpec {

  private val clock = Clock.fixed(Instant.parse(dateTimeString), ZoneOffset.UTC)
  private val ileMapper = new IleMapper(clock)

  "ILE Mapper" should {

    "create correct XML for Arrival" in {

      val input = exampleArrivalRequest

      val xml = ileMapper.generateInventoryLinkingMovementRequestXml(input)
      val reference = (xml \ "movementReference").text

      val expectedXml = exampleArrivalRequestXML(reference)

      xml shouldBe expectedXml
    }

    "create correct XML for Retrospective Arrival" which {
      "contains added goodsArrivalDateTime in correct format" in {

        val input = exampleRetrospectiveArrivalRequest

        val xml = ileMapper.generateInventoryLinkingMovementRequestXml(input)
        val reference = (xml \ "movementReference").text

        val expectedXml = exampleRetrospectiveArrivalRequestXML(reference)

        xml shouldBe expectedXml
      }
    }

    "create correct XML for Departure" in {

      val input = exampleDepartureRequest
      val expectedXml = exampleDepartureRequestXML

      ileMapper.generateInventoryLinkingMovementRequestXml(input) shouldBe expectedXml
    }

    "create correct XML based on the consolidation" in {

      val consolidation = AssociateDucrRequest(eori = validEori, mucr = ucr_2, ucr = ucr)
      val expectedXml = scala.xml.Utility.trim(exampleAssociateDucrConsolidationRequestXML)

      ileMapper.generateConsolidationXml(consolidation) shouldBe expectedXml
    }

    "create correct XML based on the ILE Query" in {

      val ucrBlock = UcrBlock(ucr, "D")
      val expectedXml = scala.xml.Utility.trim(exampleIleQueryRequestXml)

      ileMapper.generateIleQuery(ucrBlock) shouldBe expectedXml
    }
  }
}
