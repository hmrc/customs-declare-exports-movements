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
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.exports.movements.services.SubmissionService
import unit.uk.gov.hmrc.exports.movements.base.AuthTestSupport
import utils.testdata.CommonTestData.{conversationId, conversationId_2, conversationId_3, ValidHeaders}
import utils.testdata.MovementsTestData._

import scala.concurrent.Future

class SubmissionControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with AuthTestSupport with BeforeAndAfterEach with ScalaFutures with MustMatchers {

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[AuthConnector].to(mockAuthConnector), bind[SubmissionService].to(submissionServiceMock))
    .build()

  private val getAllSubmissionsUri = "/movements"
  private val submissionServiceMock = mock[SubmissionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, submissionServiceMock)
  }

  private def getSubmissionUri(conversationId: String) = s"/movements/$conversationId"

  private def routeGet(headers: Map[String, String] = ValidHeaders, uri: String): Future[Result] =
    route(app, FakeRequest(GET, uri).withHeaders(headers.toSeq: _*)).get

  "SubmissionController on getAllSubmissions" should {

    "return Ok status" in {
      withAuthorizedUser()
      when(submissionServiceMock.getSubmissionsByEori(any[String])).thenReturn(Future.successful(Seq.empty))

      val result = routeGet(uri = getAllSubmissionsUri)

      status(result) must be(OK)
    }

    "return what SubmissionService returns in the body" in {
      withAuthorizedUser()
      val serviceResponseContent =
        Seq(exampleSubmission(), exampleSubmission(conversationId = conversationId_2), exampleSubmission(conversationId = conversationId_3))
      when(submissionServiceMock.getSubmissionsByEori(any[String]))
        .thenReturn(Future.successful(serviceResponseContent))

      val result = routeGet(uri = getAllSubmissionsUri)

      status(result) must be(OK)
      contentAsJson(result) must equal(Json.toJson(serviceResponseContent))
    }
  }

  "SubmissionController on getSubmission" should {

    "return Ok status" in {
      withAuthorizedUser()
      when(submissionServiceMock.getSubmissionByConversationId(any[String])).thenReturn(Future.successful(None))

      val result = routeGet(uri = getSubmissionUri(conversationId))

      status(result) must be(OK)
    }

    "return what SubmissionService returns in the body" in {
      withAuthorizedUser()
      val serviceResponseContent = Some(exampleSubmission())
      when(submissionServiceMock.getSubmissionByConversationId(any[String]))
        .thenReturn(Future.successful(serviceResponseContent))

      val result = routeGet(uri = getSubmissionUri(conversationId))

      status(result) must be(OK)
      contentAsJson(result) must equal(Json.toJson(serviceResponseContent))
    }
  }
}
