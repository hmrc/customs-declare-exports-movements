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

package unit.uk.gov.hmrc.exports.movements

import com.codahale.metrics.SharedMetricRegistries
import com.kenshoo.play.metrics.MetricsImpl
import com.typesafe.config.ConfigFactory
import play.api.Configuration
import play.api.inject.DefaultApplicationLifecycle
import uk.gov.hmrc.exports.movements.metrics.ExportsMetrics

trait MockMetrics {

  SharedMetricRegistries.clear()

  private val minimalConfig = ConfigFactory.parseString("""
    |metrics.name=""
    |metrics.rateUnit="SECONDS"
    |metrics.durationUnit="SECONDS"
    |metrics.showSamples=false
    |metrics.jvm=false
    |metrics.logback=false
  """.stripMargin)

  val minimalConfiguration = Configuration(minimalConfig)

  val mockMetrics = new ExportsMetrics(new MetricsImpl(new DefaultApplicationLifecycle(), minimalConfiguration))

}
