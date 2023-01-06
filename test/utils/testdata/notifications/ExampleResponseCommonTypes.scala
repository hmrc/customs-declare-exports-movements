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

package testdata.notifications

import testdata.CommonTestData.{mucr, ucr}
import testdata.notifications.ExampleXmlAndDomainModelPair.{ExampleEntryStatusPair, ExampleGoodsItemPair, ExampleUcrBlockPair}
import testdata.notifications.NotificationTestData.{commodityCode_1, totalNetMass_1, totalPackages_1}
import uk.gov.hmrc.exports.movements.models.notifications.standard.{EntryStatus, GoodsItem, UcrBlock}

object ExampleResponseCommonTypes {

  object Correct {

    lazy val DucrBlock = ExampleUcrBlockPair(
      asXml = <ucrBlock>
        <ucr>{ucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>,
      asDomainModel = UcrBlock(ucr = ucr, ucrType = "D")
    )

    lazy val MucrBlock = ExampleUcrBlockPair(
      asXml = <ucrBlock>
        <ucr>{mucr}</ucr>
        <ucrType>M</ucrType>
      </ucrBlock>,
      asDomainModel = UcrBlock(ucr = mucr, ucrType = "M")
    )

    lazy val EntryStatusAll = ExampleEntryStatusPair(
      asXml = <entryStatus>
          <ics>7</ics>
          <roe>6</roe>
          <soe>3</soe>
        </entryStatus>,
      asDomainModel = EntryStatus(ics = Some("7"), roe = Some("6"), soe = Some("3"))
    )

    lazy val GoodsItemAll = ExampleGoodsItemPair(
      asXml = <goodsItem>
        <commodityCode>{commodityCode_1}</commodityCode>
        <totalPackages>{totalPackages_1}</totalPackages>
        <totalNetMass>{totalNetMass_1}</totalNetMass>
      </goodsItem>,
      asDomainModel =
        GoodsItem(commodityCode = Some(commodityCode_1), totalPackages = Some(totalPackages_1), totalNetMass = Some(BigDecimal(totalNetMass_1)))
    )
  }

}
