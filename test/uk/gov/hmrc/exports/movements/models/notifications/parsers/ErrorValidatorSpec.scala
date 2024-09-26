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

import uk.gov.hmrc.exports.movements.base.UnitSpec

class ErrorValidatorSpec extends UnitSpec {

  val errorValidator = new ErrorValidator
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
