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

package uk.gov.hmrc.exports.movements.controllers

import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.exports.movements.base.ExportsTestData
import uk.gov.hmrc.exports.movements.models._
import uk.gov.hmrc.play.test.UnitSpec

class HeaderValidatorSpec extends UnitSpec with MockitoSugar with ExportsTestData {

  trait SetUp {
    val validator = new HeaderValidator
  }

  "HeaderValidator" should {

    "return lrn from header when extract is called and header is present" in new SetUp {
      val extractedLrn: Option[String] =
        validator.extractLrnHeader(ValidHeaders)
      extractedLrn shouldBe Some(declarantLrnValue)
    }

    "return ducr from header when extract is called and header is present" in new SetUp {
      val extractedDucr: Option[String] =
        validator.extractDucrHeader(ValidHeaders)
      extractedDucr shouldBe Some(declarantDucrValue)
    }

    "return mucr from header when extract is called and header is present" in new SetUp {
      val extractedMucr: Option[String] =
        validator.extractOptionalMucrHeader(ValidHeaders)
      extractedMucr shouldBe Some(declarantMucrValue)
    }

    "return movementType from header when extract is called and header is present" in new SetUp {
      val extractedMovementType: Option[String] =
        validator.extractMovementTypeHeader(ValidHeaders)
      extractedMovementType shouldBe Some("Arrival")
    }

    "return eori from header when extract is called and header is present" in new SetUp {
      val extractedEori: Option[String] =
        validator.extractEoriHeader(ValidHeaders)
      extractedEori shouldBe Some(declarantEoriValue)
    }

    "return authToken from header when extract is called and header is present" in new SetUp {
      val extractedAuthToken: Option[String] =
        validator.extractAuthTokenHeader(ValidHeaders)
      extractedAuthToken shouldBe Some(dummyToken)
    }

    "return conversationId from header when extract is called and header is present" in new SetUp {
      val extractedConversationId: Option[String] =
        validator.extractConversationIdHeader(ValidHeaders)
      extractedConversationId shouldBe Some(conversationId)
    }

    "return None from header when extract is called and LRN header not present" in new SetUp {
      val extractedLrn: Option[String] = validator.extractLrnHeader(Map.empty)
      extractedLrn shouldBe None
    }

    "return None from header when extract is called and DUCR header not present" in new SetUp {
      val extractedDucr: Option[String] = validator.extractDucrHeader(Map.empty)
      extractedDucr shouldBe None
    }

    "return None from header when extract is called and MovementType header not present" in new SetUp {
      val extractedMovementType: Option[String] = validator.extractMovementTypeHeader(Map.empty)
      extractedMovementType shouldBe None
    }

    "return None from header when extract is called and MUCR header not present" in new SetUp {
      val extractedMucr: Option[String] = validator.extractOptionalMucrHeader(Map.empty)
      extractedMucr shouldBe None
    }

    "return None from header when extract is called and EORI header not present" in new SetUp {
      val extractedEori: Option[String] = validator.extractEoriHeader(Map.empty)
      extractedEori shouldBe None
    }

    "return None from header when extract is called and AuthToken header not present" in new SetUp {
      val extractedAuthToken: Option[String] =
        validator.extractAuthTokenHeader(Map.empty)
      extractedAuthToken shouldBe None
    }

    "return None from header when extract is called and conversationId header not present" in new SetUp {
      val extractedConversationId: Option[String] =
        validator.extractConversationIdHeader(Map.empty)
      extractedConversationId shouldBe None
    }

    "validateSubmissionHeaders" should {

      "return Right of validatedHeaderResponse when validateHeaders is called on valid headers" in new SetUp {
        val result: Either[ErrorResponse, ValidatedHeadersMovementsRequest] =
          validator.validateAndExtractMovementSubmissionHeaders(ValidHeaders)
        result should be(
          Right(ValidatedHeadersMovementsRequest(declarantDucrValue, Some(declarantMucrValue), "Arrival"))
        )
      }

      "return Left ErrorResponse when validateHeaders is called with invalid headers" in new SetUp {
        val result: Either[ErrorResponse, ValidatedHeadersMovementsRequest] =
          validator.validateAndExtractMovementSubmissionHeaders(Map.empty)
        result shouldBe Left(ErrorResponse.ErrorInvalidPayload)
      }

    }


    "validateAndExtractMovementNotificationHeaders" should {

      "return Right of MovementNotificationApiRequest when validateHeaders is called on valid headers" in new SetUp {
        val result: Either[ErrorResponse, MovementNotificationApiRequest] =
          validator.validateAndExtractMovementNotificationHeaders(ValidHeaders)
        result should be(
          Right(
            MovementNotificationApiRequest(
              AuthToken(dummyToken),
              ConversationId(conversationId),
              Eori(declarantEoriValue)
            )
          )
        )
      }

      "return Left ErrorResponse when validateHeaders is called with invalid headers" in new SetUp {
        val result: Either[ErrorResponse, MovementNotificationApiRequest] =
          validator.validateAndExtractMovementNotificationHeaders(Map.empty)
        result should be(Left(ErrorResponse.ErrorInvalidPayload))
      }

    }

    "validateAndExtractSubmissionNotificationHeaders" should {

      "return Right of SubmissionNotificationApiRequest when validateHeaders is called on valid headers" in new SetUp {
        val result: Either[ErrorResponse, SubmissionNotificationApiRequest] =
          validator.validateAndExtractSubmissionNotificationHeaders(ValidHeaders)
        result should be(Right(SubmissionNotificationApiRequest(AuthToken(dummyToken), ConversationId(conversationId))))
      }

      "return Left ErrorResponse when validateHeaders is called with invalid headers" in new SetUp {
        val result: Either[ErrorResponse, SubmissionNotificationApiRequest] =
          validator.validateAndExtractSubmissionNotificationHeaders(Map.empty)
        result should be(Left(ErrorResponse.ErrorInvalidPayload))
      }

    }
  }
}
