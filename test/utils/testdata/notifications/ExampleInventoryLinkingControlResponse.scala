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

package utils.testdata.notifications

import uk.gov.hmrc.exports.movements.models.notifications.{Entry, NotificationData, UcrBlock}
import utils.testdata.CommonTestData._
import utils.testdata.notifications.ExampleXmlAndDomainModelPair.ExampleRegularResponse
import utils.testdata.notifications.NotificationTestData._

object ExampleInventoryLinkingControlResponse {

  object Correct {

    lazy val Acknowledged = ExampleRegularResponse(asXml = <inventoryLinkingControlResponse
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

    lazy val AcknowledgedMandatoryElementsOnly = ExampleRegularResponse(asXml = <inventoryLinkingControlResponse
        xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
        xmlns="http://gov.uk/customs/inventoryLinking/v1">
          <messageCode>{MessageCodes.CST}</messageCode>
          <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
        </inventoryLinkingControlResponse>)

    lazy val Rejected = ExampleRegularResponse(
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
            <errorCode>{errorCodeDescriptive}</errorCode>
          </error>
        </inventoryLinkingControlResponse>,
      asDomainModel = NotificationData(
        messageCode = Some(MessageCodes.CST),
        actionCode = Some(actionCode_rejected),
        movementReference = Some(movementReference),
        entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "M")))),
        errorCodes = Seq(errorCode_1, validatedErrorCodeDescriptive)
      )
    )

    lazy val RejectedSingleError = ExampleRegularResponse(asXml = <inventoryLinkingControlResponse
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

    lazy val RejectedDescriptiveError = ExampleRegularResponse(asXml = <inventoryLinkingControlResponse
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

    lazy val RejectedMultipleErrors = ExampleRegularResponse(
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
      asDomainModel = NotificationData(
        messageCode = Some(MessageCodes.CST),
        actionCode = Some(actionCode_rejected),
        movementReference = Some(movementReference),
        entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ucr, ucrType = "M")))),
        errorCodes = Seq(errorCode_1, errorCode_2, errorCode_3, validatedErrorCodeDescriptive)
      )
    )

  }

  object Incorrect {

    lazy val NoMessageCode = ExampleRegularResponse(asXml = <inventoryLinkingControlResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
      </inventoryLinkingControlResponse>)

    lazy val NoActionCode = ExampleRegularResponse(asXml = <inventoryLinkingControlResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.CST}</messageCode>
      </inventoryLinkingControlResponse>)

    lazy val WrongActionCode = ExampleRegularResponse(asXml = <inventoryLinkingControlResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.CST}</messageCode>
        <actionCode>12</actionCode>
      </inventoryLinkingControlResponse>)

    lazy val DoubleUcrBlock = ExampleRegularResponse(asXml = <inventoryLinkingControlResponse
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

    lazy val DoubleMovementReference = ExampleRegularResponse(asXml = <inventoryLinkingControlResponse
      xmlns:ns2="http://gov.uk/customs/inventoryLinking/gatewayHeader/v1"
      xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{MessageCodes.CST}</messageCode>
        <actionCode>{actionCode_acknowledgedAndProcessed}</actionCode>
        <movementReference>{movementReference}</movementReference>
        <movementReference>{movementReference}</movementReference>
      </inventoryLinkingControlResponse>)

    lazy val MultipleErrorCodesInsideErrorNode = ExampleRegularResponse(asXml = <inventoryLinkingControlResponse
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
