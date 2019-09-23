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

package unit.uk.gov.hmrc.exports.movements.models.notifications.parsers

import uk.gov.hmrc.exports.movements.models.notifications.parsers.ErrorValidator
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec

class ErrorValidatorSpec extends UnitSpec {

  "Error parser" should {

    "successfully retrieve only correct errors" in {

      val errors = Seq("02", "09", "15", "20", "30", "41", "E607", "E1236549", "E10419", "E3464")
      val correctErrors = Seq("02", "15", "20", "30", "E607", "E10419", "E3464")

      ErrorValidator.validateErrors(errors) shouldBe correctErrors
    }
  }
}
