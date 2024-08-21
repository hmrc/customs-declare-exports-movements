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

package uk.gov.hmrc.exports.movements.repositories

import com.codahale.metrics.SharedMetricRegistries
import org.bson.types.ObjectId
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import utils.testdata.MovementsTestData.dateTimeString
import uk.gov.hmrc.exports.movements.models.notifications.Notification
import utils.stubs.TestMongoDB

import java.time.{Clock, Instant, ZoneOffset}

trait RepositoryISpec
    extends AnyWordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with Matchers with IntegrationPatience with TestMongoDB {
  private val clock = Clock.fixed(Instant.parse(dateTimeString), ZoneOffset.UTC)

  override val fakeApplication: Application = {
    SharedMetricRegistries.clear()
    GuiceApplicationBuilder()
      .overrides(bind[Clock].to(clock))
      .configure(mongoConfiguration)
      .build()
  }

  def equalWithoutId(notification: Notification): Matcher[Notification] = new Matcher[Notification] {
    def actualContentWas(notif: Notification): String =
      if (notif == null) {
        "Element did not exist"
      } else {
        s"\nActual content is:\n${notif}\n"
      }

    override def apply(left: Notification): MatchResult = {
      def compare: Boolean = {
        val id = ObjectId.get
        val leftNoId = left.copy(_id = id)
        val rightNoId = notification.copy(_id = id)

        leftNoId == rightNoId
      }

      MatchResult(
        left != null && compare,
        s"Notification is not equal to {$notification}\n${actualContentWas(left)}",
        s"Notification is equal to: {$notification}"
      )
    }
  }
}
