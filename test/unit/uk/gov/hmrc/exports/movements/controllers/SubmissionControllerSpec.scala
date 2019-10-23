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
import org.mockito.Mockito
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.models.submissions.SubmissionFrontendModel
import uk.gov.hmrc.exports.movements.repositories.QueryParameters
import uk.gov.hmrc.exports.movements.services.SubmissionService
import utils.testdata.CommonTestData._
import utils.testdata.MovementsTestData._

import scala.concurrent.Future

class SubmissionControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with MustMatchers with MockitoSugar {

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[SubmissionService].to(submissionServiceMock))
    .build()

  private def getAllSubmissionsUri = s"/movements?eori=$validEori"
  private val submissionServiceMock = mock[SubmissionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(submissionServiceMock)
  }

  private def getSubmissionUri(conversationId: String) = s"/movements/$conversationId?eori=$validEori"

  private def routeGet(headers: Map[String, String] = ValidHeaders, uri: String): Future[Result] =
    route(app, FakeRequest(GET, uri).withHeaders(headers.toSeq: _*)).get

  "SubmissionController on getAllSubmissions" should {

    "call SubmissionService" in {
      when(submissionServiceMock.getSubmissions(any[QueryParameters])).thenReturn(Future.successful(Seq.empty))

      routeGet(uri = getAllSubmissionsUri).futureValue

      Mockito.verify(submissionServiceMock).getSubmissions(any[QueryParameters])
    }

    "return Ok status" in {
      when(submissionServiceMock.getSubmissions(any[QueryParameters])).thenReturn(Future.successful(Seq.empty))

      val result = routeGet(uri = getAllSubmissionsUri)

      status(result) must be(OK)
    }

    "return what SubmissionService returns in the body" in {
      val serviceResponseContent =
        Seq(exampleSubmission(), exampleSubmission(conversationId = conversationId_2), exampleSubmission(conversationId = conversationId_3))
          .map(SubmissionFrontendModel(_))
      when(submissionServiceMock.getSubmissions(any[QueryParameters])).thenReturn(Future.successful(serviceResponseContent))

      val result = routeGet(uri = getAllSubmissionsUri)

      status(result) must be(OK)
      contentAsJson(result) must equal(Json.toJson(serviceResponseContent))
    }
  }

  "SubmissionController on getSubmission" should {

    "call SubmissionService" in {
      when(submissionServiceMock.getSingleSubmission(any[QueryParameters])).thenReturn(Future.successful(None))

      routeGet(uri = getSubmissionUri(conversationId)).futureValue

      Mockito.verify(submissionServiceMock).getSingleSubmission(any[QueryParameters])
    }

    "return Ok status" in {
      when(submissionServiceMock.getSingleSubmission(any[QueryParameters])).thenReturn(Future.successful(None))

      val result = routeGet(uri = getSubmissionUri(conversationId))

      status(result) must be(OK)
    }

    "return what SubmissionService returns in the body" in {
      val serviceResponseContent = Some(SubmissionFrontendModel(exampleSubmission()))
      when(submissionServiceMock.getSingleSubmission(any[QueryParameters])).thenReturn(Future.successful(serviceResponseContent))

      val result = routeGet(uri = getSubmissionUri(conversationId))

      status(result) must be(OK)
      contentAsJson(result) must equal(Json.toJson(serviceResponseContent))
    }
  }

}
