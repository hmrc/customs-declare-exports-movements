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

package uk.gov.hmrc.exports.movements.models

import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

import scala.xml.Elem

case class CustomsInventoryLinkingResponse(status: Int, conversationId: Option[String])

case class SignedInUser(
  credentials: Credentials,
  name: Name,
  email: Option[String],
  eori: String,
  externalId: String,
  internalId: Option[String],
  affinityGroup: Option[AffinityGroup],
  enrolments: Enrolments
)

case class NotifyResponse(code: String, message: String) {
  def toXml(): Elem = <errorResponse>
    <code>
      {code}
    </code> <message>
      {message}
    </message>
  </errorResponse>
}

object NotAcceptableResponse extends NotifyResponse("ACCEPT_HEADER_INVALID", "Missing or invalid Accept header")

object HeaderMissingErrorResponse
    extends NotifyResponse(
      "INTERNAL_SERVER_ERROR",
      "ClientId or ConversationId or EORI is missing in the request headers"
    )

object NotificationFailedErrorResponse extends NotifyResponse("INTERNAL_SERVER_ERROR", "Failed to save notifications")

case class ExportsResponse(status: Int, message: String)

object ExportsResponse {
  implicit val formats = Json.format[ExportsResponse]
}
