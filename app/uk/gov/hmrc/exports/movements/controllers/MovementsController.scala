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

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import uk.gov.hmrc.exports.movements.controllers.actions.Authenticator
import uk.gov.hmrc.exports.movements.controllers.request.MovementRequest
import uk.gov.hmrc.exports.movements.models.movements.MovementDeclaration.REST.format
import uk.gov.hmrc.exports.movements.services.MovementsService

import scala.concurrent.ExecutionContext

@Singleton
class MovementsController @Inject()(
  movementsService: MovementsService,
  authenticator: Authenticator,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends RESTController(controllerComponents) {

  private val logger = Logger(this.getClass)

  def submitMovement(): Action[MovementRequest] =
    authenticator.authorisedAction(parsingJson[MovementRequest]) { implicit request =>
      logPayload("Create Declaration Request Received", request.body)
      movementsService
        .create(request.body.toMovementsDeclaration(id = UUID.randomUUID().toString, eori = request.eori))
        .map(logPayload("Create Movements Response", _))
        .map(declaration => Created(declaration))
    }

  private def logPayload[T](prefix: String, payload: T)(implicit wts: Writes[T]): T = {
    logger.debug(s"Request Received: ${Json.toJson(payload)}")
    payload
  }
}
