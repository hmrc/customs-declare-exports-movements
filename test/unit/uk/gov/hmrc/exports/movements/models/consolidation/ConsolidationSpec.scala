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
import uk.gov.hmrc.exports.movements.models.consolidation._
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec

class ConsolidationSpec extends UnitSpec {

  val mucr = "mucr"
  val ducr = "ducr"

  "Consolidation Request reads" should {

    "correct read Associate Ducr request" in {

      val associateDucrJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(ASSOCIATE_DUCR.toString), "mucr" -> JsString(mucr), "ducr" -> JsString(ducr)))

      val expectedResult = AssociateDucrRequest(mucr, ducr)

      Consolidation.format.reads(associateDucrJson) shouldBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Ducr request" in {

      val diassociateDucrJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(DISASSOCIATE_DUCR.toString), "ducr" -> JsString(ducr)))

      val expectedResult = DisassiociateDucrRequest(ducr)

      Consolidation.format.reads(diassociateDucrJson) shouldBe JsSuccess(expectedResult)
    }

    "correct read Shut Mucr request" in {

      val shutMucrJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(SHUT_MUCR.toString), "mucr" -> JsString(mucr)))

      val expectedResult = ShutMucrRequest(mucr)

      Consolidation.format.reads(shutMucrJson) shouldBe JsSuccess(expectedResult)
    }
  }
}
