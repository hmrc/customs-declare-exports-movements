import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val wireMockVersion = "2.24.1"
  private val testScope = "test,it"

  val compile = Seq(
    "uk.gov.hmrc"                     %% "simple-reactivemongo"       % "7.30.0-play-26",
    ws,
    "uk.gov.hmrc"                     %% "bootstrap-backend-play-26"  % "2.24.0",
    "uk.gov.hmrc"                     %% "wco-dec"                    % "0.35.0",
    "uk.gov.hmrc"                     %% "logback-json-logger"        % "4.8.0",
    "com.typesafe.play"               %% "play-json-joda"             % "2.6.13",
    "com.github.tototoshi"            %% "scala-csv"                  % "1.3.6",
    "uk.gov.hmrc"                     %% "play-json-union-formatter"  % "1.10.0-play-26",
    "com.github.cloudyrock.mongock"   %  "mongock-core"               % "2.0.2",
    "org.mongodb"                     %  "mongo-java-driver"          % "3.12.2"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"          % "3.0.8"             % "test",
    "org.scalatestplus.play"  %% "scalatestplus-play" % "3.1.3"             % "test",
    "org.pegdown"             %  "pegdown"            % "1.6.0"             % "test",
    "com.typesafe.play"       %% "play-test"          % PlayVersion.current % "test",
    "com.github.tomakehurst"  %  "wiremock"           % wireMockVersion     % testScope,
    "org.mockito"             %  "mockito-core"       % "3.5.7"             % "test"
  )

  val jettyVersion = "9.2.26.v20180806"

  val jettyOverrides = Set(
    "org.eclipse.jetty" % "jetty-server" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-security" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-servlets" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-continuation" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-xml" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-client" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-http" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-io" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-util" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty.websocket" % "websocket-api" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty.websocket" % "websocket-common" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty.websocket" % "websocket-client" % jettyVersion % IntegrationTest
  )
}
