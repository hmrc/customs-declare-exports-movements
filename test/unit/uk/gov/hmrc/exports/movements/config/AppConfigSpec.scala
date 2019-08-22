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

package unit.uk.gov.hmrc.exports.movements.config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.PrivateMethodTester.PrivateMethod
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import play.api.Mode.Test
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec

class AppConfigSpec extends UnitSpec with MockitoSugar {

  private val appNameConfiguration = PrivateMethod[Configuration]('appNameConfiguration)

  private val validAppConfig: Config =
    ConfigFactory.parseString(
      """
        |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
        |microservice.services.auth.host=localhost.auth
        |microservice.services.auth.port=8500
        |microservice.services.customs-inventory-linking-exports.host=localhost.ile
        |microservice.services.customs-inventory-linking-exports.port=9823
        |microservice.services.customs-inventory-linking-exports.sendArrival=/
        |microservice.services.customs-inventory-linking-exports.client-id=5c68d3b5-d8a7-4212-8688-6b67f18bbce7
        |microservice.services.customs-inventory-linking-exports.schema-file-path=conf/schemas/exports/inventoryLinkingResponseExternal.xsd
      """.stripMargin
    )
  private val emptyAppConfig: Config = ConfigFactory.parseString("")
  private val validServicesConfiguration = Configuration(validAppConfig)
  private val emptyServicesConfiguration = Configuration(emptyAppConfig)

  private def runMode(conf: Configuration): RunMode = new RunMode(conf, Test)
  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf, runMode(conf))
  private def appConfig(conf: Configuration) = new AppConfig(conf, servicesConfig(conf))

  "AppConfig" should {

    "return config as object model when configuration is valid" in {
      val serviceConfig: AppConfig = appConfig(validServicesConfiguration)

      serviceConfig.authUrl shouldEqual "http://localhost.auth:8500"
      serviceConfig.customsInventoryLinkingExportsRootUrl shouldEqual "http://localhost.ile:9823"
      serviceConfig.sendArrivalUrlSuffix shouldEqual "/"
      serviceConfig.clientIdInventory shouldEqual "5c68d3b5-d8a7-4212-8688-6b67f18bbce7"
    }

    "throw an exception when mandatory configuration is invalid" in {
      val configService: AppConfig = appConfig(emptyServicesConfiguration)

      val caught: RuntimeException = intercept[RuntimeException](configService.authUrl)
      caught.getMessage shouldBe "Could not find config auth.host"

      val caught2: Exception = intercept[Exception](configService.customsInventoryLinkingExportsRootUrl)
      caught2.getMessage shouldBe "Could not find config customs-inventory-linking-exports.host"

      val caught3: Exception = intercept[Exception](configService.sendArrivalUrlSuffix)
      caught3.getMessage shouldBe "Missing configuration for Customs Inventory Linking send arrival URI"

      val caught4: Exception = intercept[Exception](configService.clientIdInventory)
      caught4.getMessage shouldBe "Missing configuration for Customs Inventory Linking Client Id"
    }

    "contain correct Inventory Linking url" in {
      val serviceConfig: AppConfig = appConfig(validServicesConfiguration)

      serviceConfig.customsInventoryLinkingExportsRootUrl shouldEqual "http://localhost.ile:9823"
    }

    "contain correct path to Inventory Linking Export schemas" in {
      val serviceConfig: AppConfig = appConfig(validServicesConfiguration)

      serviceConfig.ileSchemasFilePath shouldEqual "conf/schemas/exports/inventoryLinkingResponseExternal.xsd"
    }
  }
}
