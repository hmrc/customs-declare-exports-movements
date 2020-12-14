package uk.gov.hmrc.exports.movements.migrations.changelogs

import com.fasterxml.jackson.databind.ObjectMapper
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import stubs.TestMongoDB.mongoConfiguration

trait ChangeLogsBaseSpec extends WordSpec with MustMatchers with GuiceOneServerPerSuite with BeforeAndAfterEach {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().disable[com.kenshoo.play.metrics.PlayModule].configure(mongoConfiguration).build()

  def compareJson(actual: String, expected: String): Unit = {
    val mapper = new ObjectMapper

    val jsonActual = mapper.readTree(actual)
    val jsonExpected = mapper.readTree(expected)

    jsonActual mustBe jsonExpected
  }
}
