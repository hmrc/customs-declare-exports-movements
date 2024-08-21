/*
 * Copyright 2023 HM Revenue & Customs
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

package utils.testdata.notifications

import utils.testdata.CommonTestData._
import utils.testdata.notifications.ExampleXmlAndDomainModelPair.ExampleStandardResponse
import utils.testdata.notifications.NotificationTestData._
import uk.gov.hmrc.exports.movements.models.notifications.standard.{Entry, StandardNotificationData, UcrBlock}

object ExampleInventoryLinkingControlResponse {

  object Correct {

    lazy val Acknowledged = ExampleStandardResponse(asXml = <inventoryLinkingControlResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.CST}</messageCode>
          <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
          <ucr>
            <ucr>{ucr}</ucr>
            <ucrType>M</ucrType>
          </ucr>
          <movementReference>{movementReference}</movementReference>
        </inventoryLinkingControlResponse>)

    lazy val AcknowledgedMandatoryElementsOnly = ExampleStandardResponse(asXml = <inventoryLinkingControlResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.CST}</messageCode>
          <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
        </inventoryLinkingControlResponse>)

    lazy val Rejected = ExampleStandardResponse(
      asXml = <inventoryLinkingControlResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.CST}</messageCode>
          <actionCode>{actionCode_rejected}</actionCode>
          <ucr>
            <ucr>{mucr}</ucr>
            <ucrType>M</ucrType>
          </ucr>
          <movementReference>{movementReference}</movementReference>
          <error>
            <errorCode>{errorCode_1}</errorCode>
          </error>
          <error>
            <errorCode>{errorCodeDescriptive}</errorCode>
          </error>
        </inventoryLinkingControlResponse>,
      asDomainModel = StandardNotificationData(
        responseType = "inventoryLinkingControlResponse",
        messageCode = Some(MessageCodes.CST),
        actionCode = Some(actionCode_rejected),
        movementReference = Some(movementReference),
        entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = mucr, ucrType = "M")))),
        errorCodes = Seq(errorCode_1, validatedErrorCodeDescriptive)
      )
    )

    lazy val RejectedWithDucrPart = ExampleStandardResponse(
      asXml = <inventoryLinkingControlResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.CST}</messageCode>
        <actionCode>{actionCode_rejected}</actionCode>
        <ucr>
          <ucr>{ucr}</ucr>
          <ucrType>D</ucrType>
          <ucrPartNo>123</ucrPartNo>
        </ucr>
        <movementReference>{movementReference}</movementReference>
        <error>
          <errorCode>{errorCode_1}</errorCode>
        </error>
        <error>
          <errorCode>{errorCodeDescriptive}</errorCode>
        </error>
      </inventoryLinkingControlResponse>,
      asDomainModel = StandardNotificationData(
        responseType = "inventoryLinkingControlResponse",
        messageCode = Some(MessageCodes.CST),
        actionCode = Some(actionCode_rejected),
        movementReference = Some(movementReference),
        entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ucr, ucrPartNo = Some("123"), ucrType = "D")))),
        errorCodes = Seq(errorCode_1, validatedErrorCodeDescriptive)
      )
    )

    lazy val RejectedSingleError = ExampleStandardResponse(asXml = <inventoryLinkingControlResponse
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
        </inventoryLinkingControlResponse>)

    lazy val RejectedDescriptiveError = ExampleStandardResponse(asXml = <inventoryLinkingControlResponse
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
        </inventoryLinkingControlResponse>)

    lazy val RejectedMultipleErrors = ExampleStandardResponse(
      asXml = <inventoryLinkingControlResponse
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
            <errorCode>{errorCode_2}</errorCode>
          </error>
          <error>
            <errorCode>{errorCode_3}</errorCode>
          </error>
          <error>
            <errorCode>{errorCodeDescriptive}</errorCode>
          </error>
        </inventoryLinkingControlResponse>,
      asDomainModel = StandardNotificationData(
        responseType = "inventoryLinkingControlResponse",
        messageCode = Some(MessageCodes.CST),
        actionCode = Some(actionCode_rejected),
        movementReference = Some(movementReference),
        entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "M")))),
        errorCodes = Seq(errorCode_1, errorCode_2, errorCode_3, validatedErrorCodeDescriptive)
      )
    )

    lazy val AcknowledgedEaaMessageCode = ExampleStandardResponse(
      asXml = <inventoryLinkingControlResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.EAA}</messageCode>
        <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
        <ucr>
          <ucr>{ucr}</ucr>
          <ucrType>M</ucrType>
        </ucr>
        <movementReference>{movementReference}</movementReference>
      </inventoryLinkingControlResponse>,
      asDomainModel = StandardNotificationData(
        responseType = "inventoryLinkingControlResponse",
        messageCode = Some(MessageCodes.EAA),
        actionCode = Some(actionCode_acknowledgedAndProcessed),
        movementReference = Some(movementReference),
        entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "M"))))
      )
    )

    lazy val AcknowledgedQueMessageCode = ExampleStandardResponse(
      asXml = <inventoryLinkingControlResponse
    xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
    xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>{MessageCodes.QUE}</messageCode>
      <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
      <ucr>
        <ucr>{ucr}</ucr>
        <ucrType>M</ucrType>
      </ucr>
      <movementReference>{movementReference}</movementReference>
    </inventoryLinkingControlResponse>,
      asDomainModel = StandardNotificationData(
        responseType = "inventoryLinkingControlResponse",
        messageCode = Some(MessageCodes.QUE),
        actionCode = Some(actionCode_acknowledgedAndProcessed),
        movementReference = Some(movementReference),
        entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "M"))))
      )
    )
  }

  object Incorrect {

    lazy val NoMessageCode = ExampleStandardResponse(asXml = <inventoryLinkingControlResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
      </inventoryLinkingControlResponse>)

    lazy val NoActionCode = ExampleStandardResponse(asXml = <inventoryLinkingControlResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.CST}</messageCode>
      </inventoryLinkingControlResponse>)

    lazy val WrongActionCode = ExampleStandardResponse(asXml = <inventoryLinkingControlResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.CST}</messageCode>
        <actionCode>12</actionCode>
      </inventoryLinkingControlResponse>)

    lazy val DoubleUcrBlock = ExampleStandardResponse(asXml = <inventoryLinkingControlResponse
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
      </inventoryLinkingControlResponse>)

    lazy val DoubleMovementReference = ExampleStandardResponse(asXml = <inventoryLinkingControlResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.CST}</messageCode>
        <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
        <movementReference>{movementReference}</movementReference>
        <movementReference>{movementReference}</movementReference>
      </inventoryLinkingControlResponse>)

    lazy val MultipleErrorCodesInsideErrorNode = ExampleStandardResponse(asXml = <inventoryLinkingControlResponse
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
      </inventoryLinkingControlResponse>)
  }
}
