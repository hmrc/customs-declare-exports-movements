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

import uk.gov.hmrc.exports.movements.models.notifications.UcrBlock
import uk.gov.hmrc.exports.movements.models.submissions.IleQuerySubmission
import utils.testdata.CommonTestData._
import utils.testdata.IleQueryResponseTestData._

import scala.xml.Elem

object IleQuerySubmissionTestData {

  val ileQuerySubmission_1 = IleQuerySubmission(
    eori = validEori,
    providerId = Some(validProviderId),
    conversationId = conversationId,
    ucrBlock = UcrBlock(ucr = ucr, ucrType = "D"),
    responses = Seq(ileQueryResponse_1)
  )

  val ileQuerySubmission_2 = IleQuerySubmission(
    eori = validEori_2,
    providerId = Some(validProviderId_2),
    conversationId = conversationId_2,
    ucrBlock = UcrBlock(ucr = ucr_2, ucrType = "D"),
    responses = Seq(ileQueryResponse_2)
  )

  def ileQueryXml(ucrBlock: UcrBlock): Elem =
    <inventoryLinkingQueryRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <queryUCR>
        <ucr>{ucrBlock.ucr}</ucr>
        <ucrType>{ucrBlock.ucrType}</ucrType>
      </queryUCR>
    </inventoryLinkingQueryRequest>

}
