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

package uk.gov.hmrc.exports.movements.base

import com.google.inject.AbstractModule
import org.scalatest._
import org.scalatest.concurrent.Eventually
import play.api.inject.guice.GuiceableModule

object IntegrationTestModule extends AbstractModule {

  override def configure(): Unit = ()

  def asGuiceableModule: GuiceableModule = GuiceableModule.guiceable(this)
}

trait IntegrationTestSpec extends UnitSpec with BeforeAndAfterEach with BeforeAndAfterAll with WireMockRunner with Eventually {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startMockServer()
  }

  override protected def afterEach(): Unit = {
    resetMockServer()
    super.afterEach()
  }

  override protected def afterAll(): Unit = {
    stopMockServer()
    super.afterAll()
  }
}
