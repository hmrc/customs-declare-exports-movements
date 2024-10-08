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

package utils.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import play.api.http.Status
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers.{AUTHORIZATION, OK}
import play.mvc.Http.Status._
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.exports.movements.models.Eori
import utils.testdata.CommonTestData.{authToken, validEori}
import uk.gov.hmrc.exports.movements.base.WireMockRunner

trait AuthService extends WireMockRunner {

  val authUrl = "/auth/authorise"
  private val authUrlMatcher = urlEqualTo(authUrl)

  private val customsEnrolmentName = "HMRC-CUS-ORG"

  private val authorisationPredicate = Enrolment(customsEnrolmentName)

  private def bearerTokenMatcher(bearerToken: String): StringValuePattern = equalTo("Bearer " + bearerToken)

  def stubUnauthorizedForAll(): Unit =
    stubFor(post(urlEqualTo(authUrl)).willReturn(aResponse().withStatus(UNAUTHORIZED)))

  def stubDefaultAuthorisation(): Unit = {
    stubUnauthorizedForAll()
    stubBearerTokenAuth()
  }

  def stubBearerTokenAuth(bearerToken: String = authToken, status: Int = OK): Unit =
    stubFor(
      post(urlEqualTo(authUrl))
        .withHeader(AUTHORIZATION, equalTo(s"Bearer $bearerToken"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody("{}")
        )
    )

  def authServiceAuthorizesWithEoriAndNoRetrievals(bearerToken: String = authToken, eori: Eori = Eori(validEori)): Unit =
    stubFor(
      post(authUrlMatcher)
        .withRequestBody(equalToJson(authRequestJsonWithAuthorisedEnrolmentRetrievals(authorisationPredicate)))
        .withHeader(AUTHORIZATION, bearerTokenMatcher(bearerToken))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(s"""{
               |  "allEnrolments": [ ${enrolmentRetrievalJson(customsEnrolmentName, "EORINumber", eori.value)} ]
               |}""".stripMargin)
        )
    )

  private def authRequestJsonWithAuthorisedEnrolmentRetrievals(predicate: Predicate): String = {
    val predicateJsArray: JsArray = predicateToJson(predicate)
    val js =
      s"""
         |{
         |  "authorise": $predicateJsArray,
         |  "retrieve" : ["allEnrolments"]
         |}
    """.stripMargin
    js
  }

  private def predicateToJson(predicate: Predicate): JsArray =
    predicate.toJson match {
      case arr: JsArray => arr
      case other        => Json.arr(other)
    }

  private def enrolmentRetrievalJson(enrolmentKey: String, identifierName: String, identifierValue: String): String =
    s"""
       |{
       | "key": "$enrolmentKey",
       | "identifiers": [
       |   {
       |     "key": "$identifierName",
       |     "value": "$identifierValue"
       |   }
       | ]
       |}
    """.stripMargin

  def verifyBearerTokenAuthorisationCalled(bearerToken: String = authToken): Unit =
    verify(
      postRequestedFor(urlEqualTo(authUrl))
        .withHeader(AUTHORIZATION, equalTo(s"Bearer $bearerToken"))
    )

  def verifyAuthorisationCalledWithoutBearerToken(): Unit =
    verify(postRequestedFor(urlEqualTo(authUrl)).withoutHeader(AUTHORIZATION))

  def verifyAuthServiceCalledForNonCsp(bearerToken: String = authToken): Unit =
    verify(
      1,
      postRequestedFor(authUrlMatcher)
        .withRequestBody(equalToJson(authRequestJsonWithAuthorisedEnrolmentRetrievals(authorisationPredicate)))
        .withHeader(AUTHORIZATION, bearerTokenMatcher(bearerToken))
    )

  def verifyAuthorisationNotCalled(): Unit =
    verify(0, postRequestedFor(urlEqualTo(authUrl)))
}

object ExternalServicesConfig {

  val Port: Int = sys.env.getOrElse("WIREMOCK_SERVICE_LOCATOR_PORT", "11111").toInt
  val Host = "localhost"
  val MdgSuppDecServiceContext = "/mdgSuppDecService/submitdeclaration"
  val AuditContext = "/write/audit.*"
  val NrsServiceContext = "/submission"
  val mongoDBUri = "mongodb://localhost:27017/customs-declare-exports-movements"
}
