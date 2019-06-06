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

package component.uk.gov.hmrc.exports.movements.base

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyZeroInteractions, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.exports.movements.models.{MovementNotification, MovementSubmissions}
import uk.gov.hmrc.exports.movements.repositories.{MovementNotificationsRepository, MovementsRepository}
import unit.uk.gov.hmrc.exports.movements.controllers.NotificationTestData
import utils.ExternalServicesConfig.{Host, Port}
import utils.stubs.CustomsMovementsAPIService
import utils.{AuthService, CustomsMovementsAPIConfig, MovementsTestData}

import scala.concurrent.Future

trait ComponentTestSpec
    extends FeatureSpec with GivenWhenThen with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach
    with Eventually with MockitoSugar with Matchers with MovementsTestData with NotificationTestData with OptionValues
    with AuthService with CustomsMovementsAPIService {

  private val mockMovementNotificationsRepository = mock[MovementNotificationsRepository]
  private val mockMovementSubmissionsRepository = mock[MovementsRepository]

  override protected def beforeAll() {

    startMockServer()
  }

  override protected def beforeEach() {

    reset(mockMovementNotificationsRepository)
    reset(mockMovementSubmissionsRepository)
    resetMockServer()
  }

  override protected def afterAll() {

    stopMockServer()
  }

  // movements submission
  def withMovementSubmissionRepository(saveResponse: Boolean): OngoingStubbing[Future[Boolean]] =
    when(mockMovementSubmissionsRepository.save(any())).thenReturn(Future.successful(saveResponse))

  def verifyMovementSubmissionRepositoryIsCorrectlyCalled(eoriValue: String) {
    val submissionCaptor: ArgumentCaptor[MovementSubmissions] = ArgumentCaptor.forClass(classOf[MovementSubmissions])
    verify(mockMovementSubmissionsRepository).save(submissionCaptor.capture())
    submissionCaptor.getValue.eori shouldBe eoriValue
  }

  def verifyMovementSubmissionRepositoryWasNotCalled(): Unit =
    verifyZeroInteractions(mockMovementSubmissionsRepository)

  // movement notifications
  def withMovementNotificationRepository(saveResponse: Boolean): OngoingStubbing[Future[Boolean]] =
    when(mockMovementNotificationsRepository.save(any())).thenReturn(Future.successful(saveResponse))

  def verifyMovementNotificationRepositoryIsCorrectlyCalled(eoriValue: String) {
    val notificationCaptor: ArgumentCaptor[MovementNotification] =
      ArgumentCaptor.forClass(classOf[MovementNotification])
    verify(mockMovementNotificationsRepository).save(notificationCaptor.capture())
    notificationCaptor.getValue.eori shouldBe eoriValue
  }

  def verifyMovementNotificationRepositoryWasNotCalled(): Unit =
    verifyZeroInteractions(mockMovementNotificationsRepository)

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(bind[MovementsRepository].toInstance(mockMovementSubmissionsRepository))
      .overrides(bind[MovementNotificationsRepository].toInstance(mockMovementNotificationsRepository))
      .configure(
        Map(
          "microservice.services.auth.host" -> Host,
          "microservice.services.auth.port" -> Port,
          "microservice.services.customs-inventory-linking-exports.host" -> Host,
          "microservice.services.customs-inventory-linking-exports.port" -> Port,
          "microservice.services.customs-inventory-linking-exports.sendArrival" -> CustomsMovementsAPIConfig.submitMovementServiceContext,
          "microservice.services.customs-inventory-linking-exports.client-id" -> CustomsMovementsAPIConfig.clientId
        )
      )
      .build()
}
