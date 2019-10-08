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

package uk.gov.hmrc.exports.movements.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.movements.controllers.actions.AuthenticatedController
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.services.SubmissionService

import scala.concurrent.ExecutionContext

@Singleton
class SubmissionController @Inject()(
  authConnector: AuthConnector,
  headerValidator: HeaderValidator,
  submissionService: SubmissionService,
  cc: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends AuthenticatedController(authConnector, cc) {

  def getAllSubmissions: Action[AnyContent] = authorisedAction(parse.default) { implicit authorizedRequest =>
    submissionService
      .getSubmissionsByEori(authorizedRequest.eori.value)
      .map(movements => Ok(Json.toJson(movements)))
  }

  def getSubmission(conversationId: String): Action[AnyContent] = authorisedAction(parse.default) {
    implicit authorizedRequest =>
      submissionService
        .getSubmissionByConversationId(conversationId)
        .map(submission => Ok(Json.toJson(submission)))
  }
}
