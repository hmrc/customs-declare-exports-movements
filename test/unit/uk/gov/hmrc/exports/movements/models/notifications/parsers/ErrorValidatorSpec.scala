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

import uk.gov.hmrc.exports.movements.models.notifications.parsers.ErrorValidator
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec

class ErrorValidatorSpec extends UnitSpec {

  val errorValidator = new ErrorValidator
  val chiefErrorCode = "E3481"
  val ileErrorCode = "10"
  val incorrectCode = "incorrect"

  "Error parser" should {

    "correctly check if CHIEF error exists" in {

      errorValidator.hasErrorMessage(chiefErrorCode) shouldBe true
      errorValidator.hasErrorMessage(incorrectCode) shouldBe false
    }

    "correct check if ILE error exists" in {

      errorValidator.hasErrorMessage(ileErrorCode) shouldBe true
      errorValidator.hasErrorMessage(incorrectCode) shouldBe false
    }

    "correctly retrieve CHIEF error" in {

      errorValidator.retrieveCode(chiefErrorCode) shouldBe Some(chiefErrorCode)
    }

    "correctly retrieve ILE error" in {

      errorValidator.retrieveCode(ileErrorCode) shouldBe Some(ileErrorCode)
    }

    "return None if incorrect code" in {

      errorValidator.retrieveCode(incorrectCode) shouldBe None
    }
  }
}
