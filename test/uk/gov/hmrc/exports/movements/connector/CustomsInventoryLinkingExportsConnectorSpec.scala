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

package uk.gov.hmrc.exports.movements.connector

import java.util.UUID

import play.api.http.Status.ACCEPTED
import uk.gov.hmrc.exports.movements.base.{CustomsExportsBaseSpec, MockHttpClient}
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization

import scala.concurrent.Future
import scala.util.Random

class CustomsInventoryLinkingExportsConnectorSpec extends CustomsExportsBaseSpec {

  val eori = "eori1"
  val xml = "Xml"

  val conversationId = "48bba359-7ba9-4cf1-85ba-95db2994638e"

  val headers: Seq[(String, String)] = Seq(
    "Accept" -> "application/vnd.hmrc.1.0+xml",
    "Content-Type" -> "application/xml;charset=utf-8",
    "X-Client-ID" -> "5c68d3b5-d8a7-4212-8688-6b67f18bbce7",
    "X-EORI-Identfier" -> "eori1"
  )

  "Customs Inventory Linking Exports Connector" should {
    "POST arrival to Customs Inventory Linking Exports endpoint" in sendArrival(eori, xml) { response =>
      response.futureValue.status must be(ACCEPTED)

      response.futureValue.conversationId must be(Some(conversationId))
    }
  }

  def sendArrival(
    eori: String,
    body: String,
    hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(Random.alphanumeric.take(255).mkString)))
  )(test: Future[CustomsInventoryLinkingResponse] => Unit): Unit = {
    val expectedUrl: String = s"${appConfig.customsInventoryLinkingExports}${appConfig.sendArrival}"
    val falseServerError: Boolean = false
    val expectedHeaders: Seq[(String, String)] = headers
    val http = new MockHttpClient(expectedUrl, body, expectedHeaders, falseServerError, CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId)))
    val client = new CustomsInventoryLinkingExportsConnector(appConfig, http)
    test(client.sendMovementRequest(eori, body)(hc, ec))
  }
}
