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

package testdata

import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.Codec
import testdata.CommonTestData.{ucr, _}
import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation._

import scala.xml.{Elem, Node, NodeSeq}

object ConsolidationTestData {

  val associateDucrRequest = AssociateDucrRequest(eori = validEori, providerId = Some(validProviderId), mucr = ucr, ucr = ucr_2)
  val disassiociateDucrRequest = DisassociateDucrRequest(eori = validEori, providerId = Some(validProviderId), ucr = ucr_2)
  val shutMucrRequest = ShutMucrRequest(eori = validEori, providerId = Some(validProviderId), mucr = ucr)

  def buildUcrBlockNode(ucr: String, ucrType: String): NodeSeq =
    <ucrBlock>
      <ucr>{ucr}</ucr>
      <ucrType>{ucrType}</ucrType>
    </ucrBlock>

  def buildUcrBlockNode(ucr: String, ucrType: String, ucrPartNo: String): NodeSeq =
    <ucrBlock>
      <ucr>{ucr}</ucr>
      <ucrPartNo>{ucrPartNo}</ucrPartNo>
      <ucrType>{ucrType}</ucrType>
    </ucrBlock>

  val exampleShutMucrConsolidationRequestXML: Node =
    <inventoryLinkingConsolidationRequest>
      <messageCode>{MessageCodes.CST}</messageCode>
      <masterUCR>{ucr_2}</masterUCR>
    </inventoryLinkingConsolidationRequest>

  val exampleAssociateDucrConsolidationRequestXML: Elem =
    <inventoryLinkingConsolidationRequest>
      <messageCode>{MessageCodes.EAC}</messageCode>
      <masterUCR>{ucr_2}</masterUCR>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleAssociateMucrConsolidationRequestXML: Elem =
    <inventoryLinkingConsolidationRequest>
      <messageCode>{MessageCodes.EAC}</messageCode>
      <masterUCR>{ucr_2}</masterUCR>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>M</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleAssociateDucrPartConsolidationRequestXML: Elem =
    <inventoryLinkingConsolidationRequest>
      <messageCode>{MessageCodes.EAC}</messageCode>
      <masterUCR>{ucr_2}</masterUCR>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrPartNo>{validUcrPartNo}</ucrPartNo>
        <ucrType>D</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleDisassociateDucrConsolidationRequestXML: Elem =
    <inventoryLinkingConsolidationRequest>
      <messageCode>{MessageCodes.EAC}</messageCode>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleDisassociateMucrConsolidationRequestXML: Elem =
    <inventoryLinkingConsolidationRequest>
      <messageCode>{MessageCodes.EAC}</messageCode>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>M</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleDisassociateDucrPartConsolidationRequestXML: Elem =
    <inventoryLinkingConsolidationRequest>
      <messageCode>{MessageCodes.EAC}</messageCode>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrPartNo>{validUcrPartNo}</ucrPartNo>
        <ucrType>D</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleIleQueryRequestXml: Elem =
    <inventoryLinkingQueryRequest>
      <queryUCR>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </queryUCR>
    </inventoryLinkingQueryRequest>

  val exampleDisassociateDucrConsolidationRequestJson: JsValue = JsObject(
    Map(
      "inventoryLinkingConsolidationRequest" -> JsObject(
        Map("messageCode" -> JsString(MessageCodes.EAC), "ucrBlock" -> JsObject(Map("ucr" -> JsString(ucr), "ucrType" -> JsString("D"))))
      )
    )
  )

  val ValidConsolidationRequestHeaders = Map(
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    HeaderNames.AUTHORIZATION -> dummyToken,
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${2.0}+xml"
  )
}
