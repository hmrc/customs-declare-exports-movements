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
import uk.gov.hmrc.exports.movements.models.notifications.{MovementNotification, NotificationError}

import scala.xml.Elem

object NotificationTestData extends MovementsTestData {

  val dummyAuthToken: String =
    "Bearer BXQ3/Treo4kQCZvVcCqKPlwxRN4RA9Mb5RF8fFxOuwG5WSg+S+Rsp9Nq998Fgg0HeNLXL7NGwEAIzwM6vuA6YYhRQnTRFa" +
      "Bhrp+1w+kVW8g1qHGLYO48QPWuxdM87VMCZqxnCuDoNxVn76vwfgtpNj0+NwfzXV2Zc12L2QGgF9H9KwIkeIPK/mMlBESjue4V]"

  val movementUri = "/customs-declare-exports/notifyMovement"

  val exampleRejectInventoryLinkingControlResponseNotification: MovementNotification = MovementNotification(
    conversationId = conversationId,
    errors = Seq(NotificationError("01"), NotificationError("21")),
    payload = exampleRejectInventoryLinkingControlResponseXML.toString()
  )

  def exampleRejectInventoryLinkingControlResponseXML: Elem =
    <inventoryLinkingControlResponse>
      <messageCode>CST</messageCode>
      <actionCode>3</actionCode>
      <ucr>
        <ucr>5GB123456789000-123ABC456DEFIIIII</ucr>
        <ucrType>M</ucrType>
      </ucr>
      <movementReference/>
      <error>
        <errorCode>01</errorCode>
      </error>
      <error>
        <errorCode>21</errorCode>
      </error>
    </inventoryLinkingControlResponse>

  val exampleInventoryLinkingMovementTotalsResponseNotification: MovementNotification = MovementNotification(
    conversationId = conversationId,
    errors = Seq.empty,
    payload = exampleInventoryLinkingMovementTotalsResponseXML.toString()
  )

  def exampleInventoryLinkingMovementTotalsResponseXML: Elem =
    <inventoryLinkingMovementTotalsResponse>
      <messageCode>ERS</messageCode>
      <goodsLocation>GBAULGWLGWLGW</goodsLocation>
      <goodsArrivalDateTime>2019-07-12T13:14:54.000Z</goodsArrivalDateTime>
      <movementReference>JVG0MBQ1DQQBSWS1UEU6LFXFK</movementReference>
      <entry>
        <ucrBlock>
          <ucr>9GB025115188654-IAZ1</ucr>
          <ucrType>D</ucrType>
        </ucrBlock>
        <entryStatus>
          <roe>6</roe>
          <soe>3</soe>
        </entryStatus>
      </entry>
    </inventoryLinkingMovementTotalsResponse>

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

  val errors = Seq(NotificationError(errorCode = "01"))

  private val payloadExemplaryLength = 300
  val payload_1 = TestDataHelper.randomAlphanumericString(payloadExemplaryLength)
  val payload_2 = TestDataHelper.randomAlphanumericString(payloadExemplaryLength)

  val notification_1: MovementNotification =
    MovementNotification(conversationId = conversationId, errors = errors, payload = payload_1)
  val notification_2: MovementNotification =
    MovementNotification(conversationId = conversationId, errors = errors, payload = payload_2)
}
