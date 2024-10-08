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

package uk.gov.hmrc.exports.movements.config

import com.typesafe.config.{Config, ConfigFactory}
import play.api.Configuration
import uk.gov.hmrc.exports.movements.base.UnitSpec
import uk.gov.hmrc.exports.movements.exceptions.MissingClientIDException
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Duration
import java.time.temporal.ChronoUnit

class AppConfigSpec extends UnitSpec {

  private val validAppConfig: Config =
    ConfigFactory.parseString("""
        |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
        |microservice.services.auth.host=localhost.auth
        |mongodb.uri="mongodb://localhost:27017/customs-declare-exports-movements"
        |microservice.services.auth.port=8500
        |microservice.services.customs-inventory-linking-exports.host=localhost.ile
        |microservice.services.customs-inventory-linking-exports.port=9823
        |microservice.services.customs-inventory-linking-exports.api-version=1.0
        |microservice.services.customs-inventory-linking-exports.sendArrival=/
        |microservice.services.customs-inventory-linking-exports.client-id.some-user-agent=some-user-agent-client-id
        |microservice.services.customs-inventory-linking-exports.schema-file-path=conf/schemas/exports/inventoryLinkingResponseExternal.xsd
        |microservice.services.customs-inventory-linking-exports.internal-user-eori=ABC123
        |microservice.ileQueryResponseTimeout.value=30
        |microservice.ileQueryResponseTimeout.unit=SECONDS
      """.stripMargin)
  private val invalidAppConfig: Config = ConfigFactory.parseString("""
      |mongodb.uri="mongodb://localhost:27017/customs-movements-frontend"
      |""".stripMargin)

  private val validServicesConfiguration = Configuration(validAppConfig)
  private val invalidServicesConfiguration = Configuration(invalidAppConfig)

  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf)
  private def appConfig(conf: Configuration) = new AppConfig(conf, servicesConfig(conf), "customs-declare-exports-movements")

  "AppConfig" should {

    "return config as object model when configuration is valid" in {
      val serviceConfig: AppConfig = appConfig(validServicesConfiguration)

      serviceConfig.authUrl shouldEqual "http://localhost.auth:8500"
      serviceConfig.customsInventoryLinkingExportsRootUrl shouldEqual "http://localhost.ile:9823"
      serviceConfig.sendArrivalUrlSuffix shouldEqual "/"
    }

    "return client ID" when {
      "user agent is present" in {
        val serviceConfig: AppConfig = appConfig(validServicesConfiguration)
        val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq("User-Agent" -> "some-user-agent"))
        serviceConfig.clientIdInventory(hc) shouldEqual "some-user-agent-client-id"
      }

      "user agent is missing" in {
        val serviceConfig: AppConfig = appConfig(validServicesConfiguration)
        val hc: HeaderCarrier = HeaderCarrier()

        val exception = intercept[MissingClientIDException] {
          serviceConfig.clientIdInventory(hc)
        }

        exception.getMessage shouldBe "Missing Client ID for [User Agent]"
      }
    }

    "throw an exception when mandatory configuration is invalid" in {
      val configService: AppConfig = appConfig(invalidServicesConfiguration)

      val caught: RuntimeException = intercept[RuntimeException](configService.authUrl)
      caught.getMessage shouldBe "Could not find config key 'auth.host'"

      val caught2: Exception = intercept[Exception](configService.customsInventoryLinkingExportsRootUrl)
      caught2.getMessage shouldBe "Could not find config key 'customs-inventory-linking-exports.host'"

      val caught3: Exception = intercept[Exception](configService.sendArrivalUrlSuffix)
      caught3.getMessage shouldBe "Missing configuration for Customs Inventory Linking send arrival URI"

      val caught4: Exception = intercept[Exception](configService.internalUserEori)
      caught4.getMessage shouldBe "Missing configuration for internal user EORI value"
    }

    "contain correct Inventory Linking url" in {
      val serviceConfig: AppConfig = appConfig(validServicesConfiguration)

      serviceConfig.customsInventoryLinkingExportsRootUrl shouldEqual "http://localhost.ile:9823"
    }

    "contain correct path to Inventory Linking Export schemas" in {
      val serviceConfig: AppConfig = appConfig(validServicesConfiguration)

      serviceConfig.ileSchemasFilePath shouldEqual "conf/schemas/exports/inventoryLinkingResponseExternal.xsd"
    }

    "contain correct value and unit for ILE Query response timeout" in {
      val serviceConfig: AppConfig = appConfig(validServicesConfiguration)

      val duration = serviceConfig.ileQueryResponseTimeout
      duration shouldBe Duration.of(30, ChronoUnit.SECONDS)
    }

    "contain correct value for the internal user EORI" in {
      val serviceConfig: AppConfig = appConfig(validServicesConfiguration)

      serviceConfig.internalUserEori shouldEqual "ABC123"
    }
  }
}
