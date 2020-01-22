/*
 * Copyright 2020 HM Revenue & Customs
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

package utils.testdata.notifications

import uk.gov.hmrc.exports.movements.models.notifications.queries.QueryResponseData
import uk.gov.hmrc.exports.movements.models.notifications.{EntryStatus, GoodsItem, NotificationData, UcrBlock}

import scala.xml.Elem

sealed trait ExampleXmlAndDomainModelPair[T] {
  val asXml: Elem
  val asDomainModel: T
}

object ExampleXmlAndDomainModelPair {

  case class ExampleRegularResponse(asXml: Elem = <empty/>, asDomainModel: NotificationData = NotificationData.empty)
      extends ExampleXmlAndDomainModelPair[NotificationData]

  case class ExampleQueryResponse(asXml: Elem = <empty/>, asDomainModel: QueryResponseData = QueryResponseData())
      extends ExampleXmlAndDomainModelPair[QueryResponseData]

  case class ExampleUcrBlockPair(asXml: Elem = <empty/>, asDomainModel: UcrBlock) extends ExampleXmlAndDomainModelPair[UcrBlock]

  case class ExampleEntryStatusPair(asXml: Elem = <empty/>, asDomainModel: EntryStatus = EntryStatus())
      extends ExampleXmlAndDomainModelPair[EntryStatus]

  case class ExampleGoodsItemPair(asXml: Elem = <empty/>, asDomainModel: GoodsItem = GoodsItem()) extends ExampleXmlAndDomainModelPair[GoodsItem]

}
