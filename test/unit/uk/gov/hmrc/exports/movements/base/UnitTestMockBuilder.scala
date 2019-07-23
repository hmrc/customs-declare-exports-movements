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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import reactivemongo.api.commands.{DefaultWriteResult, WriteResult}
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.models.{CustomsInventoryLinkingResponse, Eori}
import uk.gov.hmrc.exports.movements.models.notifications.{MovementNotification, MovementNotificationFactory}
import uk.gov.hmrc.exports.movements.repositories.{
  ConsolidationRepository,
  MovementSubmissionRepository,
  NotificationRepository
}
import uk.gov.hmrc.exports.movements.services.{ConsolidationService, NotificationService}

import scala.concurrent.Future
import scala.xml.NodeSeq

object UnitTestMockBuilder extends MockitoSugar {

  val dummyWriteResultSuccess: WriteResult =
    DefaultWriteResult(ok = true, n = 1, writeErrors = Seq.empty, writeConcernError = None, code = None, errmsg = None)
  val dummyWriteResultFailure: WriteResult =
    DefaultWriteResult(ok = false, n = 0, writeErrors = Seq.empty, writeConcernError = None, code = None, errmsg = None)

  def buildNotificationRepositoryMock: NotificationRepository = {
    val notificationRepositoryMock = mock[NotificationRepository]

    when(notificationRepositoryMock.insert(any[MovementNotification])(any()))
      .thenReturn(Future.successful(dummyWriteResultFailure))
    when(notificationRepositoryMock.findNotificationsByConversationId(any[String]))
      .thenReturn(Future.successful(Seq.empty))

    notificationRepositoryMock
  }

  def buildConsolidationRepositoryMock: ConsolidationRepository = {
    val consolidationRepositoryMock = mock[ConsolidationRepository]

    when(consolidationRepositoryMock.insert(any())(any())).thenReturn(Future.successful(dummyWriteResultFailure))

    consolidationRepositoryMock
  }

  def buildNotificationServiceMock: NotificationService = {
    val notificationServiceMock = mock[NotificationService]

    when(notificationServiceMock.save(any[MovementNotification])).thenReturn(Future.successful(Left("")))

    notificationServiceMock
  }

  def buildConsolidationServiceMock: ConsolidationService = {
    val consolidationServiceMock = mock[ConsolidationService]

    when(consolidationServiceMock.submitConsolidationRequest(any(), any())(any()))
      .thenReturn(Future.successful(Left("")))

    consolidationServiceMock
  }

  def buildMovementNotificationFactoryMock: MovementNotificationFactory = {
    val movementNotificationFactoryMock = mock[MovementNotificationFactory]

    when(movementNotificationFactoryMock.buildMovementNotification(any[String], any[NodeSeq]))
      .thenReturn(MovementNotification.empty)

    movementNotificationFactoryMock
  }

  def buildCustomsInventoryLinkingExportsConnectorMock: CustomsInventoryLinkingExportsConnector = {
    val customsInventoryLinkingExportsConnectorMock = mock[CustomsInventoryLinkingExportsConnector]

    when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
      .thenReturn(Future.successful(CustomsInventoryLinkingResponse.empty))

    customsInventoryLinkingExportsConnectorMock
  }
}
