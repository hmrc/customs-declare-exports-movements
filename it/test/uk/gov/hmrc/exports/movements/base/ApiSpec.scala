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

package uk.gov.hmrc.exports.movements.base

import com.codahale.metrics.{MetricRegistry, SharedMetricRegistries}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.exports.movements.connector.{AuditWiremockTestServer, IleApiWiremockTestServer}
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import uk.gov.hmrc.exports.movements.models.notifications.queries.IleQueryResponseData
import uk.gov.hmrc.exports.movements.models.submissions.{IleQuerySubmission, Submission}
import uk.gov.hmrc.exports.movements.repositories.{
  IleQueryResponseRepository,
  IleQuerySubmissionRepository,
  NotificationRepository,
  SubmissionRepository
}
import utils.stubs.{FixedTime, TestMongoDB}

import scala.concurrent.Future
import scala.xml.NodeSeq

/*
 * Component Tests are Intentionally Explicit with the JSON input, XML & DB output and DONT use TestData helpers.
 * That way these tests act as a "spec" for our API, and we dont get unintentional API changes as a result of Model/TestData refactors etc.
 */
abstract class ApiSpec
    extends AnyWordSpec with AuditWiremockTestServer with BeforeAndAfterEach with Eventually with FixedTime with GuiceOneServerPerSuite
    with IleApiWiremockTestServer with Matchers with TestMongoDB {

  /*
    Intentionally NOT exposing the real Repository as we shouldn't test our production code using our production classes.
   */
  private lazy val notificationRepository = app.injector.instanceOf[NotificationRepository]
  private lazy val ileQueryResponseRepository = app.injector.instanceOf[IleQueryResponseRepository]
  private lazy val submissionRepository = app.injector.instanceOf[SubmissionRepository]
  private lazy val ileQuerySubmissionRepository = app.injector.instanceOf[IleQuerySubmissionRepository]

  override def fakeApplication(): Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder()
      .disable[MetricRegistry]
      .configure(ileApieConfiguration)
      .configure(mongoConfiguration)
      .configure(auditConfiguration)
      .overrides(fixedTimeBinding)
      .build()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    await(ileQueryResponseRepository.removeAll)
    await(notificationRepository.removeAll)
    await(submissionRepository.removeAll)
    await(ileQuerySubmissionRepository.removeAll)
  }

  protected def givenAnExisting(submission: Submission): Unit = await(submissionRepository.insertOne(submission))

  protected def givenAnExisting(ileQuerySubmission: IleQuerySubmission): Unit =
    await(ileQuerySubmissionRepository.insertOne(ileQuerySubmission))

  protected def givenAnExisting(notification: Notification): Unit =
    notification.data match {
      case Some(_: IleQueryResponseData) => await(ileQueryResponseRepository.insertOne(notification))
      case _                             => await(notificationRepository.insertOne(notification))
    }

  protected def theSubmissionsFor(eori: String): Seq[Submission] =
    await(submissionRepository.findAll("eori", eori))

  protected def theNotificationsFor(conversationId: String): Seq[Notification] =
    await(notificationRepository.findAll("conversationId", conversationId))

  protected def theIleQuerySubmissionsFor(eori: String): Seq[IleQuerySubmission] =
    await(ileQuerySubmissionRepository.findAll("eori", eori))

  protected def get(call: Call, headers: (String, String)*): Future[Result] =
    route(app, FakeRequest("GET", call.url).withHeaders(headers: _*).withHeaders("User-Agent" -> userAgent)).get

  protected def post[T](call: Call, payload: JsObject, headers: (String, String)*): Future[Result] =
    route(app, FakeRequest("POST", call.url).withHeaders(headers: _*).withHeaders("User-Agent" -> userAgent).withJsonBody(payload)).get

  protected def post[T](call: Call, payload: NodeSeq, headers: (String, String)*): Future[Result] =
    route(app, FakeRequest("POST", call.url).withHeaders(headers: _*).withHeaders("User-Agent" -> userAgent).withXmlBody(payload)).get

  protected def verifyEventually(requestPatternBuilder: RequestPatternBuilder): Unit = eventually(WireMock.verify(requestPatternBuilder))
}
