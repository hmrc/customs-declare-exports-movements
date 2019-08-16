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
import play.api.mvc.Codec
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.notifications._
import utils.testdata.CommonTestData._

import scala.xml.{Elem, Node, TopScope, Utility}

object NotificationTestData {

  val dummyAuthToken: String =
    "Bearer BXQ3/Treo4kQCZvVcCqKPlwxRN4RA9Mb5RF8fFxOuwG5WSg+S+Rsp9Nq998Fgg0HeNLXL7NGwEAIzwM6vuA6YYhRQnTRFa" +
      "Bhrp+1w+kVW8g1qHGLYO48QPWuxdM87VMCZqxnCuDoNxVn76vwfgtpNj0+NwfzXV2Zc12L2QGgF9H9KwIkeIPK/mMlBESjue4V]"

  val movementUri = "/customs-declare-exports/notifyMovement"

  val crcCode_success = "000"
  val crcCode_prelodgedDeclarationNotArrived = "101"
  val crcCode_declarationNotArrived = "102"

  val actionCode_acknowledgedAndProcessed = "1"
  val actionCode_partiallyProcessed = "2"
  val actionCode_rejected = "3"

  val errorCode_1 = "21"
  val errorCode_2 = "13"
  val errorCode_3 = "47"
  val errorCodeDescriptive = "error in CDS ILE processing"
  val goodsLocation = "Location"
  val submitRole = "SubmitRoleBeing35CharactersInLength"
  val movementReference = "MovRef001234"
  val declarationCount = 123
  val commodityCode_1 = 12345678
  val commodityCode_2 = 11122233
  val totalPackages_1 = 456
  val totalPackages_2 = 13
  val totalNetMass_1 = "123456789012.3456"
  val totalNetMass_2 = "123.45"

  def clearNamespaces(xml: Node): Node = xml match {
    case e:Elem => e.copy(scope = TopScope, child = e.child.map(clearNamespaces))
    case o => o
  }

  val exampleRejectInventoryLinkingControlResponseNotification: Notification = Notification(
    conversationId = conversationId,
    responseType = "inventoryLinkingControlResponse",
    payload = Utility.trim(exampleRejectInventoryLinkingControlResponseXML).toString(),
    data = NotificationData(
      messageCode = Some(MessageCodes.CST),
      actionCode = Some(actionCode_rejected),
      movementReference = Some(movementReference),
      entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "M")))),
      errorCode = Seq(errorCode_1, errorCodeDescriptive)
    )
  )

  def exampleRejectInventoryLinkingControlResponseXML: Elem =
    <inventoryLinkingControlResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>{MessageCodes.CST}</messageCode>
      <actionCode>{actionCode_rejected}</actionCode>
      <ucr>
        <ucr>{ucr}</ucr>
        <ucrType>M</ucrType>
      </ucr>
      <movementReference>{movementReference}</movementReference>
      <error>
        <errorCode>{errorCode_1}</errorCode>
      </error>
      <error>
        <errorCode>{errorCodeDescriptive}</errorCode>
      </error>
    </inventoryLinkingControlResponse>

  val exampleRejectInventoryLinkingControlResponseMultipleErrorsNotification: Notification = Notification(
    conversationId = conversationId,
    responseType = "inventoryLinkingControlResponse",
    payload = Utility.trim(exampleRejectInventoryLinkingControlResponseXML).toString(),
    data = NotificationData(
      messageCode = Some(MessageCodes.CST),
      actionCode = Some(actionCode_rejected),
      movementReference = Some(movementReference),
      entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "M")))),
      errorCode = Seq(errorCode_1, errorCode_2, errorCode_3)
    )
  )

  def exampleRejectInventoryLinkingControlResponseMultipleErrorsXML: Elem =
    <inventoryLinkingControlResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>{MessageCodes.CST}</messageCode>
      <actionCode>{actionCode_rejected}</actionCode>
      <ucr>
        <ucr>{ucr}</ucr>
        <ucrType>M</ucrType>
      </ucr>
      <movementReference>{movementReference}</movementReference>
      <error>
        <errorCode>{errorCode_1}</errorCode>
      </error>
      <error>
        <errorCode>{errorCode_2}</errorCode>
      </error>
      <error>
        <errorCode>{errorCode_3}</errorCode>
      </error>
    </inventoryLinkingControlResponse>

  val exampleInventoryLinkingMovementTotalsResponseNotification: Notification = Notification(
    conversationId = conversationId,
    responseType = "inventoryLinkingMovementTotalsResponse",
    payload = Utility.trim(exampleInventoryLinkingMovementTotalsResponseXML).toString(),
    data = NotificationData(
      messageCode = Some(MessageCodes.ERS),
      crcCode = Some(crcCode_success),
      declarationCount = Some(declarationCount),
      entries = Seq(
        Entry(
          ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "D")),
          entryStatus = Some(EntryStatus(ics = Some("7"), roe = Some("6"), soe = Some("3"))),
          goodsItem = Seq(
            GoodsItem(
              commodityCode = Some(commodityCode_1),
              totalPackages = Some(totalPackages_1),
              totalNetMass = Some(BigDecimal(totalNetMass_1))
            ),
            GoodsItem(
              commodityCode = Some(commodityCode_2),
              totalPackages = Some(totalPackages_2),
              totalNetMass = Some(BigDecimal(totalNetMass_2))
            )
          )
        )
      ),
      goodsArrivalDateTime = Some("2019-07-12T13:14:54.000Z"),
      goodsLocation = Some(goodsLocation),
      masterRoe = Some("RE"),
      masterSoe = Some("SO"),
      masterUcr = Some(ucr_2),
      movementReference = Some("MovRef001234")
    )
  )

  def exampleInventoryLinkingMovementTotalsResponseXML: Elem =
    <inventoryLinkingMovementTotalsResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>{MessageCodes.ERS}</messageCode>
      <crc>{crcCode_success}</crc>
      <goodsLocation>{goodsLocation}</goodsLocation>
      <masterUCR>{ucr_2}</masterUCR>
      <declarationCount>{declarationCount}</declarationCount>
      <goodsArrivalDateTime>2019-07-12T13:14:54.000Z</goodsArrivalDateTime>
      <movementReference>{movementReference}</movementReference>
      <masterROE>RE</masterROE>
      <masterSOE>SO</masterSOE>
      <entry>
        <ucrBlock>
          <ucr>{ucr}</ucr>
          <ucrType>D</ucrType>
        </ucrBlock>
        <goodsItem>
          <commodityCode>{commodityCode_1}</commodityCode>
          <totalPackages>{totalPackages_1}</totalPackages>
          <totalNetMass>{totalNetMass_1}</totalNetMass>
        </goodsItem>
        <goodsItem>
          <commodityCode>{commodityCode_2}</commodityCode>
          <totalPackages>{totalPackages_2}</totalPackages>
          <totalNetMass>{totalNetMass_2}</totalNetMass>
        </goodsItem>
        <submitRole>{submitRole}</submitRole>
        <entryStatus>
          <ics>7</ics>
          <roe>6</roe>
          <soe>3</soe>
        </entryStatus>
      </entry>
    </inventoryLinkingMovementTotalsResponse>

  val exampleInventoryLinkingMovementResponseNotification: Notification = Notification(
    conversationId = conversationId,
    responseType = "inventoryLinkingMovementResponse",
    payload = Utility.trim(exampleInventoryLinkingMovementTotalsResponseXML).toString(),
    data = NotificationData(
      messageCode = Some(MessageCodes.EAL),
      crcCode = Some(crcCode_success),
      goodsArrivalDateTime = Some("2019-07-12T13:14:54.000Z"),
      goodsLocation = Some("Location"),
      movementReference = Some("MovRef001234"),
      entries = Seq(
        Entry(
          ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "D")),
          entryStatus = Some(EntryStatus(ics = Some("7"), roe = Some("6"), soe = Some("3"))),
          goodsItem = Seq(
            GoodsItem(
              commodityCode = Some(commodityCode_1),
              totalPackages = Some(totalPackages_1),
              totalNetMass = Some(BigDecimal(totalNetMass_1))
            ),
            GoodsItem(
              commodityCode = Some(commodityCode_2),
              totalPackages = Some(totalPackages_2),
              totalNetMass = Some(BigDecimal(totalNetMass_2))
            )
          )
        )
      )
    )
  )

  def exampleInventoryLinkingMovementResponseXML: Elem =
    <inventoryLinkingMovementResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>{MessageCodes.EAL}</messageCode>
      <crc>{crcCode_success}</crc>
      <goodsArrivalDateTime>2019-07-12T13:14:54.000Z</goodsArrivalDateTime>
      <goodsLocation>{goodsLocation}</goodsLocation>
      <movementReference>{movementReference}</movementReference>
      <submitRole>{submitRole}</submitRole>
      <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
      <goodsItem>
        <commodityCode>{commodityCode_1}</commodityCode>
        <totalPackages>{totalPackages_1}</totalPackages>
        <totalNetMass>{totalNetMass_1}</totalNetMass>
      </goodsItem>
      <goodsItem>
        <commodityCode>{commodityCode_2}</commodityCode>
        <totalPackages>{totalPackages_2}</totalPackages>
        <totalNetMass>{totalNetMass_2}</totalNetMass>
      </goodsItem>
      <entryStatus>
        <ics>7</ics>
        <roe>6</roe>
        <soe>3</soe>
      </entryStatus>
    </inventoryLinkingMovementResponse>

  def unknownFormatResponseXML: Elem =
    <UnknownFormat>
      <ATag>
        <AndAnInnerTag>Inner tag value</AndAnInnerTag>
      </ATag>
      <AnotherTag>With another value</AnotherTag>
    </UnknownFormat>

  val validHeaders: Map[String, String] = Map(
    "X-CDS-Client-ID" -> "1234",
    CustomsHeaderNames.XConversationIdName -> conversationId,
    CustomsHeaderNames.Authorization -> dummyAuthToken,
    "X-EORI-Identifier" -> "eori1",
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${2.0}+xml",
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
  )
  val noEoriHeaders: Map[String, String] = Map(
    "X-CDS-Client-ID" -> "1234",
    CustomsHeaderNames.XConversationIdName -> conversationId,
    CustomsHeaderNames.Authorization -> dummyAuthToken,
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${2.0}+xml",
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    "X-Badge-Identifier" -> "badgeIdentifier1"
  )
  val noAcceptHeader: Map[String, String] = Map(
    "X-CDS-Client-ID" -> "1234",
    "X-Conversation-ID" -> conversationId,
    HeaderNames.ACCEPT -> "",
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    "X-Badge-Identifier" -> "badgeIdentifier1"
  )
  val noContentTypeHeader: Map[String, String] = Map(
    "X-CDS-Client-ID" -> "1234",
    "X-Conversation-ID" -> conversationId,
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${2.0}+xml",
    HeaderNames.CONTENT_TYPE -> "",
    "X-Badge-Identifier" -> "badgeIdentifier1"
  )

  private val payloadExemplaryLength = 10
  val payload_1 = TestDataHelper.randomAlphanumericString(payloadExemplaryLength)
  val payload_2 = TestDataHelper.randomAlphanumericString(payloadExemplaryLength)

  val notification_1: Notification =
    Notification(
      conversationId = conversationId,
      responseType = "TestResponse_1",
      payload = payload_1,
      data = NotificationData(messageCode = Some(MessageCodes.EAL))
    )
  val notification_2: Notification =
    Notification(
      conversationId = conversationId_2,
      responseType = "TestResponse_2",
      payload = payload_2,
      data = NotificationData(messageCode = Some(MessageCodes.EDL))
    )
}
