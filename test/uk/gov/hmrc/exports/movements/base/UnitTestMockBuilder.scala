/*
 * Copyright 2024 HM Revenue & Customs
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

import com.codahale.metrics.Timer
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.metrics.MovementsMetrics
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.notifications._
import uk.gov.hmrc.exports.movements.models.notifications.parsers.{ResponseParser, ResponseParserProvider}
import uk.gov.hmrc.exports.movements.models.notifications.standard.StandardNotificationData
import uk.gov.hmrc.exports.movements.repositories.{GenericError, SubmissionRepository}

import scala.concurrent.Future
import scala.xml.NodeSeq

object UnitTestMockBuilder {

  def buildSubmissionRepositoryMock: SubmissionRepository = {
    val submissionRepositoryMock = mock[SubmissionRepository]

    when(submissionRepositoryMock.findAll(any())).thenReturn(Future.successful(Seq.empty))
    when(submissionRepositoryMock.insertOne(any()))
      .thenReturn(Future.successful(Left(GenericError("ERROR"))))

    submissionRepositoryMock
  }

  def buildCustomsInventoryLinkingExportsConnectorMock: CustomsInventoryLinkingExportsConnector = {
    val customsInventoryLinkingExportsConnectorMock = mock[CustomsInventoryLinkingExportsConnector]

    when(customsInventoryLinkingExportsConnectorMock.submit(any(), any())(any()))
      .thenReturn(Future.successful(CustomsInventoryLinkingResponse.empty))

    customsInventoryLinkingExportsConnectorMock
  }

  def buildMovementsMetricsMock: MovementsMetrics = {
    val movementsMetricsMock = mock[MovementsMetrics]

    when(movementsMetricsMock.startTimer(any())).thenReturn(new Timer().time())

    movementsMetricsMock
  }

  def buildResponseParserProviderMock: ResponseParserProvider = {
    val responseParserFactoryMock = mock[ResponseParserProvider]

    val responseParserMock: ResponseParser[NotificationData] = buildResponseParserMock(StandardNotificationData(responseType = "TestResponse"))
    when(responseParserFactoryMock.provideResponseParser(any())).thenReturn(responseParserMock)

    responseParserFactoryMock
  }

  def buildResponseParserMock[T](returnValue: T, responseTypeIle: String = ""): ResponseParser[T] = {
    val responseParserMock = mock[ResponseParser[T]]

    when(responseParserMock.responseTypeIle).thenReturn(responseTypeIle)
    when(responseParserMock.parse(any())).thenReturn(returnValue)

    responseParserMock
  }

  def buildResponseValidatorMock: ResponseValidator = {
    val responseValidatorMock = mock[ResponseValidator]

    when(responseValidatorMock.validate(any[NodeSeq])).thenReturn(Left(XmlValidationException()))

    responseValidatorMock
  }
}
