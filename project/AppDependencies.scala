import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "8.3.0"
  val hmrcMongoVersion = "1.7.0"
  val jacksonVersion = "2.15.2"
  val testScope = "test,it"

  val compile = Seq(
    "uk.gov.hmrc"                          %% "bootstrap-backend-play-30"           % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"                    %% "hmrc-mongo-play-30"                  % hmrcMongoVersion,
    "uk.gov.hmrc"                          %% "wco-dec"                             % "0.39.0" excludeAll ExclusionRule("com.fasterxml.jackson.dataformat"),
    "uk.gov.hmrc"                          %% "play-json-union-formatter"           % "1.20.0",
    "com.github.tototoshi"                 %% "scala-csv"                           % "1.3.10",
    "com.fasterxml.jackson.module"         %% "jackson-module-scala"                % jacksonVersion,
    "com.fasterxml.jackson.dataformat"     % "jackson-dataformat-xml"               % jacksonVersion,
    "com.fasterxml.jackson.dataformat"     % "jackson-dataformat-properties"        % jacksonVersion
  )

  val test = Seq(
    "uk.gov.hmrc"               %% "bootstrap-test-play-30"     % bootstrapPlayVersion   % testScope,
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion       % testScope,
    "com.vladsch.flexmark"      %  "flexmark-all"               % "0.64.6"               % testScope,
    "org.mockito"               %% "mockito-scala-scalatest"    % "1.17.29"              % testScope,
    "org.scalatest"             %% "scalatest"                  % "3.2.15"               % testScope,
  )
}
