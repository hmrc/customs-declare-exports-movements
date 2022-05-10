import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "5.24.0"
  val hmrcMongoVersion = "0.63.0"
  val testScope = "test,it"

  val compile = Seq(
    "uk.gov.hmrc"                       %% "bootstrap-backend-play-28"     % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"                 %% "hmrc-mongo-play-28"            % hmrcMongoVersion,
    "uk.gov.hmrc"                       %% "wco-dec"                       % "0.36.0" excludeAll ExclusionRule("com.fasterxml.jackson.dataformat"),
    "uk.gov.hmrc"                       %% "logback-json-logger"           % "5.2.0",
    "uk.gov.hmrc"                       %% "play-json-union-formatter"     % "1.15.0-play-28",
    "com.typesafe.play"                 %% "play-json-joda"                % "2.9.2",
    "com.github.tototoshi"              %% "scala-csv"                     % "1.3.9",
    "com.fasterxml.jackson.module"      %% "jackson-module-scala"          % "2.13.2",
    "com.fasterxml.jackson.dataformat"  %  "jackson-dataformat-xml"        % "2.13.2",
    "com.fasterxml.jackson.dataformat"  %  "jackson-dataformat-properties" % "2.13.2",
    // Used by the Migration tool. Try to keep it to the same version of mongo-scala-driver.
    "org.mongodb"                       %  "mongodb-driver-sync"           % "4.5.1"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % bootstrapPlayVersion % testScope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % hmrcMongoVersion     % testScope,
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.9.0"            % testScope,
    "com.vladsch.flexmark"   %  "flexmark-all"             % "0.36.8"             % testScope,
    "com.github.tomakehurst" %  "wiremock"                 % "2.27.2"             % testScope
  )
}
