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

package unit.uk.gov.hmrc.exports.movements.services

import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation.AssociateDucrRequest
import uk.gov.hmrc.exports.movements.services.WCOMapper
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec
import utils.testdata.CommonTestData._

class WCOMapperSpec extends UnitSpec {

  val ileMapper = new WCOMapper
  val associateDucrCode = "EAC"

  "ILE Mapper" should {

    "create correct XML based on the consolidation" in {

      val consolidation = AssociateDucrRequest(eori = validEori, mucr = ucr, ucr = ucr_2)

      val expectedXml = scala.xml.Utility.trim {
        <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{associateDucrCode}</messageCode>
          <masterUCR>{ucr}</masterUCR>
          <ucrBlock>
            <ucr>{ucr_2}</ucr>
            <ucrType>D</ucrType>
          </ucrBlock>
        </inventoryLinkingConsolidationRequest>
      }

      ileMapper.generateConsolidationXml(consolidation) shouldBe expectedXml
    }
  }
}
