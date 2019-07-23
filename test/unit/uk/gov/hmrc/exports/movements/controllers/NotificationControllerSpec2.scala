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

package unit.uk.gov.hmrc.exports.movements.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.controllers.NotificationController
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.models.notifications.MovementNotificationFactory
import uk.gov.hmrc.exports.movements.repositories.{MovementSubmissionRepository, NotificationRepository}
import uk.gov.hmrc.exports.movements.services.NotificationService
import unit.uk.gov.hmrc.exports.movements.MockMetrics
import unit.uk.gov.hmrc.exports.movements.base.AuthTestSupport
import utils.NotificationTestData._

import scala.concurrent.Future

class NotificationControllerSpec2
    extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with AuthTestSupport with MockMetrics {

  trait SetUp {
    val mockHeaderValidator = mock[HeaderValidator]
    val mockNotificationRepository = mock[NotificationRepository]
    val mockSubmissionRepository = mock[MovementSubmissionRepository]
    val mockNotificationService = new NotificationService(mockNotificationRepository, mockSubmissionRepository)
    val mockNotificationFactory = mock[MovementNotificationFactory]

    val controller = new NotificationController(
      mockAuthConnector,
      mockHeaderValidator,
      mockMetrics,
      mockNotificationService,
      mockNotificationFactory,
      stubControllerComponents()
    )
  }

  "Notification Controller" should {

    "return list of notifications" in new SetUp {
      withAuthorizedUser()

      when(mockSubmissionRepository.findByEori(any())).thenReturn(Future.successful(Seq.empty))
      when(mockNotificationRepository.findNotificationsByConversationId(any())).thenReturn(Future.successful(Seq.empty))

      val result =
        controller.listOfNotifications()(FakeRequest(POST, "").withHeaders(validHeaders.toSeq: _*))

      status(result) must be(OK)
    }
  }
}
