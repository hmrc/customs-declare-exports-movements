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

package uk.gov.hmrc.exports.movements.models.notifications.parsers

import java.util.regex.Pattern

import com.github.tototoshi.csv._
import play.api.Logger

import scala.io.Source

case class Error(code: String, description: String)

object Error {

  private val logger = Logger(this.getClass)

  def apply(list: List[String]): Error = list match {
    case code :: description :: Nil => Error(code, description)
    case error =>
      logger.warn("Incorrect list with errors. Error: " + error)
      throw new IllegalArgumentException("Error has incorrect structure")
  }
}

object ErrorValidator {

  private val logger = Logger(this.getClass)

  def validateErrors(errors: Seq[String]): Seq[String] = {

    val correctIleErrors = errors.filter(ileErrors.map(_.code).contains(_))

    val retrievedChiefErrors =
      errors.diff(correctIleErrors).map(retrieveChiefErrorCode).filter(_.isDefined).map(_.get)

    val correctChiefErrors = retrievedChiefErrors.filter(chiefErrors.map(_.code).contains(_))

    val result = correctIleErrors ++ correctChiefErrors

    errors.diff(result).foreach(logUnknownErrors)

    result
  }

  /**
   * CHIEF errors start with capital E following by 3-5 digits.
   * Error is inside whole error message e.g. "6 E408 Unique Consignment reference does not exist"
   */
  private val chiefErrorPattern = Pattern.compile(s"^[E][0-9]{3,5}$$")

  private def readErrorsFromFile(source: Source): List[Error] = {
    val reader = CSVReader.open(source)

    val errors: List[List[String]] = reader.all()

    errors.map(Error(_))
  }

  private val ileErrors: List[Error] = {
    val source = Source.fromURL(getClass.getClassLoader.getResource("inventory_linking_exports_errors.csv"), "UTF-8")

    readErrorsFromFile(source)
  }

  private val chiefErrors: List[Error] = {
    val source = Source.fromURL(getClass.getClassLoader.getResource("chief_errors.csv"), "UTF-8")

    readErrorsFromFile(source)
  }

  private def retrieveChiefErrorCode(errorMessage: String): Option[String] =
    errorMessage.split(" ").find(chiefErrorPattern.matcher(_).matches)

  private def logUnknownErrors(unknownError: String): Unit =
    logger.warn(s"Error code $unknownError is unknown")
}
