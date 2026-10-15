# customs-declare-exports-movements

## About
This service is a backend service for Exports Movements UI.

Its responsibility is to store and allow access to information about Movement and Consolidation request submissions, as well as parsing, storing and accessing responses from Inventory Linking Exports.

|Key |Value|
|---|---|
| Digital service | CDS Exports |
| Local port | `6797` |
| Base path | `customs-declare-exports-movements` |
| Front-end | [customs-movements-frontend](https://github.com/hmrc/customs-movements-frontend) (port `6796`) |
| Internal Front-end | [customs-exports-internal-frontend](https://github.com/hmrc/customs-exports-internal-frontend) (port `6796`) |
| Acceptance tests | [movements-ui-acceptance-tests](https://github.com/hmrc/movements-ui-acceptance-tests) |
| Internal Acceptance tests | [movements-internal-ui-acceptance-tests](https://github.com/hmrc/movements-internal-ui-acceptance-tests) |
| Performance tests | [cds-movements-performance-tests](https://github.com/hmrc/cds-movements-performance-tests) |
| Stubs used in Local and Staging |[customs-inventory-linking-exports-stub](https://github.com/hmrc/customs-inventory-linking-exports-stub) |

## How to Run This Service

### Prerequisites
Use [Service Manager](#service-manager-profiles) to start all the services that Exports Movements depends on:

```bash
sm2 --start CDS_EXPORTS_ALL
```

### Running the Service Locally
To run this service from source (for example, to test your own changes), stop the instance started by Service Manager and run it with sbt:

```bash
sm2 --stop CUSTOMS_DECLARE_EXPORTS_MOVEMENTS 
sbt run
```

To test this service via our frontend service, refer to the [customs-movements-frontend](https://github.com/hmrc/customs-movements-frontend#running-the-service-locally) repository's README.

## How to Test This Service

### Local

#### Unit Tests
```bash
sbt test
```

#### Integration Tests
```bash
sbt it/test
```

#### Pre-Push Check
There is a script called `precheck.sh` that runs all tests, examines their coverage and checks if all the files are properly formatted.
It is good practice to run it just before pushing to GitHub.

```bash
./precheck.sh
```

#### Acceptance Tests (smoke and regression)
The acceptance tests for this service are in [movements-ui-acceptance-tests](https://github.com/hmrc/movements-ui-acceptance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/movements-ui-acceptance-tests#how-to-run-the-tests).

The internal UI acceptance tests for this service are in [movements-internal-ui-acceptance-tests](https://github.com/hmrc/movements-internal-ui-acceptance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/movements-internal-ui-acceptance-tests#how-to-run-the-tests).

#### Performance tests
The performance tests for this service are in [cds-movements-performance-tests](https://github.com/hmrc/cds-movements-performance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/cds-movements-performance-tests#how-to-run-the-tests).

### Manual Testing in QA and Staging
After the successful [deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-declare-exports-movements-pipeline/), manual testing is performed in the QA and/or Staging environments, see [Manual Service Verification in QA and Staging](https://github.com/hmrc/movements-ui-acceptance-tests#manual-testing-in-qa-and-staging).
## Service Catalogue
- [customs-declare-exports-movements in the MDTP Catalogue](https://catalogue.tax.service.gov.uk/repositories/customs-declare-exports-movements)

## Jenkins Pipeline
- [Build pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-declare-exports-movements/)
- [Deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-declare-exports-movements-pipeline/)

- The acceptance test Jenkins jobs (smoke and regression) are listed in the [movements-ui-acceptance-tests README](https://github.com/hmrc/movements-ui-acceptance-tests#how-to-run-tests).
- The performance test Jenkins jobs are listed in the [cds-movements-performance-tests README](https://github.com/hmrc/cds-movements-performance-tests/#jenkins-builds).

## Service Manager Profiles
These profiles are defined in [service-manager-config](https://github.com/hmrc/service-manager-config).

| Profile | Use | Services started |
|---|---|---|
| `CDS_EXPORTS_ALL` | All CDS Exports services (declarations, movements and internal) | See [service-manager-config](https://github.com/hmrc/service-manager-config/blob/main/profiles.json) |

| Service | Port | Repository |
|---|---|---|
| `CUSTOMS_DECLARE_EXPORTS_MOVEMENTS` | 6797 | This service |
| `CUSTOMS_MOVEMENTS_FRONTEND ` | 6796 | [customs-movements-frontend](https://github.com/hmrc/customs-movements-frontend)|
| `CUSTOMS_EXPORTS_INTERNAL_FRONTEND ` | 6799 | [customs-declare-exports-frontend](https://github.com/hmrc/customs-exports-internal-frontend)|
| `CUSTOMS_INVENTORY_LINKING_EXPORTS_STUB` | 86450 | [customs-inventory-linking-exports-stub](https://github.com/hmrc/customs-inventory-linking-exports-stub) |


## Endpoints

When running locally, the service is available on `http://localhost:6797`.

| Method | Path | Purpose | Sample request | Response |
|---|---|---|---|---|
| POST | `/movements` | Submit an arrival, retrospective arrival, departure or create-empty-MUCR movement to Inventory Linking Exports | `MovementsRequest` JSON body – see examples below | `202` with the conversation ID |
| POST | `/consolidation` | Submit a consolidation request, such as associating or disassociating a DUCR/MUCR or shutting a MUCR | `ConsolidationRequest` JSON body – see examples below | `202` with the conversation ID |
| GET | `/submissions` | Retrieve movement and consolidation submissions | `?eori=eori` or `?providerId=provider-id` | `200` with an array of submissions |
| GET | `/submissions/:conversationId` | Retrieve a submission by conversation ID | `/submissions/conversation-id?eori=eori` | `200` with the submission |
| GET | `/notifications` | Retrieve parsed notifications for an EORI or provider | `?eori=eori` or `?providerId=provider-id` | `200` with an array of notifications |
| GET | `/notifications/:conversationId` | Retrieve parsed notifications for a submission | `/notifications/conversation-id?eori=eori` | `200` with an array of notifications |
| POST | `/consignment-query` | Submit an ILE query for a DUCR or MUCR | `IleQueryRequest` JSON body – see example below | `202` with the conversation ID |
| GET | `/consignment-query/:conversationId` | Retrieve responses for an ILE query | `/consignment-query/conversation-id?eori=eori` | `200` with an array of ILE query responses<br>Query timed out: `424` |
| POST | `/customs-declare-exports/notifyMovement` | Receive asynchronous XML notifications from Inventory Linking Exports | XML body with `X-Conversation-ID` header | `202` |

### Example Payloads

#### Submit a Movement

`POST /movements`

The `choice` field determines the type of movement.

Supported values are:

- `Arrival`
- `RetrospectiveArrival`
- `Departure`
- `CreateEmptyMucr`

##### Arrival

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "choice": "Arrival",
  "consignmentReference": {
    "reference": "M",
    "referenceValue": "UCR"
  },
  "location": {
    "code": "GBAU"
  },
  "movementDetails": {
    "dateTime": "2026-10-15T10:30:00Z"
  },
  "transport": {
    "modeOfTransport": "1",
    "nationality": "GB",
    "transportId": "ABC123"
  }
}
```

A successful submission returns `202 Accepted` with the Inventory Linking Exports conversation ID:

```text
conversation-id
```

`providerId` is optional.

##### Departure

A departure uses the same payload structure with `choice` set to `Departure`:

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "choice": "Departure",
  "consignmentReference": {
    "reference": "M",
    "referenceValue": "UCR"
  },
  "location": {
    "code": "GBAU"
  },
  "movementDetails": {
    "dateTime": "2026-10-15T10:30:00Z"
  },
  "transport": {
    "modeOfTransport": "1",
    "nationality": "GB",
    "transportId": "ABC123"
  }
}
```

##### Retrospective Arrival

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "choice": "RetrospectiveArrival",
  "consignmentReference": {
    "reference": "M",
    "referenceValue": "UCR"
  },
  "location": {
    "code": "GBAU"
  },
  "transport": {
    "modeOfTransport": "1",
    "nationality": "GB",
    "transportId": "ABC123"
  }
}
```


##### Create Empty MUCR

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "choice": "CreateEmptyMucr",
  "consignmentReference": {
    "reference": "D",
    "referenceValue": "DUCR"
  },
  "location": {
    "code": "GBAU"
  }
}
```

#### Submit a Consolidation

`POST /consolidation`

The payload depends on the value of `consolidationType`.

Supported consolidation types include:

- `DucrAssociation`
- `DucrDisassociation`
- `MucrAssociation`
- `MucrDisassociation`
- `DucrPartAssociation`
- `DucrPartDisassociation`
- `ShutMucr`

##### Associate a DUCR with a MUCR

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "consolidationType": "DucrAssociation",
  "mucr": "MUCR",
  "ucr": "DUCR"
}
```

##### Disassociate a DUCR

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "consolidationType": "DucrDisassociation",
  "ucr": "DUCR"
}
```

##### Associate a MUCR with another MUCR

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "consolidationType": "MucrAssociation",
  "mucr": "PARENT-MUCR",
  "ucr": "CHILD-MUCR"
}
```

##### Disassociate a MUCR

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "consolidationType": "MucrDisassociation",
  "ucr": "MUCR"
}
```

##### Associate a DUCR Part

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "consolidationType": "DucrPartAssociation",
  "mucr": "MUCR",
  "ucr": "DUCR-PART"
}
```

##### Disassociate a DUCR Part

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "consolidationType": "DucrPartDisassociation",
  "ucr": "DUCR-PART"
}
```

##### Shut a MUCR

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "consolidationType": "ShutMucr",
  "mucr": "MUCR"
}
```

A successful consolidation submission returns `202 Accepted` with the conversation ID:

```text
conversation-id
```

#### Retrieve Submissions

`GET /submissions`

Submissions can be filtered using either `eori` or `providerId`:

```text
GET /submissions?eori=GB123456789000
```

or:

```text
GET /submissions?providerId=provider-id
```

Example response:

```json
[
  {
    "uuid": "submission-id",
    "eori": "GB123456789000",
    "providerId": "provider-id",
    "conversationId": "conversation-id",
    "ucrBlocks": [
      {
        "ucr": "UCR",
        "ucrType": "M"
      }
    ],
    "actionType": "Arrival",
    "requestTimestamp": "2026-10-15T10:30:00Z"
  }
]
```

A single submission can be retrieved by its conversation ID:

```text
GET /submissions/conversation-id?eori=GB123456789000
```

#### Retrieve Notifications

`GET /notifications`

Notifications can be filtered by `eori` or `providerId`:

```text
GET /notifications?eori=GB123456789000
```

Example response:

```json
[
  {
    "timestampReceived": "2026-10-15T10:35:00Z",
    "conversationId": "conversation-id",
    "responseType": "inventoryLinkingMovementResponse",
    "entries": [
      {
        "ucrBlock": {
          "ucr": "UCR",
          "ucrType": "M"
        },
        "goodsItem": []
      }
    ],
    "crcCode": "crc-code",
    "actionCode": "action-code",
    "errorCodes": [],
    "messageCode": "EAL"
  }
]
```

Notifications for a specific submission can be retrieved using its conversation ID:

```text
GET /notifications/conversation-id?eori=GB123456789000
```

#### Submit an ILE Query

`POST /consignment-query`

Example DUCR query:

```json
{
  "eori": "GB123456789000",
  "providerId": "provider-id",
  "ucrBlock": {
    "ucr": "UCR-123",
    "ucrType": "D"
  }
}
```

A successful request returns `202 Accepted` with the conversation ID:

```text
conversation-id
```

#### Retrieve an ILE Query Response

`GET /consignment-query/:conversationId`

Example:

```text
GET /consignment-query/conversation-id?eori=GB123456789000
```

Example successful response:

```json
[
  {
    "timestampReceived": "2026-10-15T10:35:00Z",
    "conversationId": "conversation-id",
    "responseType": "inventoryLinkingQueryResponse",
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
        "movements": [
          {
            "messageCode": "EAL",
            "goodsLocation": "goods-location",
            "movementDateTime": "2019-12-23T11:40:00Z",
            "movementReference": "movement-reference",
            "transportDetails": {
              "modeOfTransport": "1",
              "nationality": "GB",
              "transportId": "transport-id"
            }
          }
        ],
        "goodsItem": [
          {
            "totalPackages": 13
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
        "movements": []
      },
      "childMucrs": [],
      "typ": "SuccessfulResponseExchange",
      "childDucrs": []
    }
  }
]
```

If the service times out while waiting for an ILE query response, it returns `424 Failed Dependency`.

#### Inventory Linking Notification Callback

`POST /customs-declare-exports/notifyMovement`

Inventory Linking Exports sends notification responses asynchronously to this endpoint as XML.

The request must contain an `X-Conversation-ID` header identifying the original submission:

```text
X-Conversation-ID: conversation-id
Content-Type: application/xml
```

Example:

```xml
<inventoryLinkingMovementResponse xmlns="http://gov.uk/customs/inventoryLinking/v1">
  <messageCode>EAL</messageCode>
  <crc>crc-code</crc>
  <goodsArrivalDateTime>2026-10-15T10:35:00Z</goodsArrivalDateTime>
  <goodsLocation>GBAU</goodsLocation>
  <movementReference>movement-reference</movementReference>
  <ucrBlock>
    <ucr>UCR</ucr>
    <ucrType>M</ucrType>
  </ucrBlock>
</inventoryLinkingMovementResponse>
```

A successfully received notification returns:

```text
202 Accepted
```

If the `X-Conversation-ID` header is missing, the notification is dropped and the service still returns `202 Accepted`.

Notifications larger than the configured maximum payload size are rejected with `413 Request Entity Too Large`.

## Developer Notes

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

### ILE Query

A flow diagram for ILE Query is available on [Confluence](https://confluence.tools.tax.service.gov.uk/display/CD/ILE+Query+flow+diagram).

### Scalafmt
The code is formatted with [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html#sbt), using the rules in `.scalafmt.conf`.

Check that all project files are formatted as expected:

```bash
sbt scalafmtCheckAll scalafmtSbtCheck
```

Format `*.sbt` and `project/*.scala` files:

```bash
sbt scalafmtSbt
```

Format all project files:

```bash
sbt scalafmtAll
```

## License
This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).