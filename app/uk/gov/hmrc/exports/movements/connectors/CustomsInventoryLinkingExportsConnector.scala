/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.exports.movements.connectors

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import play.mvc.Http.Status
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.{CustomsInventoryLinkingResponse, UserIdentification}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class CustomsInventoryLinkingExportsConnector @Inject() (appConfig: AppConfig, httpClient: HttpClient)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)
  private val contentHeaders = Seq(
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml",
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
  )

  def submit(identification: UserIdentification, body: NodeSeq)(implicit hc: HeaderCarrier): Future[CustomsInventoryLinkingResponse] =
    httpClient
      .POSTString[CustomsInventoryLinkingResponse](
        s"${appConfig.customsInventoryLinkingExportsRootUrl}${appConfig.sendArrivalUrlSuffix}",
        body.toString,
        headers = headers(identification)
      )
      .recover { case error: Throwable =>
        logger.warn(s"Error from Customs Inventory Linking. $error")
        CustomsInventoryLinkingResponse(Status.INTERNAL_SERVER_ERROR, None)
      }

  private def headers(identification: UserIdentification)(implicit hc: HeaderCarrier): Seq[(String, String)] = {
    val authHeaders =
      if (identification.providerId.isDefined)
        Seq(CustomsHeaderNames.SubmitterIdentifier -> appConfig.internalUserEori, CustomsHeaderNames.XClientIdName -> appConfig.clientIdInventory)
      else Seq(CustomsHeaderNames.XClientIdName -> appConfig.clientIdInventory)

    contentHeaders ++ authHeaders
  }
}
