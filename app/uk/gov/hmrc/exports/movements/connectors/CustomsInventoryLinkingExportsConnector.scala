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

package uk.gov.hmrc.exports.movements.connectors

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import play.mvc.Http.Status
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class CustomsInventoryLinkingExportsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def sendInventoryLinkingRequest(eori: String, body: NodeSeq)(implicit hc: HeaderCarrier): Future[CustomsInventoryLinkingResponse] =
    post(eori, body.toString).map { response =>
      logger.debug(s"CUSTOMS_INVENTORY_LINKING_EXPORTS response is --> ${response.toString}")
      response
    }

  private[connectors] def post(eori: String, body: String)(implicit hc: HeaderCarrier): Future[CustomsInventoryLinkingResponse] = {
    logger.debug(s"CUSTOMS_INVENTORY_LINKING_EXPORTS request payload is -> $body")
    httpClient
      .POSTString[CustomsInventoryLinkingResponse](
        s"${appConfig.customsInventoryLinkingExportsRootUrl}${appConfig.sendArrivalUrlSuffix}",
        body,
        headers = headers(eori)
      )
      .recover {
        case error: Throwable =>
          logger.warn(s"Error from Customs Inventory Linking. $error")
          CustomsInventoryLinkingResponse(Status.INTERNAL_SERVER_ERROR, None)
      }
  }

  private def headers(eori: String)(implicit hc: HeaderCarrier): Seq[(String, String)] =
    Seq(
      HeaderNames.ACCEPT -> "application/vnd.hmrc.1.0+xml",
      HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
      CustomsHeaderNames.XClientIdName -> appConfig.clientIdInventory,
      CustomsHeaderNames.XEoriIdentifierHeaderName -> eori
    )
}
