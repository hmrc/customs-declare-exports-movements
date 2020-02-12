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

package uk.gov.hmrc.exports.movements.config

import com.google.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import uk.gov.hmrc.exports.movements.exceptions.MissingClientIDException
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(runModeConfiguration: Configuration, servicesConfig: ServicesConfig) {

  private val logger: Logger = Logger(classOf[AppConfig])

  lazy val authUrl: String = servicesConfig.baseUrl("auth")

  lazy val customsInventoryLinkingExportsRootUrl = servicesConfig.baseUrl("customs-inventory-linking-exports")

  lazy val sendArrivalUrlSuffix = servicesConfig.getConfString(
    "customs-inventory-linking-exports.sendArrival",
    throw new IllegalStateException("Missing configuration for Customs Inventory Linking send arrival URI")
  )

  def clientIdInventory(implicit hc: HeaderCarrier): String = {
    val userAgent = hc.headers.find(_._1.toLowerCase() == "user-agent").map(_._2).getOrElse {
      logger.warn("Request had missing User-Agent header.")
      throw MissingClientIDException("User Agent")
    }
    servicesConfig.getConfString(s"customs-inventory-linking-exports.client-id.$userAgent", throw MissingClientIDException(userAgent))
  }

  def clientIdInventoryConfig(implicit hc: HeaderCarrier): String = {
    val userAgent = hc.headers.find(_._1.toLowerCase() == "user-agent").map(_._2).getOrElse {
      logger.warn("Request have missing User-Agent header")
      throw new IllegalStateException("Request have missing User-Agent header")
    }
    servicesConfig.getConfString(s"customs-inventory-linking-exports.client-id.$userAgent", throw MissingClientIDException(userAgent))
  }

  lazy val ileSchemasFilePath = servicesConfig.getConfString(
    "customs-inventory-linking-exports.schema-file-path",
    throw new IllegalStateException("Missing configuration for ILE schemas file path")
  )
}
