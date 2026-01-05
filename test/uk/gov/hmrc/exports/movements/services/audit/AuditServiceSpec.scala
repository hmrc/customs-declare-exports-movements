/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.exports.movements.services.audit

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.libs.json.Json
import uk.gov.hmrc.exports.movements.base.UnitSpec
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.services.audit.AuditTypes.NotificationProcessed
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AuditServiceSpec extends UnitSpec {

  "AuditService.auditNotificationProcessed" should {
    "send the expected ExtendedDataEvent" in {
      val appConfig = mock[AppConfig]
      when(appConfig.appName).thenReturn("app-name")

      val auditConnector = mock[AuditConnector]
      when(auditConnector.sendExtendedEvent(any())(any(), any())).thenReturn(Future.successful(Success))

      val auditService = new AuditService(auditConnector, appConfig)(global)
      auditService.auditNotificationProcessed(NotificationProcessed, Json.obj()).futureValue mustBe Success
    }
  }
}
