import sbt._
import uk.gov.hmrc.DefaultBuildSettings._

val appName = "customs-declare-exports-movements"

PlayKeys.devSettings := Seq("play.server.http.port" -> "6797")

lazy val IntegrationTest = config("it") extend Test

lazy val testAll = TaskKey[Unit]("test-all")
lazy val allTest = Seq(testAll := (IntegrationTest / test).dependsOn(Test / test).value)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(commonSettings*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(unitTestSettings, scoverageSettings)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := Seq(
      (IntegrationTest / baseDirectory).value / "test/it",
      (Test / baseDirectory).value / "test/util"
    ),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false
  )
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427

lazy val commonSettings = Seq(
  majorVersion := 0,
  scalaVersion := "2.13.12",
  scalacOptions ++= scalacFlags,
  dependencyOverrides += "commons-codec" % "commons-codec" % "1.15",
  dependencyOverrides += "org.scala-lang.modules" %% "scala-xml" % "2.1.0",
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
  "-Wconf:cat=unused-imports&src=routes/.*:s",  // silent "unused import" warnings from Play routes
  "-Wconf:cat=unused&src=.*routes.*:s",  // silence private val defaultPrefix in class Routes is never used
  "-Wconf:msg=eq not selected from this instance:s" // silence eq not selected from this instance warning
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
