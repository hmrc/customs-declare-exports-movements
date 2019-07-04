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

package integration.uk.gov.hmrc.exports.movements.connector

import com.github.tomakehurst.wiremock.http.Fault
import integration.uk.gov.hmrc.exports.movements.base.IntegrationTestSpec
import integration.uk.gov.hmrc.exports.movements.util.TestModule
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.http.HeaderCarrier
import utils.CustomsMovementsAPIConfig
import utils.ExternalServicesConfig.{Host, Port}
import utils.stubs.CustomsMovementsAPIService

import scala.concurrent.Future

class CustomsInventoryLinkingMovementsConnectorSpec
    extends IntegrationTestSpec with GuiceOneAppPerSuite with MockitoSugar with CustomsMovementsAPIService
    with ScalaFutures {

  private lazy val connector = app.injector.instanceOf[CustomsInventoryLinkingExportsConnector]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override implicit lazy val app: Application =
    GuiceApplicationBuilder(overrides = Seq(TestModule.asGuiceableModule))
      .configure(
        Map(
          "microservice.services.customs-inventory-linking-exports.host" -> Host,
          "microservice.services.customs-inventory-linking-exports.port" -> Port,
          "microservice.services.customs-inventory-linking-exports.sendArrival" -> CustomsMovementsAPIConfig.submitMovementServiceContext,
          "microservice.services.customs-inventory-linking-exports.client-id" -> CustomsMovementsAPIConfig.clientId
        )
      )
      .build()

  "Customs Inventory Linking Movements Connector" should {

    "return response with specific status" when {

      "it fail to connect to external service - 500" in {

        stopMockServer()

        val response = sendValidXml(validInventoryLinkingExportRequest.toXml).futureValue

        response.status should be(INTERNAL_SERVER_ERROR)
        response.conversationId should be(None)

        startMockServer()
      }

      "request is processed successfully - 202" in {

        startInventoryLinkingService(ACCEPTED)

        val response = sendValidXml(validInventoryLinkingExportRequest.toXml).futureValue

        response.status should be(ACCEPTED)
        response.conversationId should not be empty

        verifyILEServiceWasCalled(
          requestBody = validInventoryLinkingExportRequest.toXml,
          expectedEori = declarantEoriValue
        )
      }

      "request is processed successfully - 202 (without conversationId)" in {

        startInventoryLinkingService(ACCEPTED, conversationId = false)
        val response = sendValidXml(validInventoryLinkingExportRequest.toXml).futureValue

        response.status should be(ACCEPTED)
        response.conversationId should be(None)

        verifyILEServiceWasCalled(
          requestBody = validInventoryLinkingExportRequest.toXml,
          expectedEori = declarantEoriValue
        )
      }

      "request is not processed - 500" in {

        startInventoryLinkingService(INTERNAL_SERVER_ERROR)
        val response = sendValidXml(validInventoryLinkingExportRequest.toXml).futureValue

        response.status should be(INTERNAL_SERVER_ERROR)
        response.conversationId should not be empty
      }

      "request is not processed - 401" in {

        startInventoryLinkingService(UNAUTHORIZED)
        val response = sendValidXml(validInventoryLinkingExportRequest.toXml).futureValue

        response.status should be(UNAUTHORIZED)
        response.conversationId should not be empty
      }

      "request is not processed - 404" in {

        startInventoryLinkingService(NOT_FOUND)
        val response = sendValidXml(validInventoryLinkingExportRequest.toXml).futureValue

        response.status should be(NOT_FOUND)
        response.conversationId should not be empty
      }

      "request is not processed - 400 (without conversationId)" in {

        startInventoryLinkingService(BAD_REQUEST, conversationId = false)
        val response = sendValidXml("<xml><element>test</element></xml>").futureValue

        response.status should be(BAD_REQUEST)
        response.conversationId should be(None)
      }

      "request is not processed - 400 (with conversationId)" in {

        startInventoryLinkingService(BAD_REQUEST)
        val response = sendValidXml("<xml><element>test</element></xml>").futureValue

        response.status should be(BAD_REQUEST)
        response.conversationId should not be empty
      }

      "request is not processed - fault(CONNECTION_RESET_BY_PEER)" in {

        startFaultyInventoryLinkingService(Fault.CONNECTION_RESET_BY_PEER)
        val response = sendValidXml(validInventoryLinkingExportRequest.toXml).futureValue

        response.status should be(INTERNAL_SERVER_ERROR)
        response.conversationId should be(None)
      }

      "request is not processed - fault(EMPTY_RESPONSE)" in {

        startFaultyInventoryLinkingService(Fault.EMPTY_RESPONSE)
        val response = sendValidXml(validInventoryLinkingExportRequest.toXml).futureValue

        response.status should be(INTERNAL_SERVER_ERROR)
        response.conversationId should be(None)
      }

      "request is not processed - fault(MALFORMED_RESPONSE_CHUNK)" in {

        startFaultyInventoryLinkingService(Fault.MALFORMED_RESPONSE_CHUNK)
        val response = sendValidXml(validInventoryLinkingExportRequest.toXml).futureValue

        response.status should be(INTERNAL_SERVER_ERROR)
        response.conversationId should be(None)
      }

      "request is not processed - fault(RANDOM_DATA_THEN_CLOSE)" in {

        startFaultyInventoryLinkingService(Fault.RANDOM_DATA_THEN_CLOSE)
        val response = sendValidXml(validInventoryLinkingExportRequest.toXml).futureValue

        response.status should be(INTERNAL_SERVER_ERROR)
        response.conversationId should be(None)
      }
    }
  }

  private def sendValidXml(xml: String): Future[CustomsInventoryLinkingResponse] =
    connector.sendMovementRequest(declarantEoriValue, xml)
}
