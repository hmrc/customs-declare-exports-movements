package uk.gov.hmrc.exports.movements.migrations.changelogs.movementNotifications

import com.mongodb.client.{MongoCollection, MongoDatabase}
import com.mongodb.{MongoClient, MongoClientURI}
import org.bson.Document
import stubs.TestMongoDB
import stubs.TestMongoDB.mongoConfiguration
import uk.gov.hmrc.exports.movements.migrations.changelogs.ChangeLogsBaseSpec
import uk.gov.hmrc.exports.movements.migrations.changelogs.movementNotifications.MakeParsedDataOptionalSpec._

class MakeParsedDataOptionalSpec extends ChangeLogsBaseSpec {

  private val MongoURI = mongoConfiguration.get[String]("mongodb.uri")
  private val DatabaseName = TestMongoDB.DatabaseName
  private val CollectionName = "movementNotifications"

  private implicit val mongoDatabase: MongoDatabase = {
    val uri = new MongoClientURI(MongoURI.replaceAllLiterally("sslEnabled", "ssl"))
    val client = new MongoClient(uri)

    client.getDatabase(DatabaseName)
  }

  private val changeLog = new MakeParsedDataOptional()

  override def beforeEach(): Unit = {
    super.beforeEach()
    mongoDatabase.getCollection(CollectionName).drop()
  }

  override def afterEach(): Unit = {
    mongoDatabase.getCollection(CollectionName).drop()
    super.afterEach()
  }

  def runTest(inputDataJson: String, expectedDataJson: String)(test: MongoDatabase => Unit)(implicit mongoDatabase: MongoDatabase): Unit = {
    getMovementNotificationsCollection(mongoDatabase).insertOne(Document.parse(inputDataJson))

    test(mongoDatabase)

    val result: Document = getMovementNotificationsCollection(mongoDatabase).find().first()
    val expectedResult: String = expectedDataJson

    compareJson(result.toJson, expectedResult)
  }

  private def getMovementNotificationsCollection(db: MongoDatabase): MongoCollection[Document] = mongoDatabase.getCollection(CollectionName)

  "CacheChangeLog" should {

    "correctly migrate data" when {

      "migrating IleQueryResponse" in {

        runTest(TestDataBeforeChange.ileQueryResponse, TestDataAfterChange.ileQueryResponse)(changeLog.migrationFunction)
      }

      "migrating MovementTotalsResponse" in {

        runTest(TestDataBeforeChange.movementTotalsResponse, TestDataAfterChange.movementTotalsResponse)(changeLog.migrationFunction)
      }
    }

    "not change data already migrated" when {

      "migrating IleQueryResponse" in {

        runTest(TestDataAfterChange.ileQueryResponse, TestDataAfterChange.ileQueryResponse)(changeLog.migrationFunction)
      }

      "migrating MovementTotalsResponse" in {

        runTest(TestDataAfterChange.movementTotalsResponse, TestDataAfterChange.movementTotalsResponse)(changeLog.migrationFunction)
      }
    }
  }
}

object MakeParsedDataOptionalSpec {

  object TestDataBeforeChange {

    val ileQueryResponse: String =
      """{
      |   "_id": "3414ac6d-13ba-415c-9ac0-f5ffb3fc2fee",
      |   "timestampReceived" : "2020-12-02T10:35:37.564Z",
      |   "conversationId" : "516a31eb-0a35-4e03-8ef5-25d27a153caf",
      |   "responseType" : "inventoryLinkingQueryResponse",
      |   "payload" : "<inventoryLinkingQueryResponse><queriedDUCR><UCR>9SR657768312672-8</UCR><parentMUCR>GB/0000-99999TESTMUCR5</parentMUCR><declarationID>DECLARATIONID12345</declarationID><entryStatus><ics>3</ics><roe>6</roe><soe>D</soe></entryStatus><movement><messageCode>EAL</messageCode><goodsLocation>GBAUFXTFXTFXT</goodsLocation><goodsArrivalDateTime>2020-01-17T18:30:00.000Z</goodsArrivalDateTime><movementReference>MOVEMENTREFTEST4</movementReference></movement><movement><messageCode>EDL</messageCode><goodsLocation>GBAUFXTFXTFXT</goodsLocation><goodsDepartureDateTime>2020-01-18T18:40:00.000Z</goodsDepartureDateTime><movementReference>MOVEMENTREFTEST5</movementReference><transportDetails><transportID>ABC13579</transportID><transportMode>2</transportMode><transportNationality>GB</transportNationality></transportDetails></movement><goodsItem><totalPackages>10</totalPackages></goodsItem></queriedDUCR><parentMUCR><UCR>GB/0000-99999TESTMUCR5</UCR><entryStatus><roe>H</roe><soe>3</soe></entryStatus><shut>true</shut></parentMUCR></inventoryLinkingQueryResponse>",
      |   "data" : {
      |     "queriedDucr" : {
      |       "ucr" : "9SR657768312672-8",
      |       "parentMucr" : "GB/0000-99999TESTMUCR5",
      |       "declarationId" : "DECLARATIONID12345",
      |       "entryStatus" : {
      |         "ics" : "3",
      |         "roe" : "6",
      |         "soe" : "D"
      |       },
      |       "movements" : [
      |         {
      |           "messageCode" : "EAL",
      |           "goodsLocation" : "GBAUFXTFXTFXT",
      |           "movementDateTime" : "2020-01-17T18:30:00Z",
      |           "movementReference" : "MOVEMENTREFTEST4"
      |         },
      |         {
      |           "messageCode" : "EDL",
      |           "goodsLocation" : "GBAUFXTFXTFXT",
      |           "movementDateTime" : "2020-01-18T18:40:00Z",
      |           "movementReference" : "MOVEMENTREFTEST5",
      |           "transportDetails" : {
      |             "modeOfTransport" : "2",
      |             "nationality" : "GB",
      |             "transportId" : "ABC13579"
      |           }
      |         }
      |       ],
      |       "goodsItem" : [
      |         {
      |           "totalPackages" : 10
      |         }
      |       ]
      |     },
      |     "parentMucr" : {
      |       "ucr" : "GB/0000-99999TESTMUCR5",
      |       "entryStatus" : {
      |         "roe" : "H",
      |         "soe" : "3"
      |       },
      |       "isShut" : true,
      |       "movements" : []
      |     },
      |     "childMucrs" : [],
      |     "typ" : "QueryResponse",
      |     "childDucrs" : []
      |   }
      |}
    """.stripMargin

    val movementTotalsResponse: String =
      """{
        |   "_id": "3414ac6d-13ba-415c-9ac0-f5ffb3fc2fee",
        |   "timestampReceived" : "2020-12-04T13:02:43.694Z",
        |   "conversationId" : "ed0cd95a-de03-468d-9803-ff6040a8361f",
        |   "responseType" : "inventoryLinkingMovementTotalsResponse",
        |   "payload" : "<n1:inventoryLinkingMovementTotalsResponse xsi:schemaLocation=\"http://gov.uk/customs/inventoryLinking/v1 ../../schema/request/request_schema.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:n1=\"http://gov.uk/customs/inventoryLinking/v1\"><n1:messageCode>ERS</n1:messageCode><n1:goodsArrivalDateTime>2020-12-04T13:02:00Z</n1:goodsArrivalDateTime><n1:goodsLocation>GBAUFXTFXTFXT</n1:goodsLocation><n1:movementReference>GSLl3AE0Aq9r1uzfSvF9mEApC</n1:movementReference><n1:entry><n1:ucrBlock><n1:ucr>9GB123456123456-4657</n1:ucr><n1:ucrType>D</n1:ucrType></n1:ucrBlock><n1:entryStatus><n1:roe>6</n1:roe><n1:soe>3</n1:soe></n1:entryStatus></n1:entry></n1:inventoryLinkingMovementTotalsResponse>",
        |   "data" : {
        |     "entries" : [
        |       {
        |         "ucrBlock" : {
        |           "ucr" : "9GB123456123456-4657",
        |           "ucrType" : "D"
        |         },
        |         "goodsItem" : [],
        |         "entryStatus" : {
        |           "roe" : "6",
        |           "soe" : "3"
        |         }
        |       }
        |     ],
        |     "movementReference" : "GSLl3AE0Aq9r1uzfSvF9mEApC",
        |     "goodsLocation" : "GBAUFXTFXTFXT",
        |     "errorCodes" : [],
        |     "messageCode" : "ERS",
        |     "typ" : "StandardResponse",
        |     "goodsArrivalDateTime" : "2020-12-04T13:02:00Z"
        |   }
        |}
      """.stripMargin
  }

  object TestDataAfterChange {

    val ileQueryResponse: String =
      """{
        |   "_id": "3414ac6d-13ba-415c-9ac0-f5ffb3fc2fee",
        |   "timestampReceived" : "2020-12-02T10:35:37.564Z",
        |   "conversationId" : "516a31eb-0a35-4e03-8ef5-25d27a153caf",
        |   "payload" : "<inventoryLinkingQueryResponse><queriedDUCR><UCR>9SR657768312672-8</UCR><parentMUCR>GB/0000-99999TESTMUCR5</parentMUCR><declarationID>DECLARATIONID12345</declarationID><entryStatus><ics>3</ics><roe>6</roe><soe>D</soe></entryStatus><movement><messageCode>EAL</messageCode><goodsLocation>GBAUFXTFXTFXT</goodsLocation><goodsArrivalDateTime>2020-01-17T18:30:00.000Z</goodsArrivalDateTime><movementReference>MOVEMENTREFTEST4</movementReference></movement><movement><messageCode>EDL</messageCode><goodsLocation>GBAUFXTFXTFXT</goodsLocation><goodsDepartureDateTime>2020-01-18T18:40:00.000Z</goodsDepartureDateTime><movementReference>MOVEMENTREFTEST5</movementReference><transportDetails><transportID>ABC13579</transportID><transportMode>2</transportMode><transportNationality>GB</transportNationality></transportDetails></movement><goodsItem><totalPackages>10</totalPackages></goodsItem></queriedDUCR><parentMUCR><UCR>GB/0000-99999TESTMUCR5</UCR><entryStatus><roe>H</roe><soe>3</soe></entryStatus><shut>true</shut></parentMUCR></inventoryLinkingQueryResponse>",
        |   "data" : {
        |     "responseType" : "inventoryLinkingQueryResponse",
        |     "queriedDucr" : {
        |       "ucr" : "9SR657768312672-8",
        |       "parentMucr" : "GB/0000-99999TESTMUCR5",
        |       "declarationId" : "DECLARATIONID12345",
        |       "entryStatus" : {
        |         "ics" : "3",
        |         "roe" : "6",
        |         "soe" : "D"
        |       },
        |       "movements" : [
        |         {
        |           "messageCode" : "EAL",
        |           "goodsLocation" : "GBAUFXTFXTFXT",
        |           "movementDateTime" : "2020-01-17T18:30:00Z",
        |           "movementReference" : "MOVEMENTREFTEST4"
        |         },
        |         {
        |           "messageCode" : "EDL",
        |           "goodsLocation" : "GBAUFXTFXTFXT",
        |           "movementDateTime" : "2020-01-18T18:40:00Z",
        |           "movementReference" : "MOVEMENTREFTEST5",
        |           "transportDetails" : {
        |             "modeOfTransport" : "2",
        |             "nationality" : "GB",
        |             "transportId" : "ABC13579"
        |           }
        |         }
        |       ],
        |       "goodsItem" : [
        |         {
        |           "totalPackages" : 10
        |         }
        |       ]
        |     },
        |     "parentMucr" : {
        |       "ucr" : "GB/0000-99999TESTMUCR5",
        |       "entryStatus" : {
        |         "roe" : "H",
        |         "soe" : "3"
        |       },
        |       "isShut" : true,
        |       "movements" : []
        |     },
        |     "childMucrs" : [],
        |     "typ" : "QueryResponse",
        |     "childDucrs" : []
        |   }
        |}
      """.stripMargin

    val movementTotalsResponse: String =
      """{
        |   "_id": "3414ac6d-13ba-415c-9ac0-f5ffb3fc2fee",
        |   "timestampReceived" : "2020-12-04T13:02:43.694Z",
        |   "conversationId" : "ed0cd95a-de03-468d-9803-ff6040a8361f",
        |   "payload" : "<n1:inventoryLinkingMovementTotalsResponse xsi:schemaLocation=\"http://gov.uk/customs/inventoryLinking/v1 ../../schema/request/request_schema.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:n1=\"http://gov.uk/customs/inventoryLinking/v1\"><n1:messageCode>ERS</n1:messageCode><n1:goodsArrivalDateTime>2020-12-04T13:02:00Z</n1:goodsArrivalDateTime><n1:goodsLocation>GBAUFXTFXTFXT</n1:goodsLocation><n1:movementReference>GSLl3AE0Aq9r1uzfSvF9mEApC</n1:movementReference><n1:entry><n1:ucrBlock><n1:ucr>9GB123456123456-4657</n1:ucr><n1:ucrType>D</n1:ucrType></n1:ucrBlock><n1:entryStatus><n1:roe>6</n1:roe><n1:soe>3</n1:soe></n1:entryStatus></n1:entry></n1:inventoryLinkingMovementTotalsResponse>",
        |   "data" : {
        |     "responseType" : "inventoryLinkingMovementTotalsResponse",
        |     "entries" : [
        |       {
        |         "ucrBlock" : {
        |           "ucr" : "9GB123456123456-4657",
        |           "ucrType" : "D"
        |         },
        |         "goodsItem" : [],
        |         "entryStatus" : {
        |           "roe" : "6",
        |           "soe" : "3"
        |         }
        |       }
        |     ],
        |     "movementReference" : "GSLl3AE0Aq9r1uzfSvF9mEApC",
        |     "goodsLocation" : "GBAUFXTFXTFXT",
        |     "errorCodes" : [],
        |     "messageCode" : "ERS",
        |     "typ" : "StandardResponse",
        |     "goodsArrivalDateTime" : "2020-12-04T13:02:00Z"
        |   }
        |}
      """.stripMargin
  }

}
