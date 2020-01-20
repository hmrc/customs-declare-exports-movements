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

package utils.testdata.notifications

import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames
import uk.gov.hmrc.exports.movements.models.notifications._
import utils.testdata.CommonTestData._
import utils.testdata.TestDataHelper

import scala.xml.{Elem, Node, TopScope}

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
  val errorCode_3 = "30"
  val errorCodeDescriptive = "6 E408 Unique Consignment reference does not exist"
  val validatedErrorCodeDescriptive = "E408"
  val goodsLocation = "Location"
  val submitRole = "SubmitRoleBeing35CharactersInLength"
  val declarationCount = 123
  val commodityCode_1 = 12345678
  val commodityCode_2 = 11122233
  val totalPackages_1 = 456
  val totalPackages_2 = 13
  val totalNetMass_1 = "123456789012.3456"
  val totalNetMass_2 = "123.45"

  val declarationId = "DeclarationID"
  val declarationId_2 = "DeclarationID2"

  def clearNamespaces(xml: Node): Node = xml match {
    case e: Elem => e.copy(scope = TopScope, child = e.child.map(clearNamespaces))
    case o       => o
  }

  lazy val unknownFormatResponseXML: Elem =
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
