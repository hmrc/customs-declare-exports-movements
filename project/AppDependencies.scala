import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "7.7.0"
  val hmrcMongoVersion = "0.73.0"
  val testScope = "test,it"

  val compile = Seq(
    "uk.gov.hmrc"                       %% "bootstrap-backend-play-28"     % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"                 %% "hmrc-mongo-play-28"            % hmrcMongoVersion,
    "uk.gov.hmrc"                       %% "wco-dec"                       % "0.37.0" excludeAll ExclusionRule("com.fasterxml.jackson.dataformat"),
    "uk.gov.hmrc"                       %% "play-json-union-formatter"     % "1.16.0-play-28",
    "com.typesafe.play"                 %% "play-json-joda"                % "2.9.3",
    "com.github.tototoshi"              %% "scala-csv"                     % "1.3.10",
    "com.fasterxml.jackson.module"      %% "jackson-module-scala"          % "2.13.4",
    "com.fasterxml.jackson.dataformat"  %  "jackson-dataformat-xml"        % "2.13.4",
    "com.fasterxml.jackson.dataformat"  %  "jackson-dataformat-properties" % "2.13.4",
    // Used by the Migration tool. Try to keep it to the same version of mongo-scala-driver.
    "org.mongodb"                       %  "mongodb-driver-sync"           % "4.6.1"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % bootstrapPlayVersion % testScope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % hmrcMongoVersion     % testScope,
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.10.0"            % testScope,
    "com.github.tomakehurst" %  "wiremock"                 % "2.27.2"             % testScope
  )
}
