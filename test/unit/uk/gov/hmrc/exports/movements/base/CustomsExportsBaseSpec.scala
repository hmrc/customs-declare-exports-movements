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

package unit.uk.gov.hmrc.exports.movements.base

import java.util.UUID

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import play.api.libs.ws.WSClient
import play.filters.csrf.{CSRFConfig, CSRFConfigProvider, CSRFFilter}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.metrics.MovementsMetrics
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import uk.gov.hmrc.exports.movements.repositories.{NotificationRepository, SubmissionRepository}
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder.buildSubmissionRepositoryMock

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait CustomsExportsBaseSpec
    extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures with AuthTestSupport with BeforeAndAfterEach {
  override lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(
        bind[AuthConnector].to(mockAuthConnector),
        bind[NotificationRepository].to(mockMovementNotificationsRepository),
        bind[SubmissionRepository].to(mockMovementsRepository),
        bind[CustomsInventoryLinkingExportsConnector].to(mockCustomsInventoryLinkingConnector),
        bind[MovementsMetrics].to(mockMetrics)
      )
      .build()
  val mockMovementNotificationsRepository: NotificationRepository = mock[NotificationRepository]
  val mockMovementsRepository: SubmissionRepository = buildSubmissionRepositoryMock
  val mockCustomsInventoryLinkingConnector: CustomsInventoryLinkingExportsConnector =
    mock[CustomsInventoryLinkingExportsConnector]
  val mockMetrics: MovementsMetrics = mock[MovementsMetrics]
  val cfg: CSRFConfig = injector.instanceOf[CSRFConfigProvider].get
  val token: String = injector.instanceOf[CSRFFilter].tokenProvider.generateToken

  override protected def afterEach(): Unit = {
    reset(mockMovementNotificationsRepository, mockCustomsInventoryLinkingConnector, mockMovementsRepository)
    super.afterEach()
  }

  def randomConversationId: String = UUID.randomUUID().toString

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def injector: Injector = app.injector

  def wsClient: WSClient = injector.instanceOf[WSClient]

  implicit val mat: Materializer = app.materializer

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  implicit lazy val patience: PatienceConfig =
    PatienceConfig(timeout = 5.seconds, interval = 50.milliseconds) // be more patient than the default

  protected def withConnectorCall(response: CustomsInventoryLinkingResponse) =
    when(mockCustomsInventoryLinkingConnector.sendInventoryLinkingRequest(any(), any())(any()))
      .thenReturn(Future.successful(response))

  protected def withMovements(movements: Seq[Submission]): OngoingStubbing[Future[Seq[Submission]]] =
    when(mockMovementsRepository.findByEori(any())).thenReturn(Future.successful(movements))

}
