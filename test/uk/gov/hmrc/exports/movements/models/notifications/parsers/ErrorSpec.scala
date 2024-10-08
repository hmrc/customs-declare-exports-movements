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

class ErrorSpec extends UnitSpec {

  "Error apply method" should {

    "return an Error when the structure is correct" in {

      val inputList = List("code", "description")
      val expectedError = Error("code", "description")

      Error(inputList) shouldBe expectedError
    }

    "throw an exception when list is empty" in {

      intercept[IllegalArgumentException] {
        Error(List.empty)
      }
    }

    "throw an exception when list contains wrong format" in {

      intercept[IllegalArgumentException] {
        Error(List("code", "description", "wrongColumn"))
      }
    }
  }
}
