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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.repositories.MovementsRepository
import uk.gov.hmrc.exports.movements.services.MovementsService
import uk.gov.hmrc.http.HeaderCarrier
import utils.ExternalServicesConfig.{Host, Port}
import utils.stubs.CustomsMovementsAPIService
import utils.{CustomsMovementsAPIConfig, MovementsTestData}

import scala.concurrent.Future
import scala.xml.XML

class MovementsServiceSpec
    extends IntegrationTestSpec with GuiceOneAppPerSuite with MockitoSugar with CustomsMovementsAPIService
    with MovementsTestData with ScalaFutures {

  val mockMovementsRepository: MovementsRepository = mock[MovementsRepository]

  def overrideModules: Seq[GuiceableModule] = Nil

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(overrideModules: _*)
      .overrides(bind[MovementsRepository].to(mockMovementsRepository))
      .configure(
        Map(
          "microservice.services.customs-inventory-linking-exports.host" -> Host,
          "microservice.services.customs-inventory-linking-exports.port" -> Port,
          "microservice.services.customs-inventory-linking-exports.sendArrival" -> CustomsMovementsAPIConfig.submitMovementServiceContext,
          "microservice.services.customs-inventory-linking-exports.client-id" -> CustomsMovementsAPIConfig.clientId
        )
      )
      .build()

  private lazy val movementsService = app.injector.instanceOf[MovementsService]

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  def withMovementSubmissionPersisted(result: Boolean): Unit =
    when(mockMovementsRepository.save(any())).thenReturn(Future.successful(result))

  "Movements Service" should {

    "save movement submission in DB" when {

      "Arrival is persisted" in {

        startInventoryLinkingService(ACCEPTED)
        withMovementSubmissionPersisted(true)

        val result: Future[Result] =
          movementsService.handleMovementSubmission(
            declarantEoriValue,
            declarantUcrValue,
            "Arrival",
            XML.loadString(validInventoryLinkingExportRequest.toXml)
          )

        contentAsString(result) should be("Movement Submission submitted and persisted ok")
        result.futureValue.header.status should be(ACCEPTED)
      }

      "Departure is persisted" in {

        startInventoryLinkingService(ACCEPTED)
        withMovementSubmissionPersisted(true)

        val result: Future[Result] =
          movementsService.handleMovementSubmission(
            declarantEoriValue,
            declarantUcrValue,
            "Departure",
            XML.loadString(validInventoryLinkingExportRequest.toXml)
          )

        contentAsString(result) should be("Movement Submission submitted and persisted ok")
        result.futureValue.header.status should be(ACCEPTED)
      }
    }

    "do not save movement submission in DB" when {

      "Arrival is not persisted" in {

        startInventoryLinkingService(ACCEPTED)
        withMovementSubmissionPersisted(false)

        val result: Future[Result] =
          movementsService.handleMovementSubmission(
            declarantEoriValue,
            declarantUcrValue,
            "Arrival",
            XML.loadString(validInventoryLinkingExportRequest.toXml)
          )

        contentAsString(result) should be("Unable to persist data something bad happened")
        result.futureValue.header.status should be(INTERNAL_SERVER_ERROR)
      }

      "Departure is not persisted" in {

        startInventoryLinkingService(ACCEPTED)
        withMovementSubmissionPersisted(false)

        val result: Future[Result] =
          movementsService.handleMovementSubmission(
            declarantEoriValue,
            declarantUcrValue,
            "Departure",
            XML.loadString(validInventoryLinkingExportRequest.toXml)
          )

        contentAsString(result) should be("Unable to persist data something bad happened")
        result.futureValue.header.status should be(INTERNAL_SERVER_ERROR)
      }

      "Arrival is not persisted (ACCEPTED but, no conversationID)" in {

        startInventoryLinkingService(ACCEPTED, conversationId = false)
        withMovementSubmissionPersisted(false)

        val result: Future[Result] =
          movementsService.handleMovementSubmission(
            declarantEoriValue,
            declarantUcrValue,
            "Arrival",
            XML.loadString(validInventoryLinkingExportRequest.toXml)
          )

        contentAsString(result) should be("No conversation Id Returned")
        result.futureValue.header.status should be(INTERNAL_SERVER_ERROR)
      }

      "Departure is not persisted (ACCEPTED but, no conversationID)" in {

        startInventoryLinkingService(ACCEPTED, conversationId = false)
        withMovementSubmissionPersisted(false)

        val result: Future[Result] =
          movementsService.handleMovementSubmission(
            declarantEoriValue,
            declarantUcrValue,
            "Departure",
            XML.loadString(validInventoryLinkingExportRequest.toXml)
          )

        contentAsString(result) should be("No conversation Id Returned")
        result.futureValue.header.status should be(INTERNAL_SERVER_ERROR)
      }

      "it is Not Accepted (BAD_REQUEST)" in {

        startInventoryLinkingService(BAD_REQUEST)
        withMovementSubmissionPersisted(false)

        val result: Future[Result] =
          movementsService.handleMovementSubmission(
            declarantEoriValue,
            declarantUcrValue,
            "Arrival",
            XML.loadString(validInventoryLinkingExportRequest.toXml)
          )

        contentAsString(result) should be("Non Accepted status returned by Customs Declaration Service")
        result.futureValue.header.status should be(INTERNAL_SERVER_ERROR)
      }

      "it is Not Accepted (NOT_FOUND)" in {

        startInventoryLinkingService(NOT_FOUND)
        withMovementSubmissionPersisted(false)

        val result: Future[Result] =
          movementsService.handleMovementSubmission(
            declarantEoriValue,
            declarantUcrValue,
            "Arrival",
            XML.loadString(validInventoryLinkingExportRequest.toXml)
          )

        contentAsString(result) should be("Non Accepted status returned by Customs Declaration Service")
        result.futureValue.header.status should be(INTERNAL_SERVER_ERROR)
      }

      "it is Not Accepted (UNAUTHORIZED)" in {

        startInventoryLinkingService(UNAUTHORIZED)
        withMovementSubmissionPersisted(false)

        val result: Future[Result] =
          movementsService.handleMovementSubmission(
            declarantEoriValue,
            declarantUcrValue,
            "Arrival",
            XML.loadString(validInventoryLinkingExportRequest.toXml)
          )

        contentAsString(result) should be("Non Accepted status returned by Customs Declaration Service")
        result.futureValue.header.status should be(INTERNAL_SERVER_ERROR)
      }

      "it is Not Accepted (INTERNAL_SERVER_ERROR)" in {

        startInventoryLinkingService(INTERNAL_SERVER_ERROR)
        withMovementSubmissionPersisted(false)

        val result: Future[Result] =
          movementsService.handleMovementSubmission(
            declarantEoriValue,
            declarantUcrValue,
            "Arrival",
            XML.loadString(validInventoryLinkingExportRequest.toXml)
          )

        contentAsString(result) should be("Non Accepted status returned by Customs Declaration Service")
        result.futureValue.header.status should be(INTERNAL_SERVER_ERROR)
      }
    }
  }
}
