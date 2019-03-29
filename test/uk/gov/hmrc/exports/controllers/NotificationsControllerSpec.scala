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

package uk.gov.hmrc.exports.controllers

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.exports.base.{CustomsExportsBaseSpec, ExportsTestData}

class NotificationsControllerSpec
    extends CustomsExportsBaseSpec with ExportsTestData with BeforeAndAfterEach with NotificationTestData {

  "Notifications controller" should {

    "return 202 status when it successfully save movement notification" in {
      withMovementNotificationSaved(true)

      val result =
        route(app, FakeRequest(POST, movementUri).withHeaders(validHeaders.toSeq: _*).withXmlBody(movementXml)).get

      status(result) must be(ACCEPTED)
    }

    "return 500 status if fail to save movement notification" in {
      withMovementNotificationSaved(false)

      val result =
        route(app, FakeRequest(POST, movementUri).withHeaders(validHeaders.toSeq: _*).withXmlBody(movementXml)).get

      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    "return 500 status if there is no EORI number in movement notification header" in {
      val result =
        route(app, FakeRequest(POST, movementUri).withHeaders(noEoriHeaders.toSeq: _*).withXmlBody(movementXml)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must be
      "<errorResponse><code>INTERNAL_SERVER_ERROR</code><message>" +
        "ClientId or ConversationId or EORI is missing in the request headers</message></errorResponse>"
    }
  }

  override def beforeEach: Unit =
    reset(mockMovementNotificationsRepository)
}
