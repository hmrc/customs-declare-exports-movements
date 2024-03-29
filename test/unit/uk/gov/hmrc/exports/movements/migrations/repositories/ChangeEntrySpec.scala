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

package uk.gov.hmrc.exports.movements.migrations.repositories

import java.util.Date

import org.bson.Document
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.javaapi.CollectionConverters.asJava

class ChangeEntrySpec extends AnyWordSpec with Matchers {

  "ChangeEntry on buildFullDBObject" should {

    "convert to correct Document" in {

      val date = new Date()
      val changeEntry = ChangeEntry(changeId = "changeIdValue", author = "authorValue", timestamp = date, changeLogClass = "changeLogClassValue")
      val expectedOutput =
        new Document(
          asJava(Map("changeId" -> "changeIdValue", "author" -> "authorValue", "timestamp" -> date, "changeLogClass" -> "changeLogClassValue"))
        )

      changeEntry.buildFullDBObject mustBe expectedOutput
    }
  }
}
