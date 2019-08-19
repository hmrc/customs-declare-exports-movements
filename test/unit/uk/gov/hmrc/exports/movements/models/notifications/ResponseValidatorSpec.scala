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

package unit.uk.gov.hmrc.exports.movements.models.notifications

import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpec}
import play.api.Configuration
import play.api.Mode.Test
import uk.gov.hmrc.exports.movements.config.AppConfig
import uk.gov.hmrc.exports.movements.models.notifications.ResponseValidator
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import utils.testdata.CommonTestData.MessageCodes
import utils.testdata.notifications.NotificationTestData._
import utils.testdata.notifications._

import scala.util.Success
import scala.xml.{NodeSeq, SAXParseException}

class ResponseValidatorSpec extends WordSpec with MustMatchers {

  private val DefaultConfiguration = Configuration(
    ConfigFactory.parseString(
      "microservice.services.customs-inventory-linking-exports.schema-file-path=conf/schemas/exports/inventoryLinkingResponseExternal.xsd".stripMargin
    )
  )

  private def appConfig(conf: Configuration = DefaultConfiguration): AppConfig =
    new AppConfig(conf, new ServicesConfig(conf, new RunMode(conf, Test)))

  "ResponseValidator on validate" should {

    "return Success" when {

      "provided with correct inventoryLinkingMovementResponse" which {

        "contains all possible nodes" in {
          val response = ExampleInventoryLinkingMovementResponse.Correct.AllElements.asXml

          testSuccessScenario(response)
        }

        "contains only mandatory nodes" in {
          val response = ExampleInventoryLinkingMovementResponse.Correct.MandatoryElementsOnly.asXml

          testSuccessScenario(response)
        }
      }

      "provided with correct inventoryLinkingMovementTotalsResponse" which {

        "contains all possible nodes" in {
          val response = ExampleInventoryLinkingMovementTotalsResponse.Correct.AllElements.asXml

          testSuccessScenario(response)
        }

        "contains only mandatory nodes" in {
          val response = ExampleInventoryLinkingMovementTotalsResponse.Correct.MandatoryElementsOnly.asXml

          testSuccessScenario(response)
        }
      }

      "provided with correct inventoryLinkingControlResponse" which {

        "contains no error node" in {
          val response = ExampleInventoryLinkingControlResponse.Correct.Acknowledged.asXml

          testSuccessScenario(response)
        }

        "contains single errorCode" in {
          val response = ExampleInventoryLinkingControlResponse.Correct.RejectedSingleError.asXml

          testSuccessScenario(response)
        }

        "contains descriptive errorCode" in {
          val response = ExampleInventoryLinkingControlResponse.Correct.RejectedDescriptiveError.asXml

          testSuccessScenario(response)
        }

        "contains multiple errorCodes" in {
          val response = ExampleInventoryLinkingControlResponse.Correct.RejectedMultipleErrors.asXml

          testSuccessScenario(response)
        }

        "contains only mandatory nodes" in {
          val response = ExampleInventoryLinkingControlResponse.Correct.AcknowledgedMandatoryElementsOnly.asXml

          testSuccessScenario(response)
        }
      }

      def testSuccessScenario(input: NodeSeq): Unit = {
        val validator = new ResponseValidator(appConfig())

        validator.validate(input) must equal(Success((): Unit))
      }
    }

    "return Failure" when {

      "provided with response containing no namespace" in {
        val response =
          <inventoryLinkingControlResponse>
            <messageCode>{MessageCodes.CST}</messageCode>
            <actionCode>{actionCode_rejected}</actionCode>
          </inventoryLinkingControlResponse>

        testFailureScenario(response)
      }

      "provided with response containing wrong namespace" in {
        val response =
          <inventoryLinkingControlResponse
              xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/incorrectAddress"
              xmlns="http://gov.uk/customs/inventoryLinking/incorrectAddress">
            <messageCode>{MessageCodes.CST}</messageCode>
            <actionCode>{actionCode_rejected}</actionCode>
          </inventoryLinkingControlResponse>

        testFailureScenario(response)
      }

      "provided with inventoryLinkingMovementResponse" which {

        "contains no messageCode node" in {
          val response = ExampleInventoryLinkingMovementResponse.Incorrect.NoMessageCode.asXml

          testFailureScenario(response)
        }

        "contains incorrect messageCode" in {
          val response = ExampleInventoryLinkingMovementResponse.Incorrect.WrongMessageCode.asXml

          testFailureScenario(response)
        }
      }

      "provided with inventoryLinkingMovementTotalsResponse" which {

        "contains no messageCode node" in {
          val response = ExampleInventoryLinkingMovementTotalsResponse.Incorrect.NoMessageCode.asXml

          testFailureScenario(response)
        }

        "contains no goodsLocation node" in {
          val response = ExampleInventoryLinkingMovementTotalsResponse.Incorrect.NoGoodsLocation.asXml

          testFailureScenario(response)
        }

        "contains incorrect messageCode" in {
          val response = ExampleInventoryLinkingMovementTotalsResponse.Incorrect.WrongMessageCode.asXml

          testFailureScenario(response)
        }

        "contains incorrect goodsLocation" in {
          val response = ExampleInventoryLinkingMovementTotalsResponse.Incorrect.WrongGoodsLocation.asXml

          testFailureScenario(response)
        }

        "contains entry node without ucrBlock node" in {
          val response = ExampleInventoryLinkingMovementTotalsResponse.Incorrect.EntryNodeWithoutUcrBlockNode.asXml

          testFailureScenario(response)
        }
      }

      "provided with inventoryLinkingControlResponse" which {

        "contains no messageCode node" in {
          val response = ExampleInventoryLinkingControlResponse.Incorrect.NoMessageCode.asXml

          testFailureScenario(response)
        }

        "contains no actionCode node" in {
          val response = ExampleInventoryLinkingControlResponse.Incorrect.NoActionCode.asXml

          testFailureScenario(response)
        }

        "contains incorrect actionCode" in {
          val response = ExampleInventoryLinkingControlResponse.Incorrect.WrongActionCode.asXml

          testFailureScenario(response)
        }

        "contains 2 ucrBlock nodes" in {
          val response = ExampleInventoryLinkingControlResponse.Incorrect.DoubleUcrBlock.asXml

          testFailureScenario(response)
        }

        "contains 2 movementReference nodes" in {
          val response = ExampleInventoryLinkingControlResponse.Incorrect.DoubleMovementReference.asXml

          testFailureScenario(response)
        }

        "contains multiple errorCode nodes inside error node" in {
          val response = ExampleInventoryLinkingControlResponse.Incorrect.MultipleErrorCodesInsideErrorNode.asXml

          testFailureScenario(response)
        }
      }

      def testFailureScenario(input: NodeSeq): Unit = {
        val validator = new ResponseValidator(appConfig())

        validator.validate(input).isFailure must be(true)
        validator.validate(input).failed.get mustBe a[SAXParseException]
      }
    }
  }

}