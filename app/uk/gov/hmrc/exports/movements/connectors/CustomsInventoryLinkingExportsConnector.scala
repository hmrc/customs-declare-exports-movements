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
import play.mvc.Http.Status
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsInventoryLinkingExportsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  def sendMovementRequest(
    eori: String,
    body: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsInventoryLinkingResponse] =
    post(eori, body).map { response =>
      Logger.debug(s"CUSTOMS_INVENTORY_LINKING_EXPORTS response is --> ${response.toString}")
      response
    }

  //noinspection ConvertExpressionToSAM
  val responseReader: HttpReads[CustomsInventoryLinkingResponse] =
    new HttpReads[CustomsInventoryLinkingResponse] {
      override def read(method: String, url: String, response: HttpResponse): CustomsInventoryLinkingResponse =
        CustomsInventoryLinkingResponse(response.status, response.header("X-Conversation-ID"))
    }
  private[connectors] def post(
    eori: String,
    body: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsInventoryLinkingResponse] = {
    Logger.debug(s"CUSTOMS_DECLARATIONS request payload is -> $body")
    httpClient
      .POSTString[CustomsInventoryLinkingResponse](
        s"${appConfig.customsInventoryLinkingExports}${appConfig.sendArrival}",
        body,
        headers = headers(eori)
      )(responseReader, hc, ec)
      .recover {
        case error: Throwable =>
          Logger.error(s"Error to check development environment ${error.toString}")
          Logger.error(s"Error to check development environment (GET MESSAGE) ${error.getMessage}")
          CustomsInventoryLinkingResponse(Status.INTERNAL_SERVER_ERROR, None)
      }
  }

  private def headers(eori: String): Seq[(String, String)] = Seq(
    "Accept" -> "application/vnd.hmrc.1.0+xml",
    "Content-Type" -> "application/xml;charset=utf-8",
    "X-Client-ID" -> appConfig.clientIdInventory,
    "X-EORI-Identfier" -> eori
  )

}
