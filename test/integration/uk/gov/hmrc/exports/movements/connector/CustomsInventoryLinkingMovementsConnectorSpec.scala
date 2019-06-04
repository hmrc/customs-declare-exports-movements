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

import integration.uk.gov.hmrc.exports.movements.base.IntegrationTestSpec
import utils.ExternalServicesConfig.{Host, Port}
import integration.uk.gov.hmrc.exports.movements.util.TestModule
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.http.HeaderCarrier
import utils.CustomsDeclarationsAPIConfig
import utils.stubs.CustomsDeclarationsAPIService

import scala.concurrent.Future


class CustomsInventoryLinkingMovementsConnectorSpec  extends IntegrationTestSpec with GuiceOneAppPerSuite with MockitoSugar with CustomsDeclarationsAPIService {

  private lazy val connector = app.injector.instanceOf[CustomsInventoryLinkingExportsConnector]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override implicit lazy val app: Application =
    GuiceApplicationBuilder(overrides = Seq(TestModule.asGuiceableModule))
      .configure(
        Map(
          "microservice.services.customs-declarations.host" -> Host,
          "microservice.services.customs-declarations.port" -> Port,
          "microservice.services.customs-declarations.submit-uri" -> CustomsDeclarationsAPIConfig.submitDeclarationServiceContext,
          "microservice.services.customs-declarations.bearer-token" -> authToken,
          "microservice.services.customs-declarations.api-version" -> CustomsDeclarationsAPIConfig.apiVersion
        )
      )
      .build()

  "Customs Inventory Linking Movements Connector" should {

    "return response with specific status" when {

      "it fail to connect to external service - 500" in {

        stopMockServer()

        val response = await(sendValidXml(validInventoryLinkingExportRequest.toXml))
        response.status should be(INTERNAL_SERVER_ERROR)

        startMockServer()
      }

    }
  }

  private def sendValidXml(xml: String): Future[CustomsInventoryLinkingResponse] =
    connector.sendMovementRequest(declarantEoriValue, xml)
}
