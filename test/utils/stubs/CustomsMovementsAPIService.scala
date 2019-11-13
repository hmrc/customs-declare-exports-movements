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

package utils.stubs

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.matching.UrlPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import integration.uk.gov.hmrc.exports.movements.base.WireMockRunner
import play.api.http.ContentTypes
import play.api.libs.json.JsValue
import play.api.mvc.Codec
import play.api.test.Helpers.{ACCEPTED, CONTENT_TYPE}
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import utils.CustomsMovementsAPIConfig

trait CustomsMovementsAPIService extends WireMockRunner {

  private val movementsURL = urlMatching(CustomsMovementsAPIConfig.submitMovementServiceContext)

  def startInventoryLinkingService(status: Int = ACCEPTED, conversationId: Boolean = true): Unit =
    startService(status, movementsURL, conversationId)

  def startFaultyInventoryLinkingService(fault: Fault): Unit =
    stubFor(post(movementsURL).willReturn(aResponse().withFault(fault)))

  private def startService(status: Int, url: UrlPattern, conversationId: Boolean): StubMapping =
    if (conversationId) {

      stubFor(
        post(url).willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("X-Conversation-ID", UUID.randomUUID().toString)
        )
      )
    } else {

      stubFor(
        post(url).willReturn(
          aResponse()
            .withStatus(status)
        )
      )
    }

  def verifyILEServiceWasCalled(requestBody: String): Unit =
    verifyILEServiceWasCalledWith(CustomsMovementsAPIConfig.submitMovementServiceContext, requestBody)

  def verifyILEServiceWasNotCalled(): Unit =
    verify(exactly(0), postRequestedFor(urlMatching(CustomsMovementsAPIConfig.submitMovementServiceContext)))

  private def verifyILEServiceWasCalledWith(requestPath: String, requestBody: String): Unit =
    verify(
      1,
      postRequestedFor(urlMatching(requestPath))
        .withHeader(CONTENT_TYPE, equalTo(ContentTypes.XML(Codec.utf_8)))
        .withHeader(CustomsHeaderNames.XClientIdName, equalTo(CustomsMovementsAPIConfig.clientId))
        .withRequestBody(equalToXml(requestBody))
    )
}
