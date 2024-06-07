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

package uk.gov.hmrc.exports.movements.controllers

import play.api.Logging
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class BodyLoggingAction(parser: BodyParser[NodeSeq])(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) with Logging {

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    val bodySize = request.body match {
      case AnyContentAsRaw(rawBuffer)              => rawBuffer.size
      case AnyContentAsText(text)                  => text.length.toLong
      case AnyContentAsJson(json)                  => json.toString.length.toLong
      case AnyContentAsXml(xml)                    => xml.toString().getBytes.length.toLong
      case AnyContentAsFormUrlEncoded(formData)    => formData.toString.length.toLong
      case AnyContentAsMultipartFormData(formData) => formData.files.map(_.ref.path.toFile.length()).sum
      case _                                       => 0
    }
    if (bodySize > 1024L * 1024L) logger.warn(s"Movements Notification's request body size: $bodySize bytes")

    block(request)
  }
}

object BodyLoggingAction {
  def apply(parser: BodyParser[NodeSeq])(implicit ec: ExecutionContext): BodyLoggingAction =
    new BodyLoggingAction(parser)
}
