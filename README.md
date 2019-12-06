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

### Principles

##### Component Tests
Component tests should simple and test the entire stack e.g.
- Request coming into the service - Using a real HTTP request
- Data in the DB as a result - Using a real MongoDB
- Requests to downstream services as a result - Using WireMock

They should also act as a "spec" for our API and send real `JSON/XML` and verify on real `JSON/XML`.
By the above we mean, dont use `Json.toJson(some scala model)` to generate your expected payload.
Also, dont use `TestData` helpers to build the requests. 
If we do this it will mean that we cannot change the signature of the API accidentally without breaking the component tests.

