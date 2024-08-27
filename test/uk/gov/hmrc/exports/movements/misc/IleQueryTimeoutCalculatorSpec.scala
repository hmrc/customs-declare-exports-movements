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

package uk.gov.hmrc.exports.movements.misc

import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.testdata.MovementsTestData.{dateTimeString, exampleIleQuerySubmission}
import uk.gov.hmrc.exports.movements.config.AppConfig

import java.time.temporal.ChronoUnit
import java.time.{Clock, Duration, Instant, ZoneOffset}
import java.util.concurrent.TimeUnit

class IleQueryTimeoutCalculatorSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]
  private val clock = Clock.fixed(Instant.parse(dateTimeString), ZoneOffset.UTC)

  private val ileQueryTimeoutCalculator = new IleQueryTimeoutCalculator(appConfig, clock)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
  }

  override def afterEach(): Unit = {
    reset(appConfig)

    super.afterEach()
  }

  "IleQueryTimeoutCalculator on hasQueryTimedOut" should {

    "return true" when {

      "provided with Submission which requestTimestamp is older than timeout in seconds" in {
        val timeout = Duration.of(30, ChronoUnit.SECONDS)
        when(appConfig.ileQueryResponseTimeout).thenReturn(timeout)
        val submission = exampleIleQuerySubmission().copy(requestTimestamp = Instant.now(clock).minusSeconds(31))

        ileQueryTimeoutCalculator.hasQueryTimedOut(submission) mustBe true
      }

      "provided with Submission which requestTimestamp is older than timeout in minutes" in {
        val timeout = Duration.of(2, ChronoUnit.MINUTES)
        when(appConfig.ileQueryResponseTimeout).thenReturn(timeout)
        val submission = exampleIleQuerySubmission().copy(requestTimestamp = Instant.now(clock).minusSeconds(TimeUnit.MINUTES.toSeconds(2) + 1))

        ileQueryTimeoutCalculator.hasQueryTimedOut(submission) mustBe true
      }
    }

    "return false" when {

      "provided with Submission which requestTimestamp and current time difference is smaller than timeout in seconds" in {
        val timeout = Duration.of(30, ChronoUnit.SECONDS)
        when(appConfig.ileQueryResponseTimeout).thenReturn(timeout)
        val submission = exampleIleQuerySubmission().copy(requestTimestamp = Instant.now(clock).minusSeconds(25))

        ileQueryTimeoutCalculator.hasQueryTimedOut(submission) mustBe false
      }

      "provided with Submission which requestTimestamp and current time difference is smaller than timeout in minutes" in {
        val timeout = Duration.of(2, ChronoUnit.MINUTES)
        when(appConfig.ileQueryResponseTimeout).thenReturn(timeout)
        val submission = exampleIleQuerySubmission().copy(requestTimestamp = Instant.now(clock).minusSeconds(TimeUnit.MINUTES.toSeconds(2) - 5))

        ileQueryTimeoutCalculator.hasQueryTimedOut(submission) mustBe false
      }
    }

    "call AppConfig for ileQueryResponseTimeout" in {
      val timeout = Duration.of(30, ChronoUnit.SECONDS)
      when(appConfig.ileQueryResponseTimeout).thenReturn(timeout)
      val submission = exampleIleQuerySubmission().copy(requestTimestamp = Instant.now(clock).minusSeconds(5))

      ileQueryTimeoutCalculator.hasQueryTimedOut(submission)

      verify(appConfig).ileQueryResponseTimeout
    }
  }
}
