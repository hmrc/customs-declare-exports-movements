import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "simple-reactivemongo" % "7.19.0-play-26",
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.39.0",
    "uk.gov.hmrc" %% "wco-dec" % "0.30.0",
    "uk.gov.hmrc" %% "logback-json-logger" % "4.6.0",
    "com.typesafe.play" %% "play-json-joda" % "2.6.10"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.8.0-play-26" % scope,
    "org.scalatest" %% "scalatest" % "3.0.5" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.mockito" % "mockito-core" % "2.27.0" % "test"
  )
}
