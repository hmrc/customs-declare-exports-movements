# customs-declare-exports-movements

This service is a backend service for Exports Movements UI.
Its responsibility is to store and allow access to information about Movement and Consolidation request submissions, as well as parsing, storing and accessing responses from Inventory Linking Exports.

## Technical documentation

### Notifications processing

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

#### Recognising Notification's type

There are 4 types of Notifications, Inventory Linking Exports can send:
* `inventoryLinkingMovementResponse`
* `inventoryLinkingControlResponse`
* `inventoryLinkingMovementTotalsResponse`
* `inventoryLinkingQueryResponse`

All are sent to the same endpoint so there is logic implemented to recognise the Notification's type.

All parsers extend [ResponseParser](ResponseParser.scala) class and define response types they handle.
Based on this information, [ResponseParserProvider](ResponseParserProvider.scala) extracts the type from XML and finds corresponding parser.

#### Parsing the Notification

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

## How to start the service locally
```bash
 sbt run
```
## How to start the service via service manager
```bash
 sm2 --start CUSTOMS_DECLARE_EXPORTS_MOVEMENTS 
```

All CDS Exports:

```bash
 sm2 --start CDS_EXPORTS_ALL
```

## How to Test this Service

### oLocal, Development, Staging Test Data

In the Local, Development, and Staging environments, stubbed data is used, except for the login URL, 
which remains consistent. To log in as a new user, modify any character in the EORI identifier value (e.g., GB123456789001).

- [local URL](http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A6791%2Fcustoms-declare-exports)
- [development URL](https://www.development.tax.service.gov.uk/auth-login-stub/gg-sign-in?continue=%2Fcustoms-declare-exports%2F)
- [staging URL](https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in?continue=%2Fcustoms-declare-exports%2F)
```
Enrolment Key : HMRC-CUS-ORG
Identifier Name : EORINumber
Identifier Value : GB123456789000
```
### QA Test Data

The QA environment behaves like a real live system and it interacts with DMS. So in order to login into QA please use the specified eori.

- [QA URL](https://www.qa.tax.service.gov.uk/auth-login-stub/gg-sign-in?continue=%2Fcustoms-declare-exports%2F)
```
Enrolment Key : HMRC-CUS-ORG
Identifier Name : EORINumber
Identifier Value : GB239355053000
```

### Running the test suite

To run unit tests, integration tests & scalafmt: 
```bash
 ./precheck.sh
```

### Related Test Services

- [cds-movements-performance tests](https://github.com/hmrc/cds-movements-performance-tests)


- [movements-ui-acceptance-tests](https://github.com/hmrc/movements-ui-acceptance-tests)


- [movements-internal-ui-acceptance-tests](https://github.com/hmrc/movements-internal-ui-acceptance-tests)

## Request/Response data sample
Movements request
```
{
  "eori": "GB123456789000",
  "providerId": "provider-123",
  "choice": "Arrival",
  "consignmentReference": {
    "reference": "M",
    "referenceValue": "GB123456789000-123ABC"
  },
  "location": {
    "code": "GBAU"
  },
  "movementDetails": {
    "dateTime": "2026-10-06T09:30:00Z"
  },
  "transport": {
    "modeOfTransport": "1",
    "nationality": "GB",
    "transportId": "ABC123"
  }
}
```
ILE Query Response
```
 {
  "timestampReceived": "2026-10-06T10:00:00.000Z",
  "conversationId": "conversation-id",
  "payload": "",
  "data": {
    "queriedDucr": {
      "ucr": "UCR-123",
      "parentMucr": "parent-mucr",
      "declarationId": "declaration-id",
      "entryStatus": {
        "ics": "3",
        "roe": "6",
        "soe": "14"
      },
      "goodsItem": [
        {
          "totalPackages": 13
        }
      ],
      "movements": [
        {
          "messageCode": "message-code",
          "goodsLocation": "goods-location",
          "movementDateTime": "2019-12-23T11:40:00.000Z",
          "movementReference": "movement-reference",
          "transportDetails": {
            "modeOfTransport": "mode",
            "nationality": "nationality",
            "transportId": "transport-id"
          }
        }
      ]
    },
    "parentMucr": {
      "ucr": "parent-mucr",
      "entryStatus": {
        "ics": "3",
        "roe": "H",
        "soe": "17"
      },
      "isShut": true,
      "movements": [
        {
          "messageCode": "message-code-mucr",
          "goodsLocation": "goods-location-mucr",
          "movementDateTime": "2019-12-23T12:30:00.000Z",
          "movementReference": "movement-reference-mucr",
          "transportDetails": {
            "modeOfTransport": "mode-mucr",
            "nationality": "nationality-mucr",
            "transportId": "transport-id-mucr"
          }
        }
      ]
    },
    "responseType": "response-type"
  }
}
```

## Further documentation

- [service catalogue](https://catalogue.tax.service.gov.uk/repositories/customs-declare-exports-movements) 


- [customs-declare-exports-movements pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-declare-exports-movements/)


- [all CDSExports jenkins pipelines](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/)

#### ILE Query

A flow diagram for ILE Query is available on [Confluence](https://confluence.tools.tax.service.gov.uk/display/CD/ILE+Query+flow+diagram).


## Licence

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
