/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.exports.movements.models.notifications.exchange

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.exports.movements.models.notifications.NotificationData
import uk.gov.hmrc.exports.movements.models.notifications.exchange.IleQueryResponseExchangeType._
import uk.gov.hmrc.exports.movements.models.notifications.queries.{DucrInfo, IleQueryResponseData, MucrInfo}
import uk.gov.hmrc.exports.movements.models.notifications.standard.{StandardNotificationData, UcrBlock}
import uk.gov.hmrc.play.json.Union

sealed trait IleQueryResponseExchangeData {
  val typ: IleQueryResponseExchangeType
}

object IleQueryResponseExchangeData {

  implicit val format: Format[IleQueryResponseExchangeData] = Union
    .from[IleQueryResponseExchangeData]("typ")
    .and[SuccessfulResponseExchangeData](SuccessfulResponseExchange.toString)
    .and[UcrNotFoundResponseExchangeData](UcrNotFoundResponseExchange.toString)
    .format

  def apply(notificationData: NotificationData): IleQueryResponseExchangeData = notificationData match {
    case ileQueryResponseData: IleQueryResponseData         => SuccessfulResponseExchangeData(ileQueryResponseData)
    case standardNotificationData: StandardNotificationData => UcrNotFoundResponseExchangeData(standardNotificationData)
    case other =>
      throw new IllegalStateException(s"Cannot build IleQueryResponseExchangeData from ${other.typ} type")
  }

  case class SuccessfulResponseExchangeData(
    queriedDucr: Option[DucrInfo] = None,
    queriedMucr: Option[MucrInfo] = None,
    parentMucr: Option[MucrInfo] = None,
    childDucrs: Seq[DucrInfo] = Seq.empty,
    childMucrs: Seq[MucrInfo] = Seq.empty
  ) extends IleQueryResponseExchangeData {
    override val typ = IleQueryResponseExchangeType.SuccessfulResponseExchange
  }

  object SuccessfulResponseExchangeData {
    implicit val format: OFormat[SuccessfulResponseExchangeData] = Json.format[SuccessfulResponseExchangeData]

    def apply(ileQueryResponseData: IleQueryResponseData): SuccessfulResponseExchangeData =
      new SuccessfulResponseExchangeData(
        queriedDucr = ileQueryResponseData.queriedDucr,
        queriedMucr = ileQueryResponseData.queriedMucr,
        parentMucr = ileQueryResponseData.parentMucr,
        childDucrs = ileQueryResponseData.childDucrs,
        childMucrs = ileQueryResponseData.childMucrs
      )
  }

  case class UcrNotFoundResponseExchangeData(
    messageCode: String,
    actionCode: String,
    ucrBlock: Option[UcrBlock] = None,
    movementReference: Option[String] = None,
    errorCodes: Seq[String] = Seq.empty
  ) extends IleQueryResponseExchangeData {
    override val typ = IleQueryResponseExchangeType.UcrNotFoundResponseExchange
  }

  object UcrNotFoundResponseExchangeData {
    implicit val format: OFormat[UcrNotFoundResponseExchangeData] = Json.format[UcrNotFoundResponseExchangeData]

    def apply(standardNotificationData: StandardNotificationData): UcrNotFoundResponseExchangeData =
      new UcrNotFoundResponseExchangeData(
        messageCode = standardNotificationData.messageCode.getOrElse(""),
        actionCode = standardNotificationData.actionCode.getOrElse(""),
        ucrBlock = standardNotificationData.entries.flatMap(_.ucrBlock).headOption,
        movementReference = standardNotificationData.movementReference,
        errorCodes = standardNotificationData.errorCodes
      )
  }
}
