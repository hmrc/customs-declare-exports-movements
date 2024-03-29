/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.exports.movements.migrations

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TimeUtilsSpec extends AnyWordSpec with Matchers {

  private val timeUtils = new TimeUtils

  "TimeUtils on minutesToMillis" should {
    "correctly convert to milliseconds" in {
      val minutes = 13L
      val expectedOutput = 780000L

      timeUtils.minutesToMillis(minutes) mustBe expectedOutput
    }
  }

  "TimeUtils on millisToMinutes" should {
    "correctly convert to minutes" in {
      val millis = 120000L
      val expectedOutput = 2L

      timeUtils.millisToMinutes(millis) mustBe expectedOutput
    }
  }
}
