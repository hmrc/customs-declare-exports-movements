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

package integration.uk.gov.hmrc.exports.movements.base

import com.google.inject.AbstractModule
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.inject.guice.GuiceableModule
import uk.gov.hmrc.play.test.UnitSpec

object IntegrationTestModule extends AbstractModule {
  def configure(): Unit = ()

  def asGuiceableModule: GuiceableModule = GuiceableModule.guiceable(this)
}
// TODO: moved the before/after here
trait IntegrationTestSpec extends UnitSpec
  with BeforeAndAfterEach with BeforeAndAfterAll with WireMockRunner with Eventually {

  override protected def beforeAll() {
    startMockServer()
  }

  override protected def afterEach(): Unit =
    resetMockServer()

  override protected def afterAll() {
    stopMockServer()
  }
}
