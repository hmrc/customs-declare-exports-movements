import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.{ForkedJvmPerTestSettings, SbtArtifactory, SbtAutoBuildPlugin}

val appName = "customs-declare-exports-movements"

PlayKeys.devSettings := Seq("play.server.http.port" -> "6797")

lazy val allResolvers = resolvers ++= Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  Resolver.jcenterRepo
)

lazy val ComponentTest = config("component") extend Test
lazy val IntegrationTest = config("it") extend Test

lazy val testAll = TaskKey[Unit]("test-all")
lazy val allTest = Seq(testAll := (test in ComponentTest)
  .dependsOn((test in IntegrationTest).dependsOn(test in Test)).value)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    dependencyOverrides ++= AppDependencies.jettyOverrides.toSeq,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    majorVersion := 0,
    scalaVersion := "2.12.12"
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .configs(ComponentTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(inConfig(ComponentTest)(Defaults.itSettings): _*)
  .settings(commonSettings,
    unitTestSettings,
    integrationTestSettings,
    componentTestSettings,
    allResolvers,
    scoverageSettings,
    silencerSettings)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427

def onPackageName(rootPackage: String): String => Boolean = {
  testName => testName startsWith rootPackage
}

lazy val unitTestSettings =
  inConfig(Test)(Defaults.testTasks) ++
    Seq(
      unmanagedSourceDirectories in Test := Seq(
        (baseDirectory in Test).value / "test/unit",
        (baseDirectory in Test).value / "test/utils"
      ),
      addTestReportOption(Test, "test-reports")
    )

lazy val integrationTestSettings =
  inConfig(IntegrationTest)(Defaults.testTasks) ++
    Seq(
      unmanagedSourceDirectories in IntegrationTest := Seq(
        (baseDirectory in IntegrationTest).value / "test/it",
        (baseDirectory in Test).value / "test/utils"
      ),
      fork in IntegrationTest := false,
      parallelExecution in IntegrationTest := false,
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := ForkedJvmPerTestSettings.oneForkedJvmPerTest((definedTests in IntegrationTest).value)
    )

lazy val componentTestSettings =
  inConfig(ComponentTest)(Defaults.testTasks) ++ Seq(
    unmanagedSourceDirectories in ComponentTest := Seq(
      (baseDirectory in ComponentTest).value / "test/component",
      (baseDirectory in Test).value / "test/utils"
    ),
    fork in ComponentTest := false,
    parallelExecution in ComponentTest := false,
    addTestReportOption(ComponentTest, "int-test-reports"),
    testGrouping in ComponentTest := ForkedJvmPerTestSettings.oneForkedJvmPerTest((definedTests in ComponentTest).value)
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
  coverageMinimum := 90,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  parallelExecution in Test := false
)

lazy val commonSettings: Seq[Setting[_]] = publishingSettings ++ defaultSettings()

lazy val silencerSettings: Seq[Setting[_]] = {
  val silencerVersion = "1.7.0"
  Seq(
    libraryDependencies ++= Seq(compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full)),
    // silence all warnings on autogenerated files
    scalacOptions += "-P:silencer:pathFilters=target/.*",
    // Make sure you only exclude warnings for the project directories, i.e. make builds reproducible
    scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"
  )
}
