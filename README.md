# customs-declare-exports-movements

[![Build Status](https://travis-ci.org/hmrc/customs-declare-exports-movements.svg)](https://travis-ci.org/hmrc/customs-declare-exports-movements) [ ![Download](https://api.bintray.com/packages/hmrc/releases/customs-declare-exports-movements/images/download.svg) ](https://bintray.com/hmrc/releases/customs-declare-exports-movements/_latestVersion)

This is a placeholder README.md for a new repository

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

### Scalastyle

Project contains scalafmt plugin.

Commands for code formatting:

```
scalafmt        # format compile sources
test:scalafmt   # format test sources
sbt:scalafmt    # format .sbt source
```

To ensure everything is formatted you can check project using commands below

```
scalafmt::test      # check compile sources
test:scalafmt::test # check test sources
sbt:scalafmt::test  # check .sbt sources
```