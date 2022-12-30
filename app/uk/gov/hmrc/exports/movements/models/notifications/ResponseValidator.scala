/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.exports.movements.models.notifications

import uk.gov.hmrc.exports.movements.config.AppConfig

import java.io._
import javax.inject.{Inject, Singleton}
import javax.xml.XMLConstants
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{SchemaFactory, ValidatorHandler}
import scala.util.Try
import scala.xml.parsing._
import scala.xml._

@Singleton
class ResponseValidator @Inject() (appConfig: AppConfig) {

  private val XSDFilePath = appConfig.ileSchemasFilePath
  private val schema =
    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(XSDFilePath))

  def validate(xml: NodeSeq): Try[Unit] = {
    implicit def toInputSource(xml: NodeSeq) = new InputSource(new StringReader(xml.toString))

    val validatorHandler: ValidatorHandler = schema.newValidatorHandler()
    validatorHandler.setContentHandler(new NoBindingFactoryAdapter)

    val parserFactory = SAXParserFactory.newInstance
    parserFactory.setNamespaceAware(true);
    val xmlReader = parserFactory.newSAXParser.getXMLReader
    xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true)
    xmlReader.setContentHandler(validatorHandler)

    Try(xmlReader.parse(xml))
  }
}
