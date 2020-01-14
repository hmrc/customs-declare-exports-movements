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

package unit.uk.gov.hmrc.exports.movements.controllers

import play.api.libs.json.Writes
import play.api.mvc.{AnyContent, AnyContentAsJson, AnyContentAsXml}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST}
import utils.testdata.notifications.NotificationTestData.validHeaders

import scala.xml.Elem

object FakeRequestFactory {

  def getRequest(): FakeRequest[AnyContent] = FakeRequest(GET, "/").withHeaders(validHeaders.toSeq: _*)

  def postRequest(): FakeRequest[AnyContent] = FakeRequest(POST, "/").withHeaders(validHeaders.toSeq: _*)

  def postRequest[T](body: T)(implicit wts: Writes[T]): FakeRequest[AnyContentAsJson] = postRequest().withJsonBody(wts.writes(body))

  def postRequestWithXmlBody(body: Elem): FakeRequest[AnyContentAsXml] = postRequest().withXmlBody(body)
}