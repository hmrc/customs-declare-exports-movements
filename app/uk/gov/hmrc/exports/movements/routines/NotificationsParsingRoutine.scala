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

package uk.gov.hmrc.exports.movements.routines

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.exports.movements.services.NotificationService

import scala.concurrent.Future

@Singleton
class NotificationsParsingRoutine @Inject() (sotificationService: NotificationService)(implicit rec: RoutinesExecutionContext) extends Routine {

  private val logger = Logger(this.getClass)

  override def execute(): Future[Unit] = {
    logger.info("Starting NotificationsParsingRoutine")
    sotificationService.parseUnparsedNotifications
      .map(_ => logger.info("Finished NotificationsParsingRoutine"))
  }

}
