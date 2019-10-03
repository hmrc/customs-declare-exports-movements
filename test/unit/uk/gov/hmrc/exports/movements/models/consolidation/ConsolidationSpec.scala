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

package unit.uk.gov.hmrc.exports.movements.models.consolidation

import play.api.libs.json.{JsObject, JsString, JsSuccess, JsValue}
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationType._
import uk.gov.hmrc.exports.movements.models.consolidation.{
  AssociateDucrRequest,
  Consolidation,
  ConsolidationRequest,
  DisassiociateDucrRequest,
  ShutMucrRequest
}
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.{DucrAssociation, DucrDisassociation, ShutMucr}
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec

class ConsolidationSpec extends UnitSpec {

  val mucr = "mucr"
  val ducr = "ducr"

  "Consolidation Request reads" should {

    "correct read Associate Ducr request" in {

      val associateDucrJson: JsValue =
        JsObject(Map("type" -> JsString("associateDucr"), "mucr" -> JsString(mucr), "ducr" -> JsString(ducr)))

      val expectedResult = AssociateDucrRequest(mucr, ducr)

      ConsolidationRequest.format.reads(associateDucrJson) shouldBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Ducr request" in {

      val diassociateDucrJson: JsValue = JsObject(Map("type" -> JsString("disassociateDucr"), "ducr" -> JsString(ducr)))

      val expectedResult = DisassiociateDucrRequest(ducr)

      ConsolidationRequest.format.reads(diassociateDucrJson) shouldBe JsSuccess(expectedResult)
    }

    "correct read Shut Mucr request" in {

      val shutMucrJson: JsValue = JsObject(Map("type" -> JsString("shutMucr"), "mucr" -> JsString(mucr)))

      val expectedResult = ShutMucrRequest(mucr)

      ConsolidationRequest.format.reads(shutMucrJson) shouldBe JsSuccess(expectedResult)
    }
  }

  "Associate Ducr Request" should {

    "correctly convert request to consolidation" in {

      val associateDucrRequest = AssociateDucrRequest(mucr, ducr)

      val expectedConsolidation = Consolidation(ASSOCIATE_DUCR, Some(mucr), Some(ducr), DucrAssociation)

      associateDucrRequest.consolidation() shouldBe expectedConsolidation
    }
  }

  "Disassociate Ducr Request" should {

    "correctly convert request to consolidation" in {

      val disassociateDucrRequest = DisassiociateDucrRequest(ducr)

      val expectedConsolidation = Consolidation(DISASSOCIATE_DUCR, None, Some(ducr), DucrDisassociation)

      disassociateDucrRequest.consolidation() shouldBe expectedConsolidation
    }
  }

  "Shut Mucr Request" should {

    "correctly convert request to consolidation" in {

      val shutMucrRequest = ShutMucrRequest(mucr)

      val expectedConsolidation = Consolidation(SHUT_MUCR, Some(mucr), None, ShutMucr)

      shutMucrRequest.consolidation() shouldBe expectedConsolidation
    }
  }

  "Consolidation" should {

    "correctly build submission context" in {

      val eori = "eori"
      val shutMucrXml = scala.xml.Utility.trim {
        <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>CST</messageCode>
          {mucr}
        </inventoryLinkingConsolidationRequest>
      }
      val consolidation = Consolidation(SHUT_MUCR, Some(mucr), None, ShutMucr)
      val expectedSubmissionRequestContext = SubmissionRequestContext(eori, ShutMucr, shutMucrXml)

      consolidation.buildSubmissionContext(eori, shutMucrXml) shouldBe expectedSubmissionRequestContext
    }
  }
}
