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

package integration.uk.gov.hmrc.exports.movements.services

import integration.uk.gov.hmrc.exports.movements.base.IntegrationTestSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.Helpers._
import reactivemongo.core.errors.GenericDatabaseException
import uk.gov.hmrc.exports.movements.models.submissions.ActionType
import uk.gov.hmrc.exports.movements.repositories.SubmissionRepository
import uk.gov.hmrc.exports.movements.services.{CustomsInventoryLinkingUpstreamException, SubmissionService}
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import uk.gov.hmrc.http.HeaderCarrier
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder.{buildSubmissionRepositoryMock, dummyWriteResultSuccess}
import utils.CustomsMovementsAPIConfig
import utils.ExternalServicesConfig.{Host, Port}
import utils.stubs.CustomsMovementsAPIService
import utils.testdata.CommonTestData.validEori
import utils.testdata.MovementsTestData.validInventoryLinkingExportRequest

import scala.concurrent.{Await, Future}
import scala.xml.XML

class SubmissionServiceSpec
    extends IntegrationTestSpec with GuiceOneAppPerSuite with MockitoSugar with CustomsMovementsAPIService
    with ScalaFutures with IntegrationPatience {

  val mockMovementsRepository: SubmissionRepository = buildSubmissionRepositoryMock

  def overrideModules: Seq[GuiceableModule] = Nil

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(overrideModules: _*)
      .overrides(bind[SubmissionRepository].to(mockMovementsRepository))
      .configure(
        Map(
          "microservice.services.customs-inventory-linking-exports.host" -> Host,
          "microservice.services.customs-inventory-linking-exports.port" -> Port,
          "microservice.services.customs-inventory-linking-exports.sendArrival" -> CustomsMovementsAPIConfig.submitMovementServiceContext,
          "microservice.services.customs-inventory-linking-exports.client-id" -> CustomsMovementsAPIConfig.clientId
        )
      )
      .build()

  private lazy val movementsService = app.injector.instanceOf[SubmissionService]

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  def withMovementSubmissionPersisted(result: Boolean): Unit =
    when(mockMovementsRepository.insert(any())(any())).thenReturn(if (result) {
      Future.successful(dummyWriteResultSuccess)
    } else {
      Future.failed(GenericDatabaseException("There was a problem with Database", None))
    })

  "Movements Service" should {

    "save movement submission in DB" when {

      "Arrival is persisted" in {
        startInventoryLinkingService(ACCEPTED)
        withMovementSubmissionPersisted(true)

        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Arrival,
          requestXml = XML.loadString(validInventoryLinkingExportRequest.toXml)
        )
        movementsService.submitRequest(context).futureValue should equal((): Unit)
      }

      "Departure is persisted" in {

        startInventoryLinkingService(ACCEPTED)
        withMovementSubmissionPersisted(true)

        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Departure,
          requestXml = XML.loadString(validInventoryLinkingExportRequest.toXml)
        )
        movementsService.submitRequest(context).futureValue should equal((): Unit)
      }
    }

    "do not save movement submission in DB" when {

      "Arrival is not persisted" in {

        startInventoryLinkingService(ACCEPTED)
        withMovementSubmissionPersisted(false)

        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Arrival,
          requestXml = XML.loadString(validInventoryLinkingExportRequest.toXml)
        )

        an[Exception] mustBe thrownBy {
          Await.result(movementsService.submitRequest(context), patienceConfig.timeout)
        }
      }

      "Departure is not persisted" in {

        startInventoryLinkingService(ACCEPTED)
        withMovementSubmissionPersisted(false)

        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Departure,
          requestXml = XML.loadString(validInventoryLinkingExportRequest.toXml)
        )

        an[GenericDatabaseException] mustBe thrownBy {
          Await.result(movementsService.submitRequest(context), patienceConfig.timeout)
        }
      }

      "Arrival is not persisted (ACCEPTED but, no conversationID)" in {

        startInventoryLinkingService(ACCEPTED, conversationId = false)
        withMovementSubmissionPersisted(false)

        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Arrival,
          requestXml = XML.loadString(validInventoryLinkingExportRequest.toXml)
        )

        the[CustomsInventoryLinkingUpstreamException] thrownBy {
          Await.result(movementsService.submitRequest(context), patienceConfig.timeout)
        } should have message "Status: 202. ConverstationId: Not preset . Non Accepted status returned by Customs Inventory Linking Exports"
      }

      "Departure is not persisted (ACCEPTED but, no conversationID)" in {

        startInventoryLinkingService(ACCEPTED, conversationId = false)
        withMovementSubmissionPersisted(false)

        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Departure,
          requestXml = XML.loadString(validInventoryLinkingExportRequest.toXml)
        )
        the[CustomsInventoryLinkingUpstreamException] thrownBy {
          Await.result(movementsService.submitRequest(context), patienceConfig.timeout)
        } should have message "Status: 202. ConverstationId: Not preset . Non Accepted status returned by Customs Inventory Linking Exports"
      }

      "it is Not Accepted (BAD_REQUEST)" in {

        startInventoryLinkingService(BAD_REQUEST)
        withMovementSubmissionPersisted(false)

        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Arrival,
          requestXml = XML.loadString(validInventoryLinkingExportRequest.toXml)
        )

        val result = the[CustomsInventoryLinkingUpstreamException] thrownBy {
          Await.result(movementsService.submitRequest(context), patienceConfig.timeout)
        }
        result.getMessage should fullyMatch regex "Status: 400. ConverstationId: '.*' . Non Accepted status returned by Customs Inventory Linking Exports"
      }

      "it is Not Accepted (NOT_FOUND)" in {

        startInventoryLinkingService(NOT_FOUND)
        withMovementSubmissionPersisted(false)

        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Arrival,
          requestXml = XML.loadString(validInventoryLinkingExportRequest.toXml)
        )
        a[CustomsInventoryLinkingUpstreamException] mustBe thrownBy {
            Await.result(movementsService.submitRequest(context), patienceConfig.timeout)
        }
      }

      "it is Not Accepted (UNAUTHORIZED)" in {

        startInventoryLinkingService(UNAUTHORIZED)
        withMovementSubmissionPersisted(false)

        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Arrival,
          requestXml = XML.loadString(validInventoryLinkingExportRequest.toXml)
        )

        a[CustomsInventoryLinkingUpstreamException] mustBe thrownBy {
          Await.result(movementsService.submitRequest(context), patienceConfig.timeout)
        }
      }

      "it is Not Accepted (INTERNAL_SERVER_ERROR)" in {

        startInventoryLinkingService(INTERNAL_SERVER_ERROR)
        withMovementSubmissionPersisted(false)

        val context = SubmissionRequestContext(
          eori = validEori,
          actionType = ActionType.Arrival,
          requestXml = XML.loadString(validInventoryLinkingExportRequest.toXml)
        )
        a[CustomsInventoryLinkingUpstreamException] mustBe thrownBy {
          Await.result(movementsService.submitRequest(context), patienceConfig.timeout)
        }
      }
    }
  }
}
