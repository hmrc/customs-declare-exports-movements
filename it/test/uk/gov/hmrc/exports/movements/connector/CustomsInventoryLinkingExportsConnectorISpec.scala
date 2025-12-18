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

package uk.gov.hmrc.exports.movements.connector

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status
import play.api.http.Status.ACCEPTED
import play.api.test.Helpers.*
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.{CustomsInventoryLinkingResponse, UserIdentification}
import uk.gov.hmrc.http.HeaderCarrier

import scala.xml.Elem

class CustomsInventoryLinkingExportsConnectorISpec extends ConnectorISpec {

  private val xml: Elem = <Xml></Xml>
  private val config = mock[AppConfig]
  private def connector = new CustomsInventoryLinkingExportsConnector(config, httpClientV2)

  "Customs Inventory Linking Exports Connector" should {
    when(config.customsInventoryLinkingExportsRootUrl).thenReturn(downstreamURL)
    when(config.sendArrivalUrlSuffix).thenReturn("/path")
    when(config.customsDeclarationsApiVersion).thenReturn("1.0")
    when(config.clientIdInventory(ArgumentMatchers.any[HeaderCarrier]())).thenReturn("client-id")
    when(config.internalUserEori).thenReturn("ABC123")

    "POST to ILE" when {
      "EORI only request" in {
        stubFor(
          post("/path")
            .willReturn(
              aResponse()
                .withStatus(Status.ACCEPTED)
                .withHeader(CustomsHeaderNames.XConversationIdName, "conv-id")
            )
        )

        val result: CustomsInventoryLinkingResponse = await(connector.submit(identification(None), xml)(hc))

        result.status mustBe ACCEPTED
        result.conversationId mustBe Some("conv-id")
        verify(
          postRequestedFor(urlEqualTo("/path"))
            .withRequestBody(equalTo("<Xml></Xml>"))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+xml"))
            .withHeader("Content-Type", equalTo("application/xml; charset=utf-8"))
            .withHeader("X-Client-ID", equalTo("client-id"))
            .withoutHeader("X-Badge-Identifier")
            .withoutHeader("X-Submitter-Identifier")
        )
      }

      "privileged request" in {
        stubFor(
          post("/path")
            .willReturn(
              aResponse()
                .withStatus(Status.ACCEPTED)
                .withHeader(CustomsHeaderNames.XConversationIdName, "conv-id")
            )
        )

        val result: CustomsInventoryLinkingResponse = await(connector.submit(identification(Some("id")), xml)(hc))

        result.status mustBe ACCEPTED
        result.conversationId mustBe Some("conv-id")
        verify(
          postRequestedFor(urlEqualTo("/path"))
            .withRequestBody(equalTo("<Xml></Xml>"))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+xml"))
            .withHeader("Content-Type", equalTo("application/xml; charset=utf-8"))
            .withHeader("X-Client-ID", equalTo("client-id"))
            .withHeader("X-Submitter-Identifier", equalTo("ABC123"))
        )
      }

      "handle upstream error" in {
        stubFor(
          post("/path")
            .willReturn(
              aResponse()
                .withFault(CONNECTION_RESET_BY_PEER)
            )
        )

        val result: CustomsInventoryLinkingResponse = await(connector.submit(identification(Some("id")), xml)(hc))

        result.status mustBe INTERNAL_SERVER_ERROR
        result.conversationId mustBe None
        verify(
          postRequestedFor(urlEqualTo("/path"))
            .withRequestBody(equalTo("<Xml></Xml>"))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+xml"))
            .withHeader("Content-Type", equalTo("application/xml; charset=utf-8"))
            .withHeader("X-Client-ID", equalTo("client-id"))
            .withHeader("X-Submitter-Identifier", equalTo("ABC123"))
        )
      }
    }
  }

  private def identification(userProviderId: Option[String]): UserIdentification = new UserIdentification {
    override val eori: String = "eori"
    override val providerId: Option[String] = userProviderId
  }
}
