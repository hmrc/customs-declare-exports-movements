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

package uk.gov.hmrc.exports.movements.models.notifications

import javax.inject.{Inject, Singleton}
import org.slf4j.MDC
import play.api.Logger
import uk.gov.hmrc.exports.movements.models.notifications
import uk.gov.hmrc.exports.movements.models.notifications.parsers.ResponseParserFactory

import scala.xml.{NodeSeq, SAXParseException, Utility}

@Singleton
class NotificationFactory @Inject()(responseValidator: ResponseValidator, responseParserFactory: ResponseParserFactory) {

  private val logger = Logger(this.getClass)

  def buildMovementNotification(conversationId: String, xml: NodeSeq): Notification = {
    val context = responseParserFactory.buildResponseParserContext(xml)
    checkResponseCompliance(conversationId, xml)

    val notificationData = context.parser.parse(xml)
    notifications.Notification(
      conversationId = conversationId,
      responseType = context.responseType,
      data = notificationData,
      payload = Utility.trim(xml.head).toString
    )
  }

  private def checkResponseCompliance(conversationId: String, xml: NodeSeq): Unit =
    responseValidator.validate(xml).recover {
      case exc: SAXParseException =>
        MDC.put("conversationId", conversationId)
        logger
          .warn(s"Received Notification for Conversation ID: [$conversationId] does not match the schema: ${exc.getMessage}")
        MDC.remove("conversationId")
    }

}
