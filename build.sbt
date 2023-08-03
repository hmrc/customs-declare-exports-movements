import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin

val appName = "customs-declare-exports-movements"

PlayKeys.devSettings := Seq("play.server.http.port" -> "6797")

lazy val IntegrationTest = config("it") extend Test

lazy val testAll = TaskKey[Unit]("test-all")
lazy val allTest = Seq(testAll := (IntegrationTest / test).dependsOn(Test / test).value)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .settings(commonSettings: _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(unitTestSettings, integrationTestSettings, scoverageSettings)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427

lazy val commonSettings = Seq(
  majorVersion := 0,
  scalaVersion := "2.13.8",
  scalacOptions ++= scalacFlags,
  dependencyOverrides += "commons-codec" % "commons-codec" % "1.15",
  libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test
)

lazy val scalacFlags = Seq(
  "-deprecation",            // warn about use of deprecated APIs
  "-encoding", "UTF-8",      // source files are in UTF-8
  "-feature",                // warn about misused language features
  "-language:implicitConversions",
  "-unchecked",              // warn about unchecked type parameters
  "-Ywarn-numeric-widen",
  "-Xfatal-warnings",        // warnings are fatal!!
  "-Wconf:cat=unused-imports&src=routes/.*:s"       // silent "unused import" warnings from Play routes
)

def onPackageName(rootPackage: String): String => Boolean = {
  testName => testName startsWith rootPackage
}

lazy val unitTestSettings =
  inConfig(Test)(Defaults.testTasks) ++
    Seq(
      Test / unmanagedSourceDirectories := Seq(
        (Test / baseDirectory).value / "test/unit",
        (Test / baseDirectory).value / "test/utils"
      ),
      addTestReportOption(Test, "test-reports")
    )

lazy val integrationTestSettings =
  inConfig(IntegrationTest)(Defaults.testTasks) ++
    Seq(
      IntegrationTest / unmanagedSourceDirectories := Seq(
        (IntegrationTest / baseDirectory).value / "test/it",
        (Test / baseDirectory).value / "test/utils"
      ),
      IntegrationTest / fork := false,
      IntegrationTest / parallelExecution := false,
      addTestReportOption(IntegrationTest, "int-test-reports"),
      IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value)
    )

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List(
    "<empty>"
    ,"Reverse.*"
    ,"domain\\..*"
    ,"models\\..*"
    ,"metrics\\..*"
    ,".*(BuildInfo|Routes|Options).*"
  ).mkString(";"),
  coverageMinimumStmtTotal := 90,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  Test / parallelExecution := false
)
