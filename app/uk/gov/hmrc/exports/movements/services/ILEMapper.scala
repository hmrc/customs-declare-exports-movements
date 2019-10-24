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

package uk.gov.hmrc.exports.movements.services

import javax.inject.Singleton
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationRequest
import uk.gov.hmrc.exports.movements.models.consolidation.ConsolidationType.{ConsolidationType, _}

import scala.xml.{Node, NodeSeq}

@Singleton
class ILEMapper {

  def generateConsolidationXml(consolidation: ConsolidationRequest): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{buildMessageCode(consolidation.consolidationType)}</messageCode>
        {buildMasterUcrNode(consolidation.mucrOpt)}
        {buildUcrBlockNode(consolidation.consolidationType, consolidation.ucrOpt)}
      </inventoryLinkingConsolidationRequest>
    }

  private def buildMessageCode(consolidationType: ConsolidationType): String = consolidationType match {
    case ASSOCIATE_DUCR | DISASSOCIATE_DUCR | ASSOCIATE_MUCR | DISASSOCIATE_MUCR => "EAC"
    case SHUT_MUCR                                                               => "CST"
  }

  private def buildMasterUcrNode(mucrOpt: Option[String]): NodeSeq =
    mucrOpt.map(mucr => <masterUCR>{mucr}</masterUCR>).getOrElse(NodeSeq.Empty)

  private def buildUcrBlockNode(consolidationType: ConsolidationType, ucrOpt: Option[String]): NodeSeq =
    ucrOpt.map { ducr =>
      <ucrBlock>
        <ucr>{ducr}</ucr>
        <ucrType>{ucrType(consolidationType)}</ucrType>
      </ucrBlock>
    }.getOrElse(NodeSeq.Empty)

  private def ucrType(consolidationType: ConsolidationType): String = consolidationType match {
    case ASSOCIATE_DUCR | DISASSOCIATE_DUCR => "D"
    case ASSOCIATE_MUCR | DISASSOCIATE_MUCR => "M"
  }
}
