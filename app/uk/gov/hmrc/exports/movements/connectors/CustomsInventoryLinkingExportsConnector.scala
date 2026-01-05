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

package uk.gov.hmrc.exports.movements.connectors

import play.api.Logging
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.ws.writeableOf_String
import play.api.mvc.Codec
import play.mvc.Http.Status
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.{CustomsInventoryLinkingResponse, UserIdentification}
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class CustomsInventoryLinkingExportsConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2)(implicit ec: ExecutionContext)
    extends Logging {

  private val contentHeaders = List(
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml",
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
  )

  def submit(identification: UserIdentification, body: NodeSeq)(implicit hc: HeaderCarrier): Future[CustomsInventoryLinkingResponse] =
    post(s"${appConfig.customsInventoryLinkingExportsRootUrl}${appConfig.sendArrivalUrlSuffix}", body.mkString, headers(identification)).recover {
      case error: Throwable =>
        logger.warn(s"Error from Customs Inventory Linking. $error")
        CustomsInventoryLinkingResponse(Status.INTERNAL_SERVER_ERROR, None)
    }

  private def headers(identification: UserIdentification)(implicit hc: HeaderCarrier): Seq[(String, String)] = {
    val authHeaders = List(
      identification.providerId.map(_ => CustomsHeaderNames.SubmitterIdentifier -> appConfig.internalUserEori),
      Some(CustomsHeaderNames.XClientIdName -> appConfig.clientIdInventory)
    ).flatten

    contentHeaders ++ authHeaders
  }

  private def post(url: String, body: String, additionalHeaders: Seq[(String, String)])(
    implicit ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[CustomsInventoryLinkingResponse] =
    transform(httpClientV2.post(url"$url"), additionalHeaders).withBody(body).execute[CustomsInventoryLinkingResponse]

  private def transform(rb: RequestBuilder, additionalHeaders: Seq[(String, String)]): RequestBuilder =
    rb.transform(_.addHttpHeaders(additionalHeaders: _*))
}
