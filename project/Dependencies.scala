import sbt._

object Dependencies {

  val bootstrapPlayVersion = "9.11.0"
  val hmrcMongoVersion = "2.6.0"
  val jacksonVersion = "2.18.0"

  val compile: Seq[ModuleID] = List(
    "uk.gov.hmrc"                      %% "bootstrap-backend-play-30"     % bootstrapPlayVersion,
    "uk.gov.hmrc"                      %% "wco-dec"                       % "0.40.0",
    "uk.gov.hmrc"                      %% "play-json-union-formatter"     % "1.22.0",
    "uk.gov.hmrc.mongo"                %% "hmrc-mongo-play-30"            % hmrcMongoVersion,
    "com.fasterxml.jackson.module"     %% "jackson-module-scala"          % jacksonVersion,
    "com.fasterxml.jackson.dataformat" %  "jackson-dataformat-xml"        % jacksonVersion,
    "com.fasterxml.jackson.dataformat" %  "jackson-dataformat-properties" % jacksonVersion
)

  val test: Seq[ModuleID] = List(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapPlayVersion % "test",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"  % hmrcMongoVersion     % "test",
    "com.vladsch.flexmark"   %  "flexmark-all"             % "0.64.8"             % "test",
    "org.mockito"            %% "mockito-scala-scalatest"  % "1.17.37"            % "test",
    "org.scalatest"          %% "scalatest"                % "3.2.19"             % "test",
  )

  def apply(): Seq[ModuleID] = (compile ++ test).map(_.withSources)
}
