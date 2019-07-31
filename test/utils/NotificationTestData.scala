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
import play.api.mvc.Codec
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.notifications._
import utils.MovementsTestData._

import scala.xml.{Elem, Utility}

object NotificationTestData {

  val dummyAuthToken: String =
    "Bearer BXQ3/Treo4kQCZvVcCqKPlwxRN4RA9Mb5RF8fFxOuwG5WSg+S+Rsp9Nq998Fgg0HeNLXL7NGwEAIzwM6vuA6YYhRQnTRFa" +
      "Bhrp+1w+kVW8g1qHGLYO48QPWuxdM87VMCZqxnCuDoNxVn76vwfgtpNj0+NwfzXV2Zc12L2QGgF9H9KwIkeIPK/mMlBESjue4V]"

  val movementUri = "/customs-declare-exports/notifyMovement"

  val errorCode = "21"
  val goodsLocation = "Location"
  val declarationCount = 123
  val commodityCode_1 = 12345678
  val commodityCode_2 = 11122233
  val totalPackages_1 = 456
  val totalPackages_2 = 13
  val totalNetMass_1 = "123456789012.3456"
  val totalNetMass_2 = "123.45"

  val exampleRejectInventoryLinkingControlResponseNotification: Notification = Notification(
    conversationId = conversationId,
    responseType = "inventoryLinkingControlResponse",
    payload = Utility.trim(exampleRejectInventoryLinkingControlResponseXML).toString(),
    data = NotificationData(
      messageCode = Some(MessageCodes.CST),
      actionCode = Some("3"),
      entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = "5GB123456789000-123ABC456DEFIIIII", ucrType = "M")))),
      errorCode = Some(errorCode)
    )
  )

  def exampleRejectInventoryLinkingControlResponseXML: Elem =
    <inventoryLinkingControlResponse>
      <messageCode>{MessageCodes.CST}</messageCode>
      <actionCode>3</actionCode>
      <ucr>
        <ucr>5GB123456789000-123ABC456DEFIIIII</ucr>
        <ucrType>M</ucrType>
      </ucr>
      <error>
        <errorCode>21</errorCode>
      </error>
    </inventoryLinkingControlResponse>

  val exampleInventoryLinkingMovementTotalsResponseNotification: Notification = Notification(
    conversationId = conversationId,
    responseType = "inventoryLinkingMovementTotalsResponse",
    payload = Utility.trim(exampleInventoryLinkingMovementTotalsResponseXML).toString(),
    data = NotificationData(
      messageCode = Some(MessageCodes.ERS),
      crcCode = Some("CRC"),
      declarationCount = Some(declarationCount),
      entries = Seq(
        Entry(
          ucrBlock = Some(UcrBlock(ucr = "9GB025115188654-IAZ1", ucrType = "D")),
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
      masterUcr = Some("7GB123456789000-123ABC456DEFQWERT"),
      movementReference = Some("MovRef001234")
    )
  )

  def exampleInventoryLinkingMovementTotalsResponseXML: Elem =
    <inventoryLinkingMovementTotalsResponse>
      <messageCode>{MessageCodes.ERS}</messageCode>
      <crc>CRC</crc>
      <goodsLocation>{goodsLocation}</goodsLocation>
      <goodsArrivalDateTime>2019-07-12T13:14:54.000Z</goodsArrivalDateTime>
      <movementReference>MovRef001234</movementReference>
      <declarationCount>{declarationCount}</declarationCount>
      <masterUCR>7GB123456789000-123ABC456DEFQWERT</masterUCR>
      <masterROE>RE</masterROE>
      <masterSOE>SO</masterSOE>
      <entry>
        <ucrBlock>
          <ucr>9GB025115188654-IAZ1</ucr>
          <ucrType>D</ucrType>
        </ucrBlock>
        <entryStatus>
          <ics>7</ics>
          <roe>6</roe>
          <soe>3</soe>
        </entryStatus>
        <submitRole>SubmitRole</submitRole>
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
      </entry>
    </inventoryLinkingMovementTotalsResponse>

  val exampleInventoryLinkingMovementResponseNotification: Notification = Notification(
    conversationId = conversationId,
    responseType = "inventoryLinkingMovementResponse",
    payload = Utility.trim(exampleInventoryLinkingMovementTotalsResponseXML).toString(),
    data = NotificationData(
      messageCode = Some(MessageCodes.EAL),
      crcCode = Some("CRC"),
      goodsArrivalDateTime = Some("2019-07-12T13:14:54.000Z"),
      goodsLocation = Some("Location"),
      movementReference = Some("MovRef001234"),
      entries = Seq(
        Entry(
          ucrBlock = Some(UcrBlock(ucr = "9GB025115188654-IAZ1", ucrType = "D")),
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
    <inventoryLinkingMovementResponse>
      <messageCode>{MessageCodes.EAL}</messageCode>
      <crc>CRC</crc>
      <goodsLocation>{goodsLocation}</goodsLocation>
      <goodsArrivalDateTime>2019-07-12T13:14:54.000Z</goodsArrivalDateTime>
      <movementReference>MovRef001234</movementReference>
      <submitRole>SubmitRole</submitRole>
      <ucrBlock>
        <ucr>9GB025115188654-IAZ1</ucr>
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
