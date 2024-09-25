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

package uk.gov.hmrc.exports.movements.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.exports.movements.models.movements.MovementsRequest
import uk.gov.hmrc.exports.movements.services.SubmissionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class MovementsController @Inject() (submissionService: SubmissionService, override val controllerComponents: ControllerComponents)(
  implicit executionContext: ExecutionContext
) extends BackendController(controllerComponents) {

  val createMovement: Action[MovementsRequest] = Action.async(parse.json[MovementsRequest]) { implicit request =>
    submissionService.submit(request.body).map(Accepted(_))
  }
}
