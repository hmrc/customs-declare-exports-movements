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

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.parsers.StringOption

class StringOptionSpec extends WordSpec with MustMatchers {

  "StringOption on apply" should {

    "return Some containing this String" when {
      "provided with non-empty String" in {

        val input = "This is non-empty String"
        val output = StringOption(input)

        output must equal(Some(input))
      }
    }

    "return None" when {

      "provided with empty String" in {

        val input = ""
        val output = StringOption(input)

        output must equal(None)
      }

      "provided with String containing only spaces" in {

        val input = "      "
        val output = StringOption(input)

        output must equal(None)
      }

      "provided with null value" in {

        //noinspection ScalaStyle
        val input = null
        val output = StringOption(input)

        output must equal(None)
      }
    }
  }

  "StringOption on empty" should {
    "return None" in {
      StringOption.empty must equal(None)
    }
  }

}