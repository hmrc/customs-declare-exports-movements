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

package utils

import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.Codec
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.consolidations.ConsolidationSubmission
import utils.MovementsTestData._

import scala.xml.Elem

object ConsolidationTestData {

  val consolidationSubmission =
    ConsolidationSubmission(eori = validEori, conversationId = conversationId, ucr = randomUcr)

  val exampleShutMucrConsolidationRequest: Elem =
    <inventoryLinkingConsolidationRequest>
      <messageCode>CST</messageCode>
      <masterUCR>5GB123456789000-123ABC456DEFIIIII</masterUCR>
      <ucrBlock>
        <ucr>4GB123456789000-123ABC456DEFIIIII</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleShutMucrConsolidationRequestJson: JsValue = JsObject(
    Map(
      "inventoryLinkingConsolidationRequest" -> JsObject(
        Map(
          "messageCode" -> JsString("CST"),
          "masterUCR" -> JsString("5GB123456789000-123ABC456DEFIIIII"),
          "ucrBlock" -> JsObject(
            Map("ucr" -> JsString("4GB123456789000-123ABC456DEFIIIII"), "ucrType" -> JsString("D"))
          )
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

}