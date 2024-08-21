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

package uk.gov.hmrc.exports.movements.models.notifications

import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import play.api.Logging
import uk.gov.hmrc.exports.movements.config.AppConfig

import java.io.StringReader
import javax.inject.{Inject, Singleton}
import javax.xml.XMLConstants
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import scala.collection.mutable.ListBuffer
import scala.xml.factory.XMLLoader
import scala.xml.parsing.NoBindingFactoryAdapter
import scala.xml.{Elem, NodeSeq, SAXParser}

@Singleton
class ResponseValidator @Inject() (appConfig: AppConfig) extends Logging {

  private lazy val schema = {
    val XSDFilePath = appConfig.ileSchemasFilePath
    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(XSDFilePath))
  }

  def validate(xml: NodeSeq): Either[XmlValidationException, Elem] = {
    val xmlErrors = new ListBuffer[XmlValidationError]

    trait XmlErrorCollector extends DefaultHandler {
      override def error(exc: SAXParseException): Unit = xmlErrors += XmlValidationError(exc.getLineNumber, exc.getMessage, "error")
      override def fatalError(exc: SAXParseException): Unit = xmlErrors += XmlValidationError(exc.getLineNumber, exc.getMessage, "fatal")
      override def warning(exc: SAXParseException): Unit = xmlErrors += XmlValidationError(exc.getLineNumber, exc.getMessage)
    }

    val xmlLoader: XMLLoader[Elem] = new XMLLoader[Elem] {
      val parserFactory: SAXParserFactory = SAXParserFactory.newInstance
      parserFactory.setSchema(schema)
      parserFactory.setNamespaceAware(true)
      parserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true)

      override def parser: SAXParser = parserFactory.newSAXParser
      override def adapter = new NoBindingFactoryAdapter with XmlErrorCollector
    }

    val result: Elem = xmlLoader.load(new StringReader(xml.mkString))
    if (xmlErrors.isEmpty) Right(result) else Left(XmlValidationException(xmlErrors.toList))
  }
}

case class XmlValidationError(line: Int, message: String, errorType: String = "warning")

// The 'xmlErrors' argument is empty by default to make easier to create in unit tests instances of the Exception.
case class XmlValidationException(xmlErrors: List[XmlValidationError] = List.empty) extends Exception("") {

  override def getMessage: String =
    xmlErrors.map(xmlError => s"\tAt line(${xmlError.line}): ${xmlError.message}\n").mkString
}
