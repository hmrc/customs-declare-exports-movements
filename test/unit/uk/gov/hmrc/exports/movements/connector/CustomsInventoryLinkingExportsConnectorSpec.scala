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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.http.Status.ACCEPTED
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.http.HeaderCarrier

import scala.xml.Elem

class CustomsInventoryLinkingExportsConnectorSpec extends ConnectorSpec {

  private val xml: Elem = <Xml></Xml>
  private val config = mock[AppConfig]
  private def connector = new CustomsInventoryLinkingExportsConnector(config, httpClient)

  "Customs Inventory Linking Exports Connector" should {
    given(config.customsInventoryLinkingExportsRootUrl).willReturn(downstreamURL)
    given(config.sendArrivalUrlSuffix).willReturn("/path")
    given(config.clientIdInventory(ArgumentMatchers.any[HeaderCarrier]())).willReturn("client-id")

    "POST to ILE" in {
      stubFor(
        post("/path")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withHeader(CustomsHeaderNames.XConversationIdName, "conv-id")
          )
      )

      val result: CustomsInventoryLinkingResponse = await(connector.sendInventoryLinkingRequest("eori", xml)(hc))

      result.status mustBe ACCEPTED
      result.conversationId mustBe Some("conv-id")
      verify(
        postRequestedFor(urlEqualTo("/path"))
          .withRequestBody(equalTo("<Xml></Xml>"))
          .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+xml"))
          .withHeader("Content-Type", equalTo("application/xml; charset=utf-8"))
          .withHeader("X-Client-ID", equalTo("client-id"))
      )
    }
  }
}
