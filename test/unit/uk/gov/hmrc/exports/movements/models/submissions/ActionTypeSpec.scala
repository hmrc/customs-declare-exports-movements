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

package uk.gov.hmrc.exports.movements.models.submissions

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsString}
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.ConsolidationType._
import uk.gov.hmrc.exports.movements.models.submissions.ActionType.MovementType._

class ActionTypeSpec extends AnyWordSpec with Matchers {

  "ActionType" should {

    "write to JSON format" when {

      "it is Arrival" in {
        val json = ActionType.format.writes(Arrival)
        val expectedJson = JsString("Arrival")

        json must equal(expectedJson)
      }

      "it is Retrospective Arrival" in {
        val json = ActionType.format.writes(RetrospectiveArrival)
        val expectedJson = JsString("RetrospectiveArrival")

        json must equal(expectedJson)
      }

      "it is Departure" in {
        val json = ActionType.format.writes(Departure)
        val expectedJson = JsString("Departure")

        json must equal(expectedJson)
      }

      "it is Create Empty Mucr" in {
        val json = ActionType.format.writes(CreateEmptyMucr)
        val expectedJson = JsString("CreateEmptyMucr")

        json must equal(expectedJson)
      }

      "it is DUCR Association" in {
        val json = ActionType.format.writes(DucrAssociation)
        val expectedJson = JsString("DucrAssociation")

        json must equal(expectedJson)
      }

      "it is MUCR Association" in {
        val json = ActionType.format.writes(MucrAssociation)
        val expectedJson = JsString("MucrAssociation")

        json must equal(expectedJson)
      }

      "it is DUCR Disassociation" in {
        val json = ActionType.format.writes(DucrDisassociation)
        val expectedJson = JsString("DucrDisassociation")

        json must equal(expectedJson)
      }

      "it is MUCR Disassociation" in {
        val json = ActionType.format.writes(MucrDisassociation)
        val expectedJson = JsString("MucrDisassociation")

        json must equal(expectedJson)
      }

      "it is Shut MUCR" in {
        val json = ActionType.format.writes(ShutMucr)
        val expectedJson = JsString("ShutMucr")

        json must equal(expectedJson)
      }
    }

    "read from Json format" when {

      "it is Arrival" in {
        val actionType = ActionType.format.reads(JsString("Arrival")).get
        val expectedActionType = Arrival

        actionType must equal(expectedActionType)
      }

      "it is Retrospective Arrival" in {
        val actionType = ActionType.format.reads(JsString("RetrospectiveArrival")).get
        val expectedActionType = RetrospectiveArrival

        actionType must equal(expectedActionType)
      }

      "it is Departure" in {
        val actionType = ActionType.format.reads(JsString("Departure")).get
        val expectedActionType = Departure

        actionType must equal(expectedActionType)
      }

      "it is Create Empty Mucr" in {
        val actionType = ActionType.format.reads(JsString("CreateEmptyMucr")).get
        val expectedActionType = CreateEmptyMucr

        actionType must equal(expectedActionType)
      }

      "it is DUCR Association" in {
        val actionType = ActionType.format.reads(JsString("DucrAssociation")).get
        val expectedActionType = DucrAssociation

        actionType must equal(expectedActionType)
      }

      "it is MUCR Association" in {
        val actionType = ActionType.format.reads(JsString("MucrAssociation")).get
        val expectedActionType = MucrAssociation

        actionType must equal(expectedActionType)
      }

      "it is DUCR Disassociation" in {
        val actionType = ActionType.format.reads(JsString("DucrDisassociation")).get
        val expectedActionType = DucrDisassociation

        actionType must equal(expectedActionType)
      }

      "it is MUCR Disassociation" in {
        val actionType = ActionType.format.reads(JsString("MucrDisassociation")).get
        val expectedActionType = MucrDisassociation

        actionType must equal(expectedActionType)
      }

      "it is Shut MUCR" in {
        val actionType = ActionType.format.reads(JsString("ShutMucr")).get
        val expectedActionType = ShutMucr

        actionType must equal(expectedActionType)
      }

      "it is Unknown" in {
        val result = ActionType.format.reads(JsString("Unknown"))
        val expectedResult = JsError("Unknown ActionType: [\"Unknown\"]")

        result must equal(expectedResult)
      }
    }
  }

}
