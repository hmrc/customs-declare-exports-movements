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

import javax.inject.{Inject, Singleton}
import com.github.tototoshi.csv._
import play.api.{Environment, Logger}
import play.api.libs.json.{JsArray, Json, Reads}

import scala.io.Source
import scala.util.{Failure, Success, Try}

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

@Singleton
class ErrorValidator @Inject() (environment: Environment) {

  private val logger = Logger(this.getClass)

  def hasErrorMessage(error: String): Boolean = {
    val result = errors.map(_.code).contains(error)
    if (!result) logUnknownErrors(error)

    result
  }

  def retrieveCode(error: String): Option[String] =
    errors.map(_.code).find(_ == error)

  private def readErrorsFromFile(source: Source): List[Error] = {
    val reader = CSVReader.open(source)

    val errors: List[List[String]] = reader.all()

    errors.map(Error(_))
  }

  private val ileErrors: List[Error] = {
    val source = Source.fromURL(getClass.getClassLoader.getResource("inventory_linking_exports_errors.json"), "UTF-8")

    readErrorsFromFile(source)
  }

  private def getJsonArrayFromFile[T](file: String, reader: Reads[T]): List[T] = {
    val maybeInputStream = environment.resourceAsStream(file)
    val jsonInputStream = maybeInputStream.getOrElse(throw new Exception(s"$file could not be read!"))

    Try(Json.parse(jsonInputStream)) match {
      case Success(JsArray(jsValues)) =>
        val items = jsValues.toList.map { jsValue =>
          reader.reads(jsValue).asOpt
        }

        if (items.contains(None)) {
          throw new IllegalArgumentException(s"One or more entries could not be parsed in JSON file: '$file'")
        }

        items.flatten

      case Success(_)  => throwError(file)
      case Failure(ex) => throw new IllegalArgumentException(s"Failed to read JSON file: '$file'", ex)
    }
  }

  private def throwError(jsonFile: String) =
    throw new IllegalArgumentException(s"Could not read JSON array from file: '$jsonFile'")

  private val errors: List[Error] = ileErrors

  private def logUnknownErrors(unknownError: String): Unit =
    logger.warn(s"Error code $unknownError is unknown")
}
