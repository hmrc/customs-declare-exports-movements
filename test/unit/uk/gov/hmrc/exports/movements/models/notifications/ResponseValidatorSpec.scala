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

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.exports.movements.models.notifications.ResponseValidator
import utils.CommonTestData.{MessageCodes, ucr, ucr_2}
import utils.NotificationTestData._

import scala.util.Success
import scala.xml.{NodeSeq, SAXParseException}

class ResponseValidatorSpec extends WordSpec with MustMatchers {

  "ResponseValidator on validate" should {

    "return Success" when {

      "provided with correct inventoryLinkingMovementResponse" which {

        "contains all possible nodes" in {
          testSuccessScenario(exampleInventoryLinkingMovementResponseXML)
        }

        "contains only mandatory nodes" in {
          testSuccessScenario(exampleInventoryLinkingMovementResponseMinimalXML)
        }
      }

      "provided with correct inventoryLinkingMovementTotalsResponse" which {

        "contains all possible nodes" in {
          testSuccessScenario(exampleInventoryLinkingMovementTotalsResponseXML)
        }

        "contains only mandatory nodes" in {
          testSuccessScenario(exampleInventoryLinkingMovementTotalsResponseMinimalXML)
        }
      }

      "provided with correct inventoryLinkingControlResponse" which {

        "contains no error node" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.CST}</messageCode>
              <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
              <ucr>
                <ucr>{ucr}</ucr>
                <ucrType>M</ucrType>
              </ucr>
              <movementReference>{movementReference}</movementReference>
            </inventoryLinkingControlResponse>

          testSuccessScenario(response)
        }

        "contains single errorCode" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.CST}</messageCode>
              <actionCode>{actionCode_rejected}</actionCode>
              <ucr>
                <ucr>{ucr}</ucr>
                <ucrType>M</ucrType>
              </ucr>
              <movementReference>{movementReference}</movementReference>
              <error>
                <errorCode>{errorCode_1}</errorCode>
              </error>
            </inventoryLinkingControlResponse>

          testSuccessScenario(response)
        }

        "contains descriptive errorCode" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.CST}</messageCode>
              <actionCode>{actionCode_rejected}</actionCode>
              <ucr>
                <ucr>{ucr}</ucr>
                <ucrType>M</ucrType>
              </ucr>
              <movementReference>{movementReference}</movementReference>
              <error>
                <errorCode>{errorCodeDescriptive}</errorCode>
              </error>
            </inventoryLinkingControlResponse>

          testSuccessScenario(response)
        }

        "contains multiple errorCodes" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.CST}</messageCode>
              <actionCode>{actionCode_rejected}</actionCode>
              <ucr>
                <ucr>{ucr}</ucr>
                <ucrType>M</ucrType>
              </ucr>
              <movementReference>{movementReference}</movementReference>
              <error>
                <errorCode>{errorCode_1}</errorCode>
              </error>
              <error>
                <errorCode>{errorCodeDescriptive}</errorCode>
              </error>
              <error>
                <errorCode>{errorCode_2}</errorCode>
              </error>
            </inventoryLinkingControlResponse>

          testSuccessScenario(response)
        }

        "contains only mandatory nodes" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.CST}</messageCode>
              <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
            </inventoryLinkingControlResponse>

          testSuccessScenario(response)
        }
      }

      def testSuccessScenario(input: NodeSeq): Unit = {
        val validator = new ResponseValidator()

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
          val response =
            <inventoryLinkingMovementResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
            </inventoryLinkingMovementResponse>

          testFailureScenario(response)
        }

        "contains incorrect messageCode" in {
          val response =
            <inventoryLinkingMovementResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.EAC}</messageCode>
            </inventoryLinkingMovementResponse>

          testFailureScenario(response)
        }
      }

      "provided with inventoryLinkingMovementTotalsResponse" which {

        "contains no messageCode node" in {
          val response =
            <inventoryLinkingMovementTotalsResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <goodsLocation>{goodsLocation}</goodsLocation>
            </inventoryLinkingMovementTotalsResponse>

          testFailureScenario(response)
        }

        "contains no goodsLocation node" in {
          val response =
            <inventoryLinkingMovementTotalsResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.ERS}</messageCode>
            </inventoryLinkingMovementTotalsResponse>

          testFailureScenario(response)
        }

        "contains incorrect messageCode" in {
          val response =
            <inventoryLinkingMovementTotalsResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.EAC}</messageCode>
              <goodsLocation>{goodsLocation}</goodsLocation>
            </inventoryLinkingMovementTotalsResponse>

          testFailureScenario(response)
        }

        "contains incorrect goodsLocation" in {
          val response =
            <inventoryLinkingMovementTotalsResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.ERS}</messageCode>
              <goodsLocation>GoodsLocationThatIsTooLong</goodsLocation>
            </inventoryLinkingMovementTotalsResponse>

          testFailureScenario(response)
        }

        "contains entry node without ucrBlock node" in {
          val response =
            <inventoryLinkingMovementTotalsResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.ERS}</messageCode>
              <goodsLocation>{goodsLocation}</goodsLocation>
              <entry>
                <entryStatus>
                  <ics>7</ics>
                  <roe>6</roe>
                  <soe>3</soe>
                </entryStatus>
                <submitRole>{submitRole}</submitRole>
                <goodsItem>
                  <commodityCode>{commodityCode_1}</commodityCode>
                  <totalPackages>{totalPackages_1}</totalPackages>
                  <totalNetMass>{totalNetMass_1}</totalNetMass>
                </goodsItem>
              </entry>
            </inventoryLinkingMovementTotalsResponse>

          testFailureScenario(response)
        }
      }

      "provided with inventoryLinkingControlResponse" which {

        "contains no messageCode node" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
            </inventoryLinkingControlResponse>

          testFailureScenario(response)
        }

        "contains no actionCode node" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.CST}</messageCode>
            </inventoryLinkingControlResponse>

            testFailureScenario(response)
        }

        "contains incorrect actionCode" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.CST}</messageCode>
              <actionCode>12</actionCode>
            </inventoryLinkingControlResponse>

            testFailureScenario(response)
        }

        "contains 2 ucrBlock nodes" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.CST}</messageCode>
              <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
              <ucr>
                <ucr>{ucr}</ucr>
                <ucrType>M</ucrType>
              </ucr>
              <ucr>
                <ucr>{ucr_2}</ucr>
                <ucrType>D</ucrType>
              </ucr>
            </inventoryLinkingControlResponse>

            testFailureScenario(response)
        }

        "contains 2 movementReference nodes" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.CST}</messageCode>
              <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
              <movementReference>{movementReference}</movementReference>
              <movementReference>{movementReference}</movementReference>
            </inventoryLinkingControlResponse>

            testFailureScenario(response)
        }

        "contains multiple errorCode nodes inside error node" in {
          val response =
            <inventoryLinkingControlResponse
                xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
                xmlns="http://gov.uk/customs/inventoryLinking/v1">
              <messageCode>{MessageCodes.CST}</messageCode>
              <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
              <error>
                <errorCode>{errorCodeDescriptive}</errorCode>
                <errorCode>{errorCode_1}</errorCode>
                <errorCode>{errorCode_2}</errorCode>
                <errorCode>{errorCode_3}</errorCode>
              </error>
            </inventoryLinkingControlResponse>

            testFailureScenario(response)
        }
      }

      def testFailureScenario(input: NodeSeq): Unit = {
        val validator = new ResponseValidator()

        validator.validate(input).isFailure must be(true)
        validator.validate(input).failed.get mustBe a[SAXParseException]
      }
    }
  }

}
