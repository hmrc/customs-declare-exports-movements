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
import utils.testdata.ConsolidationTestData._
import utils.testdata.MovementsTestData._
import uk.gov.hmrc.exports.movements.base.UnitSpec
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationRequest._
import uk.gov.hmrc.exports.movements.models.notifications.standard

import java.time.{Clock, Instant, ZoneOffset}

class IleMapperSpec extends UnitSpec {

  private val clock = Clock.fixed(Instant.parse(dateTimeString), ZoneOffset.UTC)
  private val ileMapper = new IleMapper(clock)

  "ILE Mapper on buildInventoryLinkingMovementRequestXml" should {

    "create correct XML for Arrival" in {
      val input = exampleArrivalRequest
      val xml = ileMapper.buildInventoryLinkingMovementRequestXml(input)
      val reference = (xml \ "movementReference").text

      val expectedXml = exampleArrivalRequestXML(reference)

      xml shouldBe expectedXml
    }

    "create correct XML for Retrospective Arrival" which {
      "contains added goodsArrivalDateTime in correct format" in {
        val input = exampleRetrospectiveArrivalRequest
        val xml = ileMapper.buildInventoryLinkingMovementRequestXml(input)
        val reference = (xml \ "movementReference").text

        val expectedXml = exampleRetrospectiveArrivalRequestXML(reference)

        xml shouldBe expectedXml
      }
    }

    "create correct XML for Departure" in {
      val input = exampleDepartureRequest
      val xml = ileMapper.buildInventoryLinkingMovementRequestXml(input)
      val reference = (xml \ "movementReference").text
      val expectedXml = exampleDepartureRequestXML(reference)

      xml shouldBe expectedXml
    }

    "create correct XML for Create Empty MUCR" in {
      val input = exampleCreateEmptyMucrRequest
      val xml = ileMapper.buildInventoryLinkingMovementRequestXml(input)
      val reference = (xml \ "movementReference").text

      val expectedXml = exampleCreateEmptyMucrRequestXML(reference)

      xml shouldBe expectedXml
    }
  }

  "ILE Mapper on buildConsolidationXml" should {

    "create correct XML based on the consolidation" when {

      "it is DUCR Association" in {
        val consolidation = AssociateDucrRequest(eori = validEori, mucr = ucr_2, ucr = ucr)
        val expectedXml = scala.xml.Utility.trim(exampleAssociateDucrConsolidationRequestXML)

        ileMapper.buildConsolidationXml(consolidation) shouldBe expectedXml
      }

      "it is MUCR Association" in {
        val consolidation = AssociateMucrRequest(eori = validEori, mucr = ucr_2, ucr = ucr)
        val expectedXml = scala.xml.Utility.trim(exampleAssociateMucrConsolidationRequestXML)

        ileMapper.buildConsolidationXml(consolidation) shouldBe expectedXml
      }

      "it is DUCR Part Association" in {
        val consolidation = AssociateDucrPartRequest(eori = validEori, mucr = ucr_2, ucr = validWholeDucrPart)
        val expectedXml = scala.xml.Utility.trim(exampleAssociateDucrPartConsolidationRequestXML)

        ileMapper.buildConsolidationXml(consolidation) shouldBe expectedXml
      }

      "it is DUCR Dissociation" in {
        val consolidation = DisassociateDucrRequest(eori = validEori, ucr = ucr)
        val expectedXml = scala.xml.Utility.trim(exampleDisassociateDucrConsolidationRequestXML)

        ileMapper.buildConsolidationXml(consolidation) shouldBe expectedXml
      }

      "it is MUCR Dissociation" in {
        val consolidation = DisassociateMucrRequest(eori = validEori, ucr = ucr)
        val expectedXml = scala.xml.Utility.trim(exampleDisassociateMucrConsolidationRequestXML)

        ileMapper.buildConsolidationXml(consolidation) shouldBe expectedXml
      }

      "it is DUCR Part Dissociation" in {
        val consolidation = DisassociateDucrPartRequest(eori = validEori, ucr = validWholeDucrPart)
        val expectedXml = scala.xml.Utility.trim(exampleDisassociateDucrPartConsolidationRequestXML)

        ileMapper.buildConsolidationXml(consolidation) shouldBe expectedXml
      }

      "it is Shut MUCR" in {
        val consolidation = ShutMucrRequest(eori = validEori, mucr = ucr_2)
        val expectedXml = scala.xml.Utility.trim(exampleShutMucrConsolidationRequestXML)

        ileMapper.buildConsolidationXml(consolidation) shouldBe expectedXml
      }
    }
  }

  "ILE Mapper on buildIleQuery" should {
    "create correct XML based on the ILE Query" in {
      val ucrBlock = standard.UcrBlock(ucr = ucr, ucrType = "D")
      val expectedXml = scala.xml.Utility.trim(exampleIleQueryRequestXml)

      ileMapper.buildIleQuery(ucrBlock) shouldBe expectedXml
    }
  }
}
