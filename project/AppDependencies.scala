import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val testScope = "test,it"

  val compile = Seq(
    "uk.gov.hmrc"                       %% "simple-reactivemongo"           % "8.0.0-play-28",
    "uk.gov.hmrc"                       %% "bootstrap-backend-play-28"      % "5.3.0",
    "uk.gov.hmrc"                       %% "wco-dec"                        % "0.35.0" excludeAll ExclusionRule("com.fasterxml.jackson.dataformat"),
    "uk.gov.hmrc"                       %% "logback-json-logger"            % "5.1.0",
    "com.typesafe.play"                 %% "play-json-joda"                 % "2.6.13",
    "com.github.tototoshi"              %% "scala-csv"                      % "1.3.6",
    "uk.gov.hmrc"                       %% "play-json-union-formatter"      % "1.13.0-play-27",
    "com.github.cloudyrock.mongock"     %  "mongock-core"                   % "2.0.2",
    "org.mongodb.scala"                 %% "mongo-scala-driver"             % "2.9.0",
    "com.fasterxml.jackson.module"      %% "jackson-module-scala"           % "2.12.3",
    "com.fasterxml.jackson.dataformat"  % "jackson-dataformat-xml"          % "2.12.3",
    "com.fasterxml.jackson.dataformat"  % "jackson-dataformat-properties"   % "2.12.3"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"          % "3.2.9"             % testScope,
    "org.scalatestplus.play"  %% "scalatestplus-play" % "5.1.0"             % testScope,
    "com.typesafe.play"       %% "play-test"          % PlayVersion.current % testScope,
    "org.scalatestplus"       %% "mockito-3-4"        % "3.2.9.0"           % testScope,
    "com.vladsch.flexmark"    %  "flexmark-all"       % "0.36.8"            % testScope,
    "com.github.tomakehurst"  %  "wiremock"           % "2.27.2"            % testScope
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
