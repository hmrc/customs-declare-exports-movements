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

package uk.gov.hmrc.exports.movements.models.notifications.parsers

import javax.inject.Inject
import uk.gov.hmrc.exports.movements.models.XmlTags
import uk.gov.hmrc.exports.movements.models.notifications.standard.{Entry, StandardNotificationData, UcrBlock}

import scala.xml.NodeSeq

class ControlResponseParser @Inject() (errorValidator: ErrorValidator) extends ResponseParser[StandardNotificationData] {

  override val responseTypeIle: String = "inventoryLinkingControlResponse"

  override def parse(responseXml: NodeSeq): StandardNotificationData = StandardNotificationData(
    messageCode = StringOption((responseXml \ XmlTags.messageCode).text),
    actionCode = StringOption((responseXml \ XmlTags.actionCode).text),
    entries = (responseXml \ XmlTags.ucr).map { ucrNode =>
      Entry(ucrBlock =
        Some(
          UcrBlock(
            ucr = (ucrNode \ XmlTags.ucr).text,
            ucrPartNo = StringOption((ucrNode \ XmlTags.ucrPartNo).text),
            ucrType = (ucrNode \ XmlTags.ucrType).text
          )
        )
      )
    },
    movementReference = StringOption((responseXml \ XmlTags.movementReference).text),
    errorCodes = (responseXml \ XmlTags.error \ XmlTags.errorCode).flatMap { error =>
      val errorTxt = error.text

      if (errorValidator.hasErrorMessage(errorTxt))
        errorValidator.retrieveCode(errorTxt)
      else
        Some(errorTxt)
    },
    responseType = responseTypeIle
  )
}
