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

package uk.gov.hmrc.exports.movements.models

import play.api.libs.json._
import uk.gov.hmrc.wco.dec.Response

sealed trait ExportStatus

object ExportStatus {

  implicit object StatusFormat extends Format[ExportStatus] {
    def reads(status: JsValue): JsResult[ExportStatus] = status match {
      case JsString("Pending") => JsSuccess(Pending)
      case JsString("Cancellation Requested") =>
        JsSuccess(RequestedCancellation)
      case JsString("01")   => JsSuccess(Accepted)
      case JsString("02")   => JsSuccess(Received)
      case JsString("03")   => JsSuccess(Rejected)
      case JsString("05")   => JsSuccess(UndergoingPhysicalCheck)
      case JsString("06")   => JsSuccess(AdditionalDocumentsRequired)
      case JsString("07")   => JsSuccess(Amended)
      case JsString("08")   => JsSuccess(Released)
      case JsString("09")   => JsSuccess(Cleared)
      case JsString("10")   => JsSuccess(Cancelled)
      case JsString("1139") => JsSuccess(CustomsPositionGranted)
      case JsString("1141") => JsSuccess(CustomsPositionDenied)
      case JsString("16")   => JsSuccess(GoodsHaveExitedTheCommunity)
      case JsString("17")   => JsSuccess(DeclarationHandledExternally)
      case JsString("18")   => JsSuccess(AwaitingExitResults)
      case _                => JsSuccess(UnknownExportStatus)
    }

    def writes(status: ExportStatus): JsValue = status match {
      case Pending                      => JsString("Pending")
      case RequestedCancellation        => JsString("Cancellation Requested")
      case Accepted                     => JsString("01")
      case Received                     => JsString("02")
      case Rejected                     => JsString("03")
      case UndergoingPhysicalCheck      => JsString("05")
      case AdditionalDocumentsRequired  => JsString("06")
      case Amended                      => JsString("07")
      case Released                     => JsString("08")
      case Cleared                      => JsString("09")
      case Cancelled                    => JsString("10")
      case CustomsPositionGranted       => JsString("1139")
      case CustomsPositionDenied        => JsString("1141")
      case GoodsHaveExitedTheCommunity  => JsString("16")
      case DeclarationHandledExternally => JsString("17")
      case AwaitingExitResults          => JsString("18")
      case UnknownExportStatus          => JsString("UnknownStatus")
    }
  }

  def retrieveFromResponse(response: Response): ExportStatus =
    response.functionCode match {
      case "Pending"                => Pending
      case "Cancellation Requested" => RequestedCancellation
      case "01"                     => Accepted
      case "02"                     => Received
      case "03"                     => Rejected
      case "05"                     => UndergoingPhysicalCheck
      case "06"                     => AdditionalDocumentsRequired
      case "07"                     => Amended
      case "08"                     => Released
      case "09"                     => Cleared
      case "10"                     => Cancelled
      case "11" if response.status.headOption.flatMap(_.nameCode).contains("39") =>
        CustomsPositionGranted
      case "11" if response.status.headOption.flatMap(_.nameCode).contains("41") =>
        CustomsPositionDenied
      case "16" => GoodsHaveExitedTheCommunity
      case "17" => DeclarationHandledExternally
      case "18" => AwaitingExitResults
      case _    => UnknownExportStatus
    }
}

case object Pending extends ExportStatus

case object Accepted extends ExportStatus

case object Received extends ExportStatus

case object Rejected extends ExportStatus

case object UndergoingPhysicalCheck extends ExportStatus {
  override def toString: String = "Undergoing Physical Check"
}

case object AdditionalDocumentsRequired extends ExportStatus {
  override def toString: String = "Additional Documents Required"
}

case object Amended extends ExportStatus

case object Released extends ExportStatus

case object Cleared extends ExportStatus

case object Cancelled extends ExportStatus

case object RequestedCancellation extends ExportStatus {
  override def toString: String = "Cancellation Requested"
}

case object CustomsPositionGranted extends ExportStatus {
  override def toString: String = "Customs Position Granted"
}

case object CustomsPositionDenied extends ExportStatus {
  override def toString: String = "Customs Position Denied"
}

case object GoodsHaveExitedTheCommunity extends ExportStatus {
  override def toString: String = "Goods Have Exited The Community"
}

case object DeclarationHandledExternally extends ExportStatus {
  override def toString: String = "Declaration Handled Externally"
}

case object AwaitingExitResults extends ExportStatus {
  override def toString: String = "Awaiting Exit Results"
}

case object UnknownExportStatus extends ExportStatus {
  override def toString: String = "Unknown status"
}
