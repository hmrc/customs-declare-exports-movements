/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.exports.movements.models.movements.IleQueryRequest
import uk.gov.hmrc.exports.movements.repositories.SearchParameters
import uk.gov.hmrc.exports.movements.services.IleQueryService
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class IleQueryController @Inject()(ileQueryService: IleQueryService, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def submitIleQuery(): Action[IleQueryRequest] = Action.async(parse.json[IleQueryRequest]) { implicit request =>
    ileQueryService.submit(request.body).map(Accepted(_))
  }

  def getIleQueryResponses(eori: Option[String], providerId: Option[String], conversationId: String): Action[AnyContent] = Action.async {
    implicit request =>
      val searchParameters = SearchParameters(eori = eori, providerId = providerId, conversationId = Some(conversationId))

      ileQueryService.fetchResponses(searchParameters).map {
        case Right(ileQueryResponses) => Ok(Json.toJson(ileQueryResponses))
        case Left(_)                  => GatewayTimeout
      }
  }
}
