import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "7.20.0"
  val hmrcMongoVersion = "1.3.0"
  val jacksonVersion = "2.15.2"
  val testScope = "test,it"

  val compile = Seq(
    "uk.gov.hmrc"                       %% "bootstrap-backend-play-28"     % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"                 %% "hmrc-mongo-play-28"            % hmrcMongoVersion,
    "uk.gov.hmrc"                       %% "wco-dec"                       % "0.39.0" excludeAll ExclusionRule("com.fasterxml.jackson.dataformat"),
    "uk.gov.hmrc"                       %% "play-json-union-formatter"     % "1.18.0-play-28",
    "com.typesafe.play"                 %% "play-json-joda"                % "2.9.4",
    "com.github.tototoshi"              %% "scala-csv"                     % "1.3.10",
    "com.fasterxml.jackson.module"      %% "jackson-module-scala"          % jacksonVersion,
    "com.fasterxml.jackson.dataformat"  %  "jackson-dataformat-xml"        % jacksonVersion,
    "com.fasterxml.jackson.dataformat"  %  "jackson-dataformat-properties" % jacksonVersion,
    // Used by the Migration tool. Try to keep it to the same version of mongo-scala-driver.
    "org.mongodb"                       %  "mongodb-driver-sync"           % "4.8.2"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapPlayVersion % testScope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion     % testScope,
    "com.vladsch.flexmark"   %  "flexmark-all"            % "0.64.6"             % testScope,
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.12"            % testScope,
    "org.scalatest"          %% "scalatest"               % "3.2.15"             % testScope,
    "com.github.tomakehurst" %  "wiremock-jre8"           % "2.35.0"             % testScope
  )
}
