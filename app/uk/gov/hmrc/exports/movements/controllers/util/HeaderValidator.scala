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

package uk.gov.hmrc.exports.movements.controllers.util

import javax.inject.Singleton
import play.api.Logger
import play.api.http.HeaderNames
import uk.gov.hmrc.exports.movements.models._

@Singleton
class HeaderValidator {

  def extractLrnHeader(headers: Map[String, String]): Option[String] =
    extractHeader(CustomsHeaderNames.XLrnHeaderName, headers)

  def extractUcrHeader(headers: Map[String, String]): Option[String] =
    extractHeader(CustomsHeaderNames.XUcrHeaderName, headers)

  def extractMovementTypeHeader(headers: Map[String, String]): Option[String] =
    extractHeader(CustomsHeaderNames.XMovementTypeHeaderName, headers)

  def extractAuthTokenHeader(headers: Map[String, String]): Option[String] =
    extractHeader(HeaderNames.AUTHORIZATION, headers)

  def extractConversationIdHeader(headers: Map[String, String]): Option[String] =
    extractHeader(CustomsHeaderNames.XConversationIdName, headers)

  def extractEoriHeader(headers: Map[String, String]): Option[String] =
    extractHeader(CustomsHeaderNames.XEoriIdentifierHeaderName, headers)

  private def extractHeader(headerName: String, headers: Map[String, String]): Option[String] =
    headers.get(headerName) match {
      case Some(header) if !header.isEmpty => Some(header)
      case _ =>
        Logger.error(s"Error Extracting $headerName")
        None
    }

  def validateAndExtractMovementSubmissionHeaders(
    implicit headers: Map[String, String]
  ): Either[ErrorResponse, ValidatedHeadersMovementsRequest] = {
    val result = for {
      ucr <- extractUcrHeader(headers)
      movementType <- extractMovementTypeHeader(headers)
    } yield ValidatedHeadersMovementsRequest(ucr, movementType)
    result match {
      case Some(request) => Right(request)
      case None =>
        Logger.debug("Error validating and extracting headers")
        Left(ErrorResponse.ErrorInvalidPayload)
    }
  }

  def validateAndExtractMovementNotificationHeaders(
    headers: Map[String, String]
  ): Either[ErrorResponse, MovementNotificationApiRequestHeaders] = {
    val result = for {
      eori <- extractEoriHeader(headers)
      authToken <- extractAuthTokenHeader(headers)
      conversationId <- extractConversationIdHeader(headers)
    } yield MovementNotificationApiRequestHeaders(AuthToken(authToken), ConversationId(conversationId), Eori(eori))
    result match {
      case Some(request) => Right(request)
      case _ =>
        Logger.debug("Error validating and extracting movement notification headers")
        Left(ErrorResponse.ErrorInvalidPayload)
    }
  }
}
