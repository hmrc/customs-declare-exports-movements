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

package unit.uk.gov.hmrc.exports.movements.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, INTERNAL_SERVER_ERROR}
import play.api.mvc.Result
import uk.gov.hmrc.exports.movements.models.CustomsInventoryLinkingResponse
import uk.gov.hmrc.exports.movements.models.submissions.Submission
import uk.gov.hmrc.exports.movements.services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import unit.uk.gov.hmrc.exports.movements.base.CustomsExportsBaseSpec
import utils.MovementsTestData._

import scala.concurrent.Future
import scala.xml.NodeSeq

class SubmissionServiceSpec extends CustomsExportsBaseSpec with BeforeAndAfterEach {

  override def beforeEach: Unit =
    reset(mockCustomsInventoryLinkingConnector, mockMovementsRepository)

  trait SetUp {
    val testObj = new SubmissionService(mockCustomsInventoryLinkingConnector, mockMovementsRepository)
    implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  }

  "MovementsService" should {

    "return accepted when connector and persist movements successful" in new SetUp() {
      val xml: NodeSeq = <xmlval><a><b></b></a><a><b></b></a></xmlval>

      withConnectorCall(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId)))
      withMovementSaved(true)

      val result: Result =
        testObj
          .handleMovementSubmission(declarantEoriValue, declarantUcrValue, "movementType", xml)
          .futureValue

      result.header.status must be(ACCEPTED)
      verify(mockCustomsInventoryLinkingConnector).sendInventoryLinkingRequest(any[String], any[NodeSeq])(any())
      verify(mockMovementsRepository).save(any[Submission])
    }

    "return internal server error when connector succeeds but persist movements fails" in new SetUp() {
      val xml: NodeSeq = <xmlval><a><b></b></a><a><b></b></a></xmlval>

      withConnectorCall(CustomsInventoryLinkingResponse(BAD_REQUEST, None))
      withMovementSaved(true)

      val result: Result =
        testObj
          .handleMovementSubmission(declarantEoriValue, declarantUcrValue, "movementType", xml)
          .futureValue

      result.header.status must be(INTERNAL_SERVER_ERROR)
      verify(mockCustomsInventoryLinkingConnector).sendInventoryLinkingRequest(any[String], any[NodeSeq])(any())
      verifyZeroInteractions(mockMovementsRepository)
    }

    "return internal server error when connector succeeds but return no conversation id" in new SetUp() {
      val xml: NodeSeq = <xmlval><a><b></b></a><a><b></b></a></xmlval>

      withConnectorCall(CustomsInventoryLinkingResponse(ACCEPTED, None))

      val result: Result =
        testObj
          .handleMovementSubmission(declarantEoriValue, declarantUcrValue, "movementType", xml)
          .futureValue

      result.header.status must be(INTERNAL_SERVER_ERROR)
      verify(mockCustomsInventoryLinkingConnector).sendInventoryLinkingRequest(any[String], any[NodeSeq])(any())
      verifyZeroInteractions(mockMovementsRepository)
    }

    "return internal server error when connector fails, persist should not be attempted" in new SetUp() {
      val xml: NodeSeq = <xmlval><a><b></b></a><a><b></b></a></xmlval>

      withConnectorCall(CustomsInventoryLinkingResponse(ACCEPTED, Some(conversationId)))
      withMovementSaved(false)

      val result: Result =
        testObj
          .handleMovementSubmission(declarantEoriValue, declarantUcrValue, "movementType", xml)
          .futureValue

      result.header.status must be(INTERNAL_SERVER_ERROR)
      verify(mockCustomsInventoryLinkingConnector).sendInventoryLinkingRequest(any[String], any[NodeSeq])(any())
      verify(mockMovementsRepository).save(any[Submission])
    }
  }

  private def withMovementSaved(result: Boolean): OngoingStubbing[Future[Boolean]] =
    when(mockMovementsRepository.save(any())).thenReturn(Future.successful(result))

}
