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

package uk.gov.hmrc.exports.movements.models.consolidation

import play.api.libs.json.{JsObject, JsString, JsSuccess, JsValue}
import utils.testdata.CommonTestData._
import uk.gov.hmrc.exports.movements.base.UnitSpec
import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation._
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType._

class ConsolidationSpec extends UnitSpec {

  private val mucr = ucr
  private val ducr = ucr_2

  "Consolidation Request reads" should {

    "correct read Associate Ducr request" in {

      val associateDucrJson: JsValue =
        JsObject(
          Map(
            "consolidationType" -> JsString(DucrAssociation.typeName),
            "eori" -> JsString(validEori),
            "mucr" -> JsString(mucr),
            "ucr" -> JsString(ducr)
          )
        )

      val expectedResult = AssociateDucrRequest(eori = validEori, mucr = mucr, ucr = ducr)

      Consolidation.format.reads(associateDucrJson) shouldBe JsSuccess(expectedResult)
    }

    "correct read Associate Mucr request" in {

      val associateMucrJson: JsValue =
        JsObject(
          Map(
            "consolidationType" -> JsString(MucrAssociation.typeName),
            "eori" -> JsString(validEori),
            "mucr" -> JsString(mucr),
            "ucr" -> JsString(mucr)
          )
        )

      val expectedResult = AssociateMucrRequest(eori = validEori, mucr = mucr, ucr = mucr)

      Consolidation.format.reads(associateMucrJson) shouldBe JsSuccess(expectedResult)
    }

    "correct read Associate Ducr Part request" in {

      val associateDucrPartJson: JsValue =
        JsObject(
          Map(
            "consolidationType" -> JsString(DucrPartAssociation.typeName),
            "eori" -> JsString(validEori),
            "mucr" -> JsString(mucr),
            "ucr" -> JsString(mucr)
          )
        )

      val expectedResult = AssociateDucrPartRequest(eori = validEori, mucr = mucr, ucr = mucr)

      Consolidation.format.reads(associateDucrPartJson) shouldBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Ducr request" in {

      val disassociateDucrJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(DucrDisassociation.typeName), "eori" -> JsString(validEori), "ucr" -> JsString(ducr)))

      val expectedResult = DisassociateDucrRequest(eori = validEori, ucr = ducr)

      Consolidation.format.reads(disassociateDucrJson) shouldBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Mucr request" in {

      val disassociateMucrJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(MucrDisassociation.typeName), "eori" -> JsString(validEori), "ucr" -> JsString(mucr)))

      val expectedResult = DisassociateMucrRequest(eori = validEori, ucr = mucr)

      Consolidation.format.reads(disassociateMucrJson) shouldBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Ducr Part request" in {

      val disassociateDucrPartJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(DucrPartDisassociation.typeName), "eori" -> JsString(validEori), "ucr" -> JsString(ducr)))

      val expectedResult = DisassociateDucrPartRequest(eori = validEori, ucr = ducr)

      Consolidation.format.reads(disassociateDucrPartJson) shouldBe JsSuccess(expectedResult)
    }

    "correct read Shut Mucr request" in {

      val shutMucrJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(ShutMucr.typeName), "eori" -> JsString(validEori), "mucr" -> JsString(mucr)))

      val expectedResult = ShutMucrRequest(eori = validEori, mucr = mucr)

      Consolidation.format.reads(shutMucrJson) shouldBe JsSuccess(expectedResult)
    }
  }
}
