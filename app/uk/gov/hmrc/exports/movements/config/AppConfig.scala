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

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(
  runModeConfiguration: Configuration,
  servicesConfig: ServicesConfig
) {

  lazy val authUrl: String = servicesConfig.baseUrl("auth")

  lazy val customsInventoryLinkingExports = servicesConfig.baseUrl("customs-inventory-linking-exports")
  lazy val sendArrival = servicesConfig.getConfString(
    "customs-inventory-linking-exports.sendArrival",
    throw new IllegalStateException("Missing configuration for Customs Inventory Linking send arrival URI")
  )
  lazy val clientIdInventory = servicesConfig.getConfString(
    "customs-inventory-linking-exports.client-id",
    throw new IllegalStateException("Missing configuration for Customs Inventory Linking Client Id")
  )
}
