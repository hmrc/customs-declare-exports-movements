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

package uk.gov.hmrc.exports.movements.models.notifications

import javax.inject.Singleton

import scala.xml.NodeSeq

//TODO This should be an object. There is not point to need to inject this or have it as class. Changing to object will break a lot of code, so be careful
@Singleton
class NotificationFactory {

  private val inventoryLinkingControlResponseLabel = "inventoryLinkingControlResponse"
  private val inventoryLinkingMovementTotalsResponse = "inventoryLinkingMovementTotalsResponse"

  def buildMovementNotification(conversationId: String, xml: NodeSeq): Notification =
    if (xml.nonEmpty) {
      xml.head.label match {
        case `inventoryLinkingControlResponseLabel`   => buildFromKnownResponse(conversationId, xml)
        case `inventoryLinkingMovementTotalsResponse` => buildFromKnownResponse(conversationId, xml)
        case unknownLabel =>
          throw new IllegalArgumentException(s"Unknown Inventory Linking Response: $unknownLabel")
      }
    } else throw new IllegalArgumentException(s"Cannot find root element in: $xml")

  private def buildFromKnownResponse(conversationId: String, xml: NodeSeq): Notification =
    Notification(conversationId = conversationId, errors = buildErrors(xml), payload = xml.toString)

  private def buildErrors(responseXml: NodeSeq): Seq[NotificationError] =
    if ((responseXml \ "error").nonEmpty) {
      val errorsXml = responseXml \ "error"
      errorsXml.map(singleErrorXml => NotificationError(errorCode = (singleErrorXml \ "errorCode").text))
    } else Seq.empty

}
