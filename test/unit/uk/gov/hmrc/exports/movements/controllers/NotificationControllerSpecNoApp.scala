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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.controllers.NotificationController
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import unit.uk.gov.hmrc.exports.movements.base.UnitTestMockBuilder._
import utils.testdata.CommonTestData.{conversationId, validEori}
import utils.testdata.notifications.NotificationTestData._

import scala.concurrent.{ExecutionContext, Future}

class NotificationControllerSpecNoApp extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures {

  trait SetUp {
    val headerValidatorMock = mock[HeaderValidator]
    val notificationRepositoryMock = buildNotificationRepositoryMock
    val submissionRepositoryMock = buildSubmissionRepositoryMock
    val notificationServiceMock = buildNotificationServiceMock
    val notificationFactoryMock = buildMovementNotificationFactoryMock
    val movementsMetricsMock = buildMovementsMetricsMock

    val controller = new NotificationController(
      headerValidatorMock,
      movementsMetricsMock,
      notificationServiceMock,
      notificationFactoryMock,
      stubControllerComponents()
    )(ExecutionContext.global)

    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
  }

  "Notification Controller" should {

    "return list of notifications" in new SetUp {

      when(notificationRepositoryMock.findByConversationId(any())).thenReturn(Future.successful(Seq.empty))

      val result =
        controller.listOfNotifications(Some(validEori), None, conversationId)(FakeRequest(POST, "").withHeaders(validHeaders.toSeq: _*))

      status(result) must be(OK)
    }
  }
}
