/*
 * Copyright 2023 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import org.slf4j.MDC
import play.api.Logger
import uk.gov.hmrc.exports.movements.models.notifications.parsers.ResponseParserProvider

import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, Utility, XML}

@Singleton
class NotificationFactory @Inject() (responseValidator: ResponseValidator, responseParserProvider: ResponseParserProvider) {

  private val logger = Logger(this.getClass)

  def buildMovementNotification(conversationId: String, xml: String): Notification =
    Try(XML.loadString(xml)) match {
      case Success(xmlElem) => buildMovementNotification(conversationId, xmlElem)
      case Failure(exc) =>
        logWarnings(conversationId, exc, xml)
        Notification(conversationId = conversationId, payload = xml, data = None)
    }

  def buildMovementNotification(conversationId: String, xml: NodeSeq): Notification =
    responseValidator.validate(xml).map(_ => responseParserProvider.provideResponseParser(xml)).map(_.parse(xml)) match {
      case Success(notificationData) =>
        Notification(conversationId = conversationId, payload = Utility.trim(xml.head).toString, data = Some(notificationData))

      case Failure(exc) =>
        logWarnings(conversationId, exc, xml.toString())
        Notification(conversationId = conversationId, payload = Utility.trim(xml.head).toString, data = None)
    }

  private def logWarnings(conversationId: String, exc: Throwable, xml: String): Unit = {
    MDC.put("conversationId", conversationId)
    val message = s"There was a problem during parsing notification with conversationId=[$conversationId]"
    logger.error(s"$message: ${exc.getClass} => ${exc.getMessage}.\nPayload was [$xml]")
    MDC.remove("conversationId")
  }
}
