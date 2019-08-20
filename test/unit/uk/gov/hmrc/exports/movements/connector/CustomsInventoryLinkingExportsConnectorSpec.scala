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

package unit.uk.gov.hmrc.exports.movements.connector

import play.api.http.ContentTypes
import play.api.http.Status.ACCEPTED
import play.api.mvc.Codec
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import unit.uk.gov.hmrc.exports.movements.base.{CustomsExportsBaseSpec, MockHttpClient}

import scala.concurrent.Future
import scala.util.Random
import scala.xml.{Elem, NodeSeq}

class CustomsInventoryLinkingExportsConnectorSpec extends CustomsExportsBaseSpec {

  val eori = "eori1"
  val xml: Elem = <Xml></Xml>

  val conversationId = "48bba359-7ba9-4cf1-85ba-95db2994638e"

  // TODO: updated the headers to match ones in connector
  val headers: Seq[(String, String)] = Seq(
    HeaderNames.ACCEPT -> "application/vnd.hmrc.1.0+xml",
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    CustomsHeaderNames.XClientIdName -> "5c68d3b5-d8a7-4212-8688-6b67f18bbce7",
    CustomsHeaderNames.XEoriIdentifierHeaderName -> "eori1"
  )

  "Customs Inventory Linking Exports Connector" should {

    "POST arrival to Customs Inventory Linking Exports endpoint" in sendArrival(eori, xml) { response =>
      response.futureValue.status must be(ACCEPTED)
      response.futureValue.conversationId must be(Some(conversationId))
    }
  }

  def sendArrival(
    eori: String,
    body: NodeSeq,
    hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(Random.alphanumeric.take(255).mkString)))
  )(test: Future[CustomsInventoryLinkingResponse] => Unit): Unit = {
    val expectedUrl: String = s"${appConfig.customsInventoryLinkingExportsRootUrl}${appConfig.sendArrivalUrlSuffix}"
    val falseServerError: Boolean = false
    val expectedHeaders: Seq[(String, String)] = headers
    val http = new MockHttpClient(
      wsClient,
      expectedUrl,
      body,
      expectedHeaders,
      falseServerError,
      CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId))
    )
    val client = new CustomsInventoryLinkingExportsConnector(appConfig, http)
    test(client.sendInventoryLinkingRequest(eori, body)(hc))
  }
}
