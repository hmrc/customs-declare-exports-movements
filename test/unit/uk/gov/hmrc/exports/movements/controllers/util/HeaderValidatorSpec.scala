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
import unit.uk.gov.hmrc.exports.movements.base.UnitSpec
import utils.CommonTestData.{ValidHeaders, conversationId}

class HeaderValidatorSpec extends UnitSpec with MockitoSugar {

  trait SetUp {
    val validator = new HeaderValidator
  }

  "Header Validator" when {

    "header is present" should {

      "return conversationId from header when extract is called" in new SetUp {
        val extractedConversationId: Option[String] =
          validator.extractConversationIdHeader(ValidHeaders)
        extractedConversationId shouldBe Some(conversationId)
      }
    }

    "header is not present" should {

      "return None from header when extract is called (no ConversationId)" in new SetUp {
        val extractedConversationId: Option[String] =
          validator.extractConversationIdHeader(Map.empty)
        extractedConversationId shouldBe None
      }
    }
  }

}
