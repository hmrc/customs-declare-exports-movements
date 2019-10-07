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
import uk.gov.hmrc.exports.movements.models.consolidation.Consolidation

import scala.xml.{Node, NodeSeq}

@Singleton
class ILEMapper {

  def generateConsolidationXml(consolidation: Consolidation): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{consolidation.consolidationType.toString}</messageCode>
        {buildMasterUcrNode(consolidation.mucrOpt)}
        {buildUcrBlockNode(consolidation.ducrOpt)}
      </inventoryLinkingConsolidationRequest>
    }

  private def buildMasterUcrNode(mucrOpt: Option[String]): NodeSeq =
    mucrOpt.map(mucr => <masterUCR>{mucr}</masterUCR>).getOrElse(NodeSeq.Empty)

  private def buildUcrBlockNode(ducrOpt: Option[String]): NodeSeq =
    ducrOpt.map { ducr =>
      <ucrBlock>
        <ucr>{ducr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
    }.getOrElse(NodeSeq.Empty)
}
