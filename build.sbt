import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "customs-declare-exports-movements"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

PlayKeys.devSettings := Seq("play.server.http.port" -> "6797")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(commonSettings)
  .settings(scoverageSettings)

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    publish / skip := true,
    Test / testOptions += Tests.Argument("-o", "-h", "it/target/html-report")
  )

lazy val commonSettings = Seq(
  scalacOptions ++= scalacFlags,
  retrieveManaged := true,
  libraryDependencies ++= Dependencies.compile ++ Dependencies.test
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

addCommandAlias("ucomp", "Test/compile")
addCommandAlias("icomp", "it/Test/compile")
addCommandAlias("precommit", ";clean;scalafmt;Test/scalafmt;it/Test/scalafmt;coverage;test;it/test;scalafmtCheckAll;coverageReport")
