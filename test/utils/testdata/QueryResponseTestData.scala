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

package utils.testdata

import uk.gov.hmrc.exports.movements.models.notifications.queries.{QueryResponse, QueryResponseData}
import utils.testdata.CommonTestData.{conversationId, conversationId_2}

object QueryResponseTestData {

  private val payloadExemplaryLength = 10
  private val payload_1 = TestDataHelper.randomAlphanumericString(payloadExemplaryLength)
  private val payload_2 = TestDataHelper.randomAlphanumericString(payloadExemplaryLength)

  val queryResponse_1: QueryResponse = QueryResponse(conversationId = conversationId, payload = payload_1, data = QueryResponseData())
  val queryResponse_2: QueryResponse = QueryResponse(conversationId = conversationId_2, payload = payload_2, data = QueryResponseData())

}
