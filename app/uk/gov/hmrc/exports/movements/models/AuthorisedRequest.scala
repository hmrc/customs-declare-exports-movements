/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Request, WrappedRequest}

trait HasEori {
  val eori: Eori
}

trait HasConversationId {
  val conversationId: ConversationId
}

case class Eori(value: String) extends AnyVal
object Eori {
  implicit val format: OFormat[Eori] = Json.format[Eori]
}

case class ConversationId(value: String) extends AnyVal

case class AuthorizedSubmissionRequest[A](eori: Eori, request: Request[A]) extends WrappedRequest[A](request) with HasEori

case class ValidatedHeadersRequest(ducr: String, movementType: String)

case class NotificationApiRequestHeaders(conversationId: ConversationId) extends HasConversationId
