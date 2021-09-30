# customs-declare-exports-movements

[![Build Status](https://travis-ci.org/hmrc/customs-declare-exports-movements.svg)](https://travis-ci.org/hmrc/customs-declare-exports-movements) [ ![Download](https://api.bintray.com/packages/hmrc/releases/customs-declare-exports-movements/images/download.svg) ](https://bintray.com/hmrc/releases/customs-declare-exports-movements/_latestVersion)

This service is a backend service for Exports Movements UI.
Its responsibility is to store and allow access to information about Movement and Consolidation request submissions, as well as parsing, storing and accessing responses from Inventory Linking Exports.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

## Scalastyle

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

## Principles

#### Component Tests
Component tests should simple and test the entire stack e.g.
- Request coming into the service - Using a real HTTP request
- Data in the DB as a result - Using a real MongoDB
- Requests to downstream services as a result - Using WireMock

They should also act as a "spec" for our API and send real `JSON/XML` and verify on real `JSON/XML`.
By the above we mean, dont use `Json.toJson(some scala model)` to generate your expected payload.
Also, dont use `TestData` helpers to build the requests. 
If we do this it will mean that we cannot change the signature of the API accidentally without breaking the component tests.

## Notifications processing

Notification is an asynchronous response to Movement or Consolidation request, coming from Inventory Linking Exports service.

Notifications processing contains of a few steps in order to parse and store data from ILE Notification:
1. Extract `conversationId` from request headers
2. Validate request's body against the XSD Schema
3. Recognise Notification's type and choose parser class
4. Parse the Notification
5. Insert Notification into `notifications` collection, containing both XML payload and parsed data

Should the process fail at any of the 2-4 steps, Notification's payload is stored into `notifications` collection, but without parsed data.
There is Routine running at the start of the service that runs the parsing process again for these Notifications.

If any of the steps fail, the service responds with `Accepted` HTTP status. The only scenario when the response is different is when the service is unable to handle request.  

It is worth mentioning that if the service fails to extract `conversationId` (step no. 1), it ends up with Notification payload not being stored as `conversationId` is required to create `Notification` instance.
However, absence of `conversationId` in request headers should be treated as a significant fault on the sending side.
In order to spot such situation, a message is logged with `warn` level. 

### Recognising Notification's type

There are 4 types of Notifications, Inventory Linking Exports can send:
* `inventoryLinkingMovementResponse`
* `inventoryLinkingControlResponse`
* `inventoryLinkingMovementTotalsResponse`
* `inventoryLinkingQueryResponse`

All are sent to the same endpoint so there is logic implemented to recognise the Notification's type.

All parsers extend [ResponseParser](ResponseParser.scala) class and define response types they handle.
Based on this information, [ResponseParserProvider](ResponseParserProvider.scala) extracts the type from XML and finds corresponding parser.

### Parsing the Notification

Notification's data is parsed and put into `data` field in [Notification](Notification.scala) class.

What is important is that `data` field is of type `NotificationData` (trait) which is extended by 2 classes: 
* `StandardNotificationData` - stores data from `inventoryLinkingMovementResponse`, `inventoryLinkingControlResponse` and `inventoryLinkingMovementTotalsResponse` types
* `IleQueryResponseData` - stores data from `inventoryLinkingQueryResponse` type

The reason for having 2 data classes is that initially there was no handling for `inventoryLinkingQueryResponse`.
The 3 other Notification types contain information that has the same meaning from this project's perspective.
Due to the way these Notifications are handled in the project, there was never a need to use different models for them.

However, `inventoryLinkingQueryResponse` handling was implemented later and this type contains very different information.
What is even more important is that use case for this type is different to other Notifications.
Therefore, there is a dedicated model for it.

```
inventoryLinkingMovementResponse            \
inventoryLinkingControlResponse              >-->    StandardNotificationData
inventoryLinkingMovementTotalsResponse      /

inventoryLinkingQueryResponse                 -->    IleQueryResponseData
```

## ILE Query

A flow diagram for ILE Query is available on [Confluence](https://confluence.tools.tax.service.gov.uk/display/CD/ILE+Query+flow+diagram).
