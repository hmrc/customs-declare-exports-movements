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

import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames._
import utils.testdata.TestDataHelper.randomAlphanumericString

object CommonTestData {

  object MessageCodes {
    val EAA = "EAA"
    val EAL = "EAL"
    val EDL = "EDL"
    val EAC = "EAC"
    val CST = "CST"
    val ERS = "ERS"
    val EMR = "EMR"
  }

  val validEori: String = "GB167676"
  val validEori_2: String = "GB089393"
  val validProviderId: String = "PROVIDERID123"
  val validProviderId_2: String = "PROVIDERID456"
  val conversationId: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e11"
  val conversationId_2: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e22"
  val conversationId_3: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e33"
  val conversationId_4: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e44"
  val conversationId_5: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e55"
  val ucr = "7GB123456789000-123ABC456DEFQWERT"
  val ucr_2 = "9GB025115188654-IAZ1"
  val randomUcr: String = randomAlphanumericString(16)

  val location = "LOCATION"
  val agentRole = "ARL"
  val shedOPID = "SOP"
  val movementReference = "MovRef001234"
  private val masterOptCodes = Seq("A", "F", "R", "X")
  val masterOpt = masterOptCodes.head
  val transportId = "TransportID"
  val transportMode = "X"
  val transportNationality = "UK"

  val authToken: String =
    "BXQ3/Treo4kQCZvVcCqKPlwxRN4RA9Mb5RF8fFxOuwG5WSg+S+Rsp9Nq998Fgg0HeNLXL7NGwEAIzwM6vuA6YYhRQnTRFaBhrp+1w+kVW8g1qHGLYO48QPWuxdM87VMCZqxnCuDoNxVn76vwfgtpNj0+NwfzXV2Zc12L2QGgF9H9KwIkeIPK/mMlBESjue4V]"
  val dummyToken: String = s"Bearer $authToken"
  val declarantLrnValue: String = "MyLrnValue1234"
  val declarantUcrValue: String = "MyDucrValue1234"
  val declarantMrnValue: String = "MyMucrValue1234"
  val XmlContentTypeHeader: (String, String) = CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
  val JsonContentTypeHeader: (String, String) = CONTENT_TYPE -> ContentTypes.JSON
  val ValidAuthorizationHeader: (String, String) = HeaderNames.AUTHORIZATION -> dummyToken
  val ValidConversationIdHeader: (String, String) = XConversationIdName -> conversationId
  val ValidUcrHeader: (String, String) = XUcrHeaderName -> declarantUcrValue
  val ValidLrnHeader: (String, String) = XLrnHeaderName -> declarantLrnValue
  val ValidMovementTypeHeader: (String, String) = XMovementTypeHeaderName -> "Arrival"

  val ValidHeaders: Map[String, String] =
    Map(XmlContentTypeHeader, ValidAuthorizationHeader, ValidConversationIdHeader)
  val ValidJsonHeaders: Map[String, String] =
    Map(JsonContentTypeHeader, ValidAuthorizationHeader, ValidConversationIdHeader)

}
