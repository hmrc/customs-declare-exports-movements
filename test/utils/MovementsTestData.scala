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

import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.exports.movements.controllers.util.CustomsHeaderNames._
import uk.gov.hmrc.exports.movements.models._
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import uk.gov.hmrc.wco.dec.{DateTimeString, MetaData, Response, ResponseDateTimeElement, Declaration => WcoDeclaration}

import scala.util.Random

object MovementsTestData {

  private lazy val responseFunctionCodes: Seq[String] =
    Seq("01", "02", "03", "05", "06", "07", "08", "09", "10", "11", "16", "17", "18")
  val validEori: String = "GB167676"
  val randomEori: String = randomString(8)
  val lrn: Option[String] = Some(randomString(22))
  val mrn: String = "MRN87878797"
  val conversationId: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e11"
  val conversationId_2: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e22"
  val randomUcr: String = randomString(16)
  val before: Long = System.currentTimeMillis()
  val authToken: String =
    "BXQ3/Treo4kQCZvVcCqKPlwxRN4RA9Mb5RF8fFxOuwG5WSg+S+Rsp9Nq998Fgg0HeNLXL7NGwEAIzwM6vuA6YYhRQnTRFaBhrp+1w+kVW8g1qHGLYO48QPWuxdM87VMCZqxnCuDoNxVn76vwfgtpNj0+NwfzXV2Zc12L2QGgF9H9KwIkeIPK/mMlBESjue4V]"
  val dummyToken: String = s"Bearer $authToken"
  val declarantEoriValue: String = "ZZ123456789000"
  val declarantEori: Eori = Eori(declarantEoriValue)
  val declarantLrnValue: String = "MyLrnValue1234"
  val declarantUcrValue: String = "MyDucrValue1234"
  val declarantMrnValue: String = "MyMucrValue1234"
  val ContentTypeHeader: (String, String) = CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
  val ValidXEoriIdentifierHeader: (String, String) = XEoriIdentifierHeaderName -> declarantEoriValue
  val ValidLrnHeader: (String, String) = XLrnHeaderName -> declarantLrnValue
  val ValidAuthorizationHeader: (String, String) = HeaderNames.AUTHORIZATION -> dummyToken
  val ValidConversationIdHeader: (String, String) = XConversationIdName -> conversationId
  val ValidUcrHeader: (String, String) = XUcrHeaderName -> declarantUcrValue
  val ValidMovementTypeHeader: (String, String) = XMovementTypeHeaderName -> "Arrival"

  val now: DateTime = DateTime.now.withZone(DateTimeZone.UTC)
  val dtfOut = DateTimeFormat.forPattern("yyyyMMddHHmmss")

  val response1: Seq[Response] = Seq(
    Response(
      functionCode = randomResponseFunctionCode,
      functionalReferenceId = Some("123"),
      issueDateTime = dateTimeElement(now.minusHours(6))
    )
  )
  val response2: Seq[Response] = Seq(
    Response(
      functionCode = randomResponseFunctionCode,
      functionalReferenceId = Some("456"),
      issueDateTime = dateTimeElement(now.minusHours(5))
    )
  )

  def movementSubmission(
    eori: String = validEori,
    convoId: String = conversationId,
    subUcr: String = randomUcr
  ): MovementSubmissions =
    MovementSubmissions(eori, convoId, subUcr, "Arrival")

  val ValidHeaders: Map[String, String] = Map(
    ContentTypeHeader,
    ValidAuthorizationHeader,
    ValidConversationIdHeader,
    ValidXEoriIdentifierHeader,
    // This is not needed
    ValidLrnHeader,
    ValidUcrHeader,
    ValidMovementTypeHeader
  )

  def dateTimeElement(dateTimeVal: DateTime) =
    Some(ResponseDateTimeElement(DateTimeString("102", dateTimeVal.toString("yyyyMMdd"))))

  def validInventoryLinkingExportRequest = InventoryLinkingMovementRequest(
    messageCode = "11",
    agentDetails = Some(AgentDetails(eori = Some(declarantEoriValue), agentLocation = Some("location"))),
    ucrBlock = UcrBlock(ucr = declarantUcrValue, ucrType = "type"),
    goodsLocation = "goodsLocation",
    goodsArrivalDateTime = Some(now.toString),
    goodsDepartureDateTime = Some(now.toString),
    transportDetails = Some(TransportDetails(transportID = Some("transportId"), transportMode = Some("mode")))
  )

  def randomSubmitDeclaration: MetaData =
    MetaData(declaration = Option(WcoDeclaration(functionalReferenceId = Some(randomString(35)))))

  protected def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  protected def randomResponseFunctionCode: String = responseFunctionCodes(Random.nextInt(responseFunctionCodes.length))
}
