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

import play.api.Logger
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.exports.movements.utils.JsonFile

import javax.inject.{Inject, Singleton}

case class Error(code: String, description: String)

object Error {

  implicit val format: OFormat[Error] = Json.format[Error]
  private val logger = Logger(this.getClass)

  def apply(list: List[String]): Error = list match {
    case code :: description :: Nil => Error(code, description)
    case error =>
      logger.warn("Incorrect list with errors. Error: " + error)
      throw new IllegalArgumentException("Error has incorrect structure")
  }
}

@Singleton
class ErrorValidator @Inject() (jsonFile: JsonFile) {

  private val logger = Logger(this.getClass)

  def hasErrorMessage(error: String): Boolean = {
    val result = errors.map(_.code).contains(error)
    if (!result) logUnknownErrors(error)

    result
  }

  def retrieveCode(error: String): Option[String] =
    errors.map(_.code).find(_ == error)

  private val ileErrors: List[Error] = {
    val source = "inventory_linking_exports_errors.json"

    jsonFile.getJsonArrayFromFile(source, Error.format)
  }

  private val errors: List[Error] = ileErrors

  private def logUnknownErrors(unknownError: String): Unit =
    logger.warn(s"Error code $unknownError is unknown")
}
