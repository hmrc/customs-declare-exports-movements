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

package uk.gov.hmrc.exports.movements.config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.PrivateMethodTester.PrivateMethod
import org.scalatest.mockito.MockitoSugar
import play.api.Mode.Test
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import uk.gov.hmrc.play.test.UnitSpec

class AppConfigSpec extends UnitSpec with MockitoSugar {
  val appNameConfiguration = PrivateMethod[Configuration]('appNameConfiguration)
  private val validAppConfig: Config =
    ConfigFactory.parseString("""
        |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
        |microservice.services.auth.host=localhostauth
        |microservice.services.auth.port=9988
        |microservice.services.customs-inventory-linking-exports.host=localhostile
        |microservice.services.customs-inventory-linking-exports.port=9875
        |microservice.services.customs-inventory-linking-exports.sendArrival=/
        |microservice.services.customs-inventory-linking-exports.client-id=xx1445

      """.stripMargin)
  private val emptyAppConfig: Config = ConfigFactory.parseString("")
  private val validServicesConfiguration = Configuration(validAppConfig)
  private val emptyServicesConfiguration = Configuration(emptyAppConfig)

  private def runMode(conf: Configuration): RunMode = new RunMode(conf, Test)
  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf, runMode(conf))
  private def appConfig(conf: Configuration) = new AppConfig(conf, servicesConfig(conf))

  "AppConfig" should {
    "return config as object model when configuration is valid" in {
      val configService: AppConfig = appConfig(validServicesConfiguration)

      configService.authUrl shouldBe "http://localhostauth:9988"
      configService.customsInventoryLinkingExports shouldBe "http://localhostile:9875"
      configService.sendArrival shouldBe "/"
      configService.clientIdInventory shouldBe "xx1445"
    }

    "throw an exception when mandatory configuration is invalid" in {
      val configService: AppConfig = appConfig(emptyServicesConfiguration)

      val caught: RuntimeException = intercept[RuntimeException](configService.authUrl)
      caught.getMessage shouldBe "Could not find config auth.host"

      val caught2: Exception = intercept[Exception](configService.customsInventoryLinkingExports)
      caught2.getMessage shouldBe "Could not find config customs-inventory-linking-exports.host"

      val caught3: Exception = intercept[Exception](configService.sendArrival)
      caught3.getMessage shouldBe "Missing configuration for Customs Inventory Linking send arrival URI"

      val caught4: Exception = intercept[Exception](configService.clientIdInventory)
      caught4.getMessage shouldBe "Missing configuration for Customs Inventory Linking Client Id"
    }
  }
}
