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

import com.codahale.metrics.Timer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import reactivemongo.api.commands.{DefaultWriteResult, WriteResult}
import reactivemongo.core.errors.GenericDatabaseException
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.metrics.MovementsMetrics
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.notifications.parsers.{
  ResponseParser,
  ResponseParserContext,
  ResponseParserFactory
}
import uk.gov.hmrc.exports.movements.models.notifications.{Notification, NotificationData, NotificationFactory}
import uk.gov.hmrc.exports.movements.models.submissions.SubmissionFactory
import uk.gov.hmrc.exports.movements.repositories.{NotificationRepository, SubmissionRepository}
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import uk.gov.hmrc.exports.movements.services.{NotificationService, SubmissionService}
import utils.MovementsTestData.emptySubmission

import scala.concurrent.Future
import scala.xml.NodeSeq

object UnitTestMockBuilder extends MockitoSugar {

  val dummyWriteResultSuccess: WriteResult =
    DefaultWriteResult(ok = true, n = 1, writeErrors = Seq.empty, writeConcernError = None, code = None, errmsg = None)

  def buildNotificationRepositoryMock: NotificationRepository = {
    val notificationRepositoryMock = mock[NotificationRepository]

    when(notificationRepositoryMock.insert(any[Notification])(any()))
      .thenReturn(Future.failed(GenericDatabaseException("ERROR", None)))
    when(notificationRepositoryMock.findNotificationsByConversationId(any[String]))
      .thenReturn(Future.successful(Seq.empty))

    notificationRepositoryMock
  }

  def buildSubmissionRepositoryMock: SubmissionRepository = {
    val submissionRepositoryMock = mock[SubmissionRepository]

    when(submissionRepositoryMock.findByEori(any())).thenReturn(Future.successful(Seq.empty))
    when(submissionRepositoryMock.insert(any())(any()))
      .thenReturn(Future.failed(GenericDatabaseException("ERROR", None)))

    submissionRepositoryMock
  }

  def buildNotificationServiceMock: NotificationService = {
    val notificationServiceMock = mock[NotificationService]

    when(notificationServiceMock.save(any[Notification])).thenReturn(Future.successful(Left("")))
    when(notificationServiceMock.getAllNotifications(any())).thenReturn(Future.successful(Seq.empty))

    notificationServiceMock
  }

  def buildSubmissionServiceMock: SubmissionService = {
    val submissionServiceMock = mock[SubmissionService]

    when(submissionServiceMock.submitRequest(any())(any()))
      .thenReturn(Future.successful(Left("")))

    submissionServiceMock
  }

  def buildMovementNotificationFactoryMock: NotificationFactory = {
    val movementNotificationFactoryMock = mock[NotificationFactory]

    when(movementNotificationFactoryMock.buildMovementNotification(any[String], any[NodeSeq]))
      .thenReturn(Notification.empty)

    movementNotificationFactoryMock
  }

  def buildSubmissionFactoryMock: SubmissionFactory = {
    val submissionFactory = mock[SubmissionFactory]

    when(submissionFactory.buildMovementSubmission(any[String], any[SubmissionRequestContext]))
      .thenReturn(emptySubmission)

    submissionFactory
  }

  def buildCustomsInventoryLinkingExportsConnectorMock: CustomsInventoryLinkingExportsConnector = {
    val customsInventoryLinkingExportsConnectorMock = mock[CustomsInventoryLinkingExportsConnector]

    when(customsInventoryLinkingExportsConnectorMock.sendInventoryLinkingRequest(any(), any())(any()))
      .thenReturn(Future.successful(CustomsInventoryLinkingResponse.empty))

    customsInventoryLinkingExportsConnectorMock
  }

  def buildMovementsMetricsMock: MovementsMetrics = {
    val movementsMetricsMock = mock[MovementsMetrics]

    when(movementsMetricsMock.startTimer(any())).thenReturn(new Timer().time())

    movementsMetricsMock
  }

  def buildResponseParserFactoryMock: ResponseParserFactory = {
    val responseParserFactoryMock = mock[ResponseParserFactory]

    val responseParserMock = buildResponseParserMock
    when(responseParserFactoryMock.buildResponseParser(any())).thenReturn(responseParserMock)
    val responseParserContext = ResponseParserContext("", responseParserMock)
    when(responseParserFactoryMock.buildResponseParserContext(any())).thenReturn(responseParserContext)

    responseParserFactoryMock
  }

  def buildResponseParserMock: ResponseParser = {
    val responseParserMock = mock[ResponseParser]

    when(responseParserMock.parse(any())).thenReturn(NotificationData.empty)

    responseParserMock
  }
}
