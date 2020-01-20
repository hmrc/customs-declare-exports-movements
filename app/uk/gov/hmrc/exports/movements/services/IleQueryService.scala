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

package uk.gov.hmrc.exports.movements.services

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.Status
import uk.gov.hmrc.exports.movements.connectors.CustomsInventoryLinkingExportsConnector
import uk.gov.hmrc.exports.movements.exceptions.CustomsInventoryLinkingUpstreamException
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.movements.IleQueryRequest
import uk.gov.hmrc.exports.movements.models.submissions.IleQuerySubmission
import uk.gov.hmrc.exports.movements.repositories.IleQueryRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IleQueryService @Inject()(ileMapper: ILEMapper, ileQueryRepository: IleQueryRepository, ileConnector: CustomsInventoryLinkingExportsConnector)(
  implicit ec: ExecutionContext
) {

  private val logger = Logger(this.getClass)

  def submit(ileQueryRequest: IleQueryRequest)(implicit hc: HeaderCarrier): Future[String] = {
    val requestXml = ileMapper.generateIleQuery(ileQueryRequest.ucrBlock)

    ileConnector.submit(ileQueryRequest, requestXml).flatMap {

      case CustomsInventoryLinkingResponse(Status.ACCEPTED, Some(conversationId)) =>
        val ileQuerySubmission = IleQuerySubmission(
          eori = ileQueryRequest.eori,
          providerId = ileQueryRequest.providerId,
          conversationId = conversationId,
          ucrBlock = ileQueryRequest.ucrBlock
        )

        ileQueryRepository.insert(ileQuerySubmission).map(_ => conversationId)

      case CustomsInventoryLinkingResponse(status, conversationId) =>
        logger.warn(s"ILE Query failed with conversation-id=[$conversationId] and status [$status]")
        Future.failed(
          new CustomsInventoryLinkingUpstreamException(status, conversationId, "Non Accepted status returned by Customs Inventory Linking Exports")
        )
    }
  }

}
