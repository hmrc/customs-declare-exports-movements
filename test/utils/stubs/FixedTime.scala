/*
 * Copyright 2020 HM Revenue & Customs
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

package stubs

import java.time._

import play.api.inject.guice.GuiceableModule

trait FixedTime {

  protected val currentDate: LocalDate = LocalDate.of(2020, 1, 1)
  protected val currentTime: LocalTime = LocalTime.of(12, 30, 30)
  protected val currentDateTime: LocalDateTime = LocalDateTime.of(currentDate, currentTime)
  protected val currentInstant: Instant = currentDateTime.toInstant(ZoneOffset.UTC)
  protected val clock: Clock = Clock.fixed(currentInstant, ZoneOffset.UTC)
  protected lazy val fixedTimeBinding: GuiceableModule = play.api.inject.bind[Clock].toInstance(clock)

}
