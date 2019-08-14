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

package uk.gov.hmrc.exports.movements.models.notifications

import java.io.{File, StringReader}

import javax.inject.Singleton
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{SchemaFactory, ValidatorHandler}
import org.xml.sax.helpers.XMLReaderFactory

import scala.util.Try
import scala.xml.parsing._
import scala.xml.{NodeSeq, _}

@Singleton
class ResponseValidator {

  private val xsdPath = "conf/schemas/exports/inventoryLinkingResponseExternal.xsd"

  def validate(xml: NodeSeq): Try[Unit] = {
    implicit def toInputSource(xml: NodeSeq) = new InputSource(new StringReader(xml.toString))

    // XSD file cannot be read as stream, cause the validator won't find root element definition.
    val xsdFile = new File(xsdPath)
    val schemaLang = XMLConstants.W3C_XML_SCHEMA_NS_URI
    val xsdStream = new StreamSource(xsdFile)
    val schema = SchemaFactory.newInstance(schemaLang).newSchema(xsdStream)

    val validatorHandler = schema.newValidatorHandler()
    validatorHandler.setContentHandler(new NoBindingFactoryAdapter)

    val xmlReader = XMLReaderFactory.createXMLReader()
    xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true)
    xmlReader.setContentHandler(validatorHandler)

    Try(xmlReader.parse(xml))
  }

  private def createValidatorHandler(): ValidatorHandler = ???

}