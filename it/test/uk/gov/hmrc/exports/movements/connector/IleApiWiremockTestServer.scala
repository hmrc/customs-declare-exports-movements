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

package uk.gov.hmrc.exports.movements.connector

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.{RequestPatternBuilder, UrlPattern}
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import play.api.Configuration
import play.api.http.Status
import uk.gov.hmrc.exports.movements.base.WiremockTestServer

import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters.ListHasAsScala

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

  val urlOfILE: UrlPattern = urlEqualTo("/")

  protected def givenIleApiAcceptsTheSubmission(): Unit =
    stubFor(
      post(urlOfILE)
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("X-Conversation-ID", "conversation-id")
        )
    )

  protected def postRequestedToILE(): RequestPatternBuilder = postRequestedFor(urlOfILE)

  def bodyOfGetRequest(url: UrlPattern): String =
    bodyOfRequest(WireMock.findAll(getRequestedFor(url)).asScala.toList)

  def bodyOfPostRequest(url: UrlPattern): String =
    bodyOfRequest(WireMock.findAll(postRequestedFor(url)).asScala.toList)

  private def bodyOfRequest(requests: List[LoggedRequest]): String = {
    assert(requests.length == 1)
    new String(requests.head.getBody, StandardCharsets.UTF_8)
  }
}
