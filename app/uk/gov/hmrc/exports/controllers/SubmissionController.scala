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

package uk.gov.hmrc.exports.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BodyParsers, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.config.AppConfig
import uk.gov.hmrc.exports.models._
import uk.gov.hmrc.exports.repositories.MovementsRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SubmissionController @Inject()(
  appConfig: AppConfig,
  movementsRepository: MovementsRepository,
  authConnector: AuthConnector
) extends ExportController(authConnector) {

  def saveMovementSubmission(): Action[MovementResponse] =
    authorisedAction(parse.json[MovementResponse]) { implicit request =>
      processSave
    }

  private def processSave()(
    implicit request: AuthorizedSubmissionRequest[MovementResponse],
    hc: HeaderCarrier
  ): Future[Result] = {
    val body = request.body
    movementsRepository
      .save(MovementSubmissions(request.eori.value, body.conversationId, body.ducr, body.mucr, body.movementType))
      .map(
        res =>
          if (res) {
            Logger.debug("movement submission data saved to DB")
            Ok(Json.toJson(ExportsResponse(OK, "Movement Submission saved")))
          } else {
            Logger.error("error  saving movement submission data to DB")
            InternalServerError("failed saving movement submission")
          }
      )
  }

  def getMovements: Action[AnyContent] =
    authorisedAction(BodyParsers.parse.default) { implicit authorizedRequest =>
      movementsRepository.findByEori(authorizedRequest.eori.value).map { movements =>
        Ok(Json.toJson(movements))
      }
    }
}
