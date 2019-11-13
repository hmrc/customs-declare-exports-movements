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
import play.api.mvc._
import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation
import uk.gov.hmrc.exports.movements.services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class ConsolidationController @Inject()(consolidationService: SubmissionService, cc: ControllerComponents)(
  implicit executionContext: ExecutionContext
) extends BackendController(cc) {

  def submitConsolidation(): Action[Consolidation] = Action.async(parse.json[Consolidation]) { implicit request =>
    consolidationService.submitConsolidation(request.body).map(_ => Accepted(request.body))
  }
}
