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

package uk.gov.hmrc.exports.movements.connector

import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.exports.movements.base.WiremockTestServer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import utils.testdata.MovementsTestData.dateTimeString

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.ExecutionContext

class ConnectorISpec extends AnyWordSpec with GuiceOneAppPerSuite with WiremockTestServer with Matchers with BeforeAndAfterEach {

  private val clock = Clock.fixed(Instant.parse(dateTimeString), ZoneOffset.UTC)

  override def fakeApplication(): Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder()
      .overrides(bind[Clock].to(clock))
      .build()
  }

  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier = HeaderCarrier()
  protected val httpClientV2: HttpClientV2 = app.injector.instanceOf[HttpClientV2]
}
