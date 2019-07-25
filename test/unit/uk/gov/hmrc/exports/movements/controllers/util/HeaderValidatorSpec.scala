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

package unit.uk.gov.hmrc.exports.movements.controllers.util

import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.exports.movements.controllers.util.HeaderValidator
import uk.gov.hmrc.exports.movements.models._
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec
import utils.MovementsTestData._

class HeaderValidatorSpec extends UnitSpec with MockitoSugar {

  trait SetUp {
    val validator = new HeaderValidator
  }

  "Header Validator" when {

    "header is present" should {

      "return UCR from header when extract is called" in new SetUp {
        val extractedUcr: Option[String] =
          validator.extractUcrHeader(ValidHeaders)
        extractedUcr shouldBe Some(declarantUcrValue)
      }

      "return movementType from header when extract is called" in new SetUp {
        val extractedMovementType: Option[String] =
          validator.extractMovementTypeHeader(ValidHeaders)
        extractedMovementType shouldBe Some("Arrival")
      }

      "return conversationId from header when extract is called" in new SetUp {
        val extractedConversationId: Option[String] =
          validator.extractConversationIdHeader(ValidHeaders)
        extractedConversationId shouldBe Some(conversationId)
      }
    }

    "header is not present" should {

      "return None from header when extract is called (no DUCR)" in new SetUp {
        val extractedDucr: Option[String] = validator.extractUcrHeader(Map.empty)
        extractedDucr shouldBe None
      }

      "return None from header when extract is called (no MovementType)" in new SetUp {
        val extractedMovementType: Option[String] = validator.extractMovementTypeHeader(Map.empty)
        extractedMovementType shouldBe None
      }

      "return None from header when extract is called (no ConversationId)" in new SetUp {
        val extractedConversationId: Option[String] =
          validator.extractConversationIdHeader(Map.empty)
        extractedConversationId shouldBe None
      }
    }
  }

  "Validate Submission Headers" should {

    "return Right of validatedHeaderResponse when validateHeaders is called on valid headers" in new SetUp {
      val result: Either[ErrorResponse, ValidatedHeadersRequest] =
        validator.validateAndExtractMovementSubmissionHeaders(ValidHeaders)
      result should be(Right(ValidatedHeadersRequest(declarantUcrValue, "Arrival")))
    }

    "return Left ErrorResponse when validateHeaders is called with invalid headers" in new SetUp {
      val result: Either[ErrorResponse, ValidatedHeadersRequest] =
        validator.validateAndExtractMovementSubmissionHeaders(Map.empty)
      result shouldBe Left(ErrorResponse.ErrorInvalidPayload)
    }

  }
}
