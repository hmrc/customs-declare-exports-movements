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

package uk.gov.hmrc.exports.config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.PrivateMethodTester.{PrivateMethod, _}
import org.scalatest.mockito.MockitoSugar
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.test.UnitSpec

class AppConfigSpec extends UnitSpec with MockitoSugar {
  val environment = Environment.simple()
  val mode = PrivateMethod[Mode]('mode)
  val appNameConfiguration = PrivateMethod[Configuration]('appNameConfiguration)
  private val validAppConfig: Config =
    ConfigFactory.parseString(
      """
        |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
        |microservice.services.auth.host=localhostauth
        |microservice.services.auth.port=9988
        |microservice.services.customs-declarations.host=remotedec-api
        |microservice.services.customs-declarations.port=6000
        |microservice.services.customs-declarations.api-version=1.0
        |microservice.services.customs-declarations.submit-uri=/declarations
        |microservice.services.customs-declarations.cancel-uri=/declarations/cancel
        |microservice.services.customs-declarations.bearer-token=Bearer DummyBearerToken
      """.stripMargin)
  private val emptyAppConfig: Config = ConfigFactory.parseString("")
  private val validServicesConfiguration = Configuration(validAppConfig)
  private val emptyServicesConfiguration = Configuration(emptyAppConfig)

  private def appConfig(conf: Configuration) = new AppConfig(conf, environment)

  "AppConfig" should {
    "return config as object model when configuration is valid" in {
      val configService: AppConfig = appConfig(validServicesConfiguration)

      configService invokePrivate mode() shouldBe environment.mode
      configService invokePrivate appNameConfiguration() shouldBe validServicesConfiguration
      configService.authUrl shouldBe "http://localhostauth:9988"
      configService.loginUrl shouldBe "http://localhost:9949/auth-login-stub/gg-sign-in"
    }

    "throw an exception when mandatory configuration is invalid" in {
      val configService: AppConfig = appConfig(emptyServicesConfiguration)

      val caught: RuntimeException = intercept[RuntimeException](configService.authUrl)
      caught.getMessage shouldBe "Could not find config auth.host"

      val caught2: Exception = intercept[Exception](configService.loginUrl)
      caught2.getMessage shouldBe "Missing configuration key: urls.login"
    }
  }
}
