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

package uk.gov.hmrc.exports.movements.services

import com.codahale.metrics.SharedMetricRegistries
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.Helpers._
import stubs.ExternalServicesConfig._
import stubs.{CustomsMovementsAPIConfig, CustomsMovementsAPIService}
import testdata.MovementsTestData._
import uk.gov.hmrc.exports.movements.base.IntegrationTestSpec
import uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.repositories.{GenericError, SubmissionRepository}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.{Await, Future}

class SubmissionServiceISpec
    extends IntegrationTestSpec with GuiceOneAppPerSuite with CustomsMovementsAPIService with ScalaFutures with IntegrationPatience {

  private def overrideModules: Seq[GuiceableModule] = Nil
  private val submissionRepository: SubmissionRepository = buildSubmissionRepositoryMock
  private val clock = Clock.fixed(Instant.parse(dateTimeString), ZoneOffset.UTC)

  override val fakeApplication: Application = {
    SharedMetricRegistries.clear()
    GuiceApplicationBuilder()
      .overrides(overrideModules: _*)
      .overrides(bind[SubmissionRepository].to(submissionRepository))
      .overrides(bind[Clock].to(clock))
      .configure(
        Map(
          "mongodb.uri" -> mongoDBUri,
          "microservice.services.customs-inventory-linking-exports.host" -> Host,
          "microservice.services.customs-inventory-linking-exports.port" -> Port,
          "microservice.services.customs-inventory-linking-exports.sendArrival" -> CustomsMovementsAPIConfig.submitMovementServiceContext,
          "microservice.services.customs-inventory-linking-exports.client-id.customs-movements-frontend" -> CustomsMovementsAPIConfig.clientId
        )
      )
      .build()
  }

  private lazy val movementsService = app.injector.instanceOf[SubmissionService]

  private implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq("user-agent" -> "customs-movements-frontend"))

  def withMovementSubmissionPersisted(result: Boolean): Unit =
    when(submissionRepository.insertOne(any())).thenReturn(
      if (result) Future.successful(Right(exampleSubmission()))
      else Future.successful(Left(GenericError("Some error")))
    )

  "Movements Service" should {

    "do not save movement submission in DB" when {

      "Arrival is not persisted (ACCEPTED but, no conversationID)" in {
        startInventoryLinkingService(ACCEPTED, conversationId = false)
        withMovementSubmissionPersisted(false)

        the[CustomsInventoryLinkingUpstreamException] thrownBy {
          Await.result(movementsService.submit(exampleArrivalRequest), patienceConfig.timeout)
        } should have message "Status: 202. ConverstationId: Not preset . Non Accepted status returned by Customs Inventory Linking Exports"
      }

      "Departure is not persisted (ACCEPTED but, no conversationID)" in {
        startInventoryLinkingService(ACCEPTED, conversationId = false)
        withMovementSubmissionPersisted(false)

        the[CustomsInventoryLinkingUpstreamException] thrownBy {
          Await.result(movementsService.submit(exampleDepartureRequest), patienceConfig.timeout)
        } should have message "Status: 202. ConverstationId: Not preset . Non Accepted status returned by Customs Inventory Linking Exports"
      }

      "it is Not Accepted (BAD_REQUEST)" in {
        startInventoryLinkingService(BAD_REQUEST)
        withMovementSubmissionPersisted(false)

        val result = the[CustomsInventoryLinkingUpstreamException] thrownBy {
          Await.result(movementsService.submit(exampleArrivalRequest), patienceConfig.timeout)
        }
        result.getMessage should fullyMatch regex "Status: 400. ConverstationId: '.*' . Non Accepted status returned by Customs Inventory Linking Exports"
      }

      "it is Not Accepted (NOT_FOUND)" in {
        startInventoryLinkingService(NOT_FOUND)
        withMovementSubmissionPersisted(false)

        a[CustomsInventoryLinkingUpstreamException] mustBe thrownBy {
          Await.result(movementsService.submit(exampleArrivalRequest), patienceConfig.timeout)
        }
      }

      "it is Not Accepted (UNAUTHORIZED)" in {
        startInventoryLinkingService(UNAUTHORIZED)
        withMovementSubmissionPersisted(false)

        a[CustomsInventoryLinkingUpstreamException] mustBe thrownBy {
          Await.result(movementsService.submit(exampleArrivalRequest), patienceConfig.timeout)
        }
      }

      "it is Not Accepted (INTERNAL_SERVER_ERROR)" in {
        startInventoryLinkingService(INTERNAL_SERVER_ERROR)
        withMovementSubmissionPersisted(false)

        a[CustomsInventoryLinkingUpstreamException] mustBe thrownBy {
          Await.result(movementsService.submit(exampleArrivalRequest), patienceConfig.timeout)
        }
      }
    }
  }
}
