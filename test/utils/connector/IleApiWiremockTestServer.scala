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

package utils.connector

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, postRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.matching.{RequestPatternBuilder, StringValuePattern}
import integration.uk.gov.hmrc.exports.movements.connector.WiremockTestServer
import play.api.Configuration
import play.api.http.Status

import scala.xml.NodeSeq

trait IleApiWiremockTestServer extends WiremockTestServer {

  protected val clientId = "some-client-id"
  protected val userAgent = "some-user-agent"
  protected val ileApieConfiguration: Configuration =
    Configuration.from(
      Map(
        "mongodb.uri" -> "mongodb://localhost:27017/customs-declare-exports-movements",
        "microservice.services.customs-inventory-linking-exports.port" -> wirePort,
        "microservice.services.customs-inventory-linking-exports.client-id.some-user-agent" -> clientId
      )
    )

  protected def givenIleApiAcceptsTheSubmission(conversationId: String): Unit =
    stubFor(
      post("/")
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("X-Conversation-ID", conversationId)
        )
    )

  protected def postRequestedToILE(): RequestPatternBuilder = postRequestedFor(urlEqualTo("/"))

  protected def equalToXml(nodeSeq: NodeSeq): StringValuePattern = WireMock.equalToXml(nodeSeq.toString())

}
