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
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.exports.movements.services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class SubmissionController @Inject()(submissionService: SubmissionService, cc: ControllerComponents)(implicit executionContext: ExecutionContext)
    extends BackendController(cc) {

  def getAllSubmissions(eori: Option[String], providerId: Option[String]): Action[AnyContent] = Action.async(parse.default) { implicit request =>
    submissionService
      .getSubmissions(SearchParameters(eori = eori, providerId = providerId))
      .map(movements => Ok(Json.toJson(movements)))
  }

  def getSubmission(eori: Option[String], providerId: Option[String], conversationId: String): Action[AnyContent] = Action.async(parse.default) {
    implicit request =>
      submissionService
        .getSingleSubmission(SearchParameters(eori = eori, providerId = providerId, conversationId = Some(conversationId)))
        .map(submissions => Ok(Json.toJson(submissions)))
  }

}
