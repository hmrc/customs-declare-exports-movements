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

package utils.testdata

import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.Codec
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.submissions.ActionType
import uk.gov.hmrc.exports.movements.services.context.SubmissionRequestContext
import utils.testdata.CommonTestData._

import scala.xml.Elem

object ConsolidationTestData {

  val exampleShutMucrConsolidationRequestXML: Elem =
    <inventoryLinkingConsolidationRequest>
      <messageCode>{MessageCodes.CST}</messageCode>
      <masterUCR>{ucr_2}</masterUCR>
    </inventoryLinkingConsolidationRequest>

  val exampleShutMucrConsolidationRequestJson: JsValue = JsObject(
    Map(
      "inventoryLinkingConsolidationRequest" -> JsObject(
        Map("messMovementRepositorySpecageCode" -> JsString(MessageCodes.CST), "masterUCR" -> JsString(ucr_2))
      )
    )
  )

  val exampleAssociateDucrConsolidationRequestXML: Elem =
    <inventoryLinkingConsolidationRequest>
      <messageCode>{MessageCodes.EAC}</messageCode>
      <masterUCR>{ucr_2}</masterUCR>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleAssociateDucrConsolidationRequestJson: JsValue = JsObject(
    Map(
      "inventoryLinkingConsolidationRequest" -> JsObject(
        Map(
          "messageCode" -> JsString(MessageCodes.EAC),
          "masterUCR" -> JsString(ucr_2),
          "ucrBlock" -> JsObject(Map("ucr" -> JsString(ucr), "ucrType" -> JsString("D")))
        )
      )
    )
  )

  val exampleDisassociateDucrConsolidationRequestXML: Elem =
    <inventoryLinkingConsolidationRequest>
      <messageCode>{MessageCodes.EAC}</messageCode>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleDisassociateDucrConsolidationRequestJson: JsValue = JsObject(
    Map(
      "inventoryLinkingConsolidationRequest" -> JsObject(
        Map(
          "messageCode" -> JsString(MessageCodes.EAC),
          "ucrBlock" -> JsObject(Map("ucr" -> JsString(ucr), "ucrType" -> JsString("D")))
        )
      )
    )
  )

  val ValidConsolidationRequestHeaders = Map(
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    HeaderNames.AUTHORIZATION -> dummyToken,
    CustomsHeaderNames.XEoriIdentifierHeaderName -> validEori,
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${2.0}+xml"
  )

  val exampleShutMucrContext: SubmissionRequestContext = SubmissionRequestContext(
    eori = validEori,
    actionType = ActionType.ShutMucr,
    requestXml = exampleShutMucrConsolidationRequestXML
  )
}
