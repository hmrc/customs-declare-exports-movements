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

package uk.gov.hmrc.exports.movements.models.notifications.parsers

import play.api.{Environment, Mode}
import uk.gov.hmrc.exports.movements.base.UnitSpec
import uk.gov.hmrc.exports.movements.utils.JsonFile

class ErrorValidatorSpec extends UnitSpec {

  private lazy val jsonFile = new JsonFile(Environment.simple(mode = Mode.Test))
  val errorValidator = new ErrorValidator(jsonFile)
  val ileErrorCode = "10"
  val incorrectCode = "incorrect"

  "Error parser" should {
    "correct check if ILE error exists" in {
      errorValidator.hasErrorMessage(ileErrorCode) shouldBe true
      errorValidator.hasErrorMessage(incorrectCode) shouldBe false
    }

    "correctly retrieve ILE error" in {
      errorValidator.retrieveCode(ileErrorCode) shouldBe Some(ileErrorCode)
    }

    "return None if incorrect code" in {
      errorValidator.retrieveCode(incorrectCode) shouldBe None
    }
  }
}
