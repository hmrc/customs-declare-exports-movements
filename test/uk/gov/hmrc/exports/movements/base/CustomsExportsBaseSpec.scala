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

package uk.gov.hmrc.exports.movements.base

import java.util.UUID

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
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
import uk.gov.hmrc.exports.movements.metrics.ExportsMetrics
import uk.gov.hmrc.exports.movements.models.{CustomsInventoryLinkingResponse, MovementSubmissions}
import uk.gov.hmrc.exports.movements.repositories.{MovementNotificationsRepository, MovementsRepository}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait CustomsExportsBaseSpec
    extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures with AuthTestSupport {
  override lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(
        bind[AuthConnector].to(mockAuthConnector),
        bind[MovementNotificationsRepository].to(mockMovementNotificationsRepository),
        bind[MovementsRepository].to(mockMovementsRepository),
        bind[CustomsInventoryLinkingExportsConnector].to(mockCustomsInventoryLinkingConnector),
        bind[ExportsMetrics].to(mockMetrics)
      )
      .build()
  val mockMovementNotificationsRepository: MovementNotificationsRepository = mock[MovementNotificationsRepository]
  val mockMovementsRepository: MovementsRepository = mock[MovementsRepository]
  val mockCustomsInventoryLinkingConnector: CustomsInventoryLinkingExportsConnector = mock[CustomsInventoryLinkingExportsConnector]
  val mockMetrics: ExportsMetrics = mock[ExportsMetrics]
  val cfg: CSRFConfig = injector.instanceOf[CSRFConfigProvider].get
  val token: String = injector.instanceOf[CSRFFilter].tokenProvider.generateToken

  def randomConversationId: String = UUID.randomUUID().toString

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def injector: Injector = app.injector

  def wsClient: WSClient = injector.instanceOf[WSClient]

  implicit val mat: Materializer = app.materializer

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  implicit lazy val patience: PatienceConfig =
    PatienceConfig(timeout = 5.seconds, interval = 50.milliseconds) // be more patient than the default

  protected def withConnectorCall(response: CustomsInventoryLinkingResponse) ={
    when(mockCustomsInventoryLinkingConnector.sendMovementRequest(any(), any())(any(), any()))
      .thenReturn(Future.successful(response))
  }

  protected def withDataSaved(ok: Boolean): OngoingStubbing[Future[Boolean]] =
    when(mockMovementsRepository.save(any())).thenReturn(Future.successful(ok))

  protected def withMovements(movements: Seq[MovementSubmissions]): OngoingStubbing[Future[Seq[MovementSubmissions]]] =
    when(mockMovementsRepository.findByEori(any())).thenReturn(Future.successful(movements))

  protected def withMovementNotificationSaved(ok: Boolean): OngoingStubbing[Future[Boolean]] =
    when(mockMovementNotificationsRepository.save(any())).thenReturn(Future.successful(ok))

}
