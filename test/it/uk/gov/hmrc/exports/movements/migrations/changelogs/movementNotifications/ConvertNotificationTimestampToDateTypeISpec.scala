package uk.gov.hmrc.exports.movements.migrations.changelogs.movementNotifications

import com.mongodb.client.{MongoClients, MongoDatabase}
import org.bson.Document
import stubs.TestMongoDB
import stubs.TestMongoDB.mongoConfiguration
import uk.gov.hmrc.exports.movements.migrations.changelogs.ChangeLogsBaseISpec
import uk.gov.hmrc.exports.movements.migrations.changelogs.movementNotifications.ConvertNotificationTimestampToDateTypeISpec._

class ConvertNotificationTimestampToDateTypeISpec extends ChangeLogsBaseISpec {

  private val MongoURI = mongoConfiguration.get[String]("mongodb.uri")
  private val DatabaseName = TestMongoDB.DatabaseName
  private val CollectionName = "movementNotifications"

  private val mongoDatabase: MongoDatabase =
    MongoClients.create(MongoURI.replace("sslEnabled", "ssl")).getDatabase(DatabaseName)

  private val changeLog = new ConvertNotificationTimestampToDateType

  override def beforeEach(): Unit = {
    super.beforeEach()
    mongoDatabase.getCollection(CollectionName).drop()
  }

  override def afterEach(): Unit = {
    mongoDatabase.getCollection(CollectionName).drop()
    super.afterEach()
  }

  def runTest(inputDataJson: String, expectedDataJson: String)(test: MongoDatabase => Unit): Unit = {
    mongoDatabase.getCollection(CollectionName).insertOne(Document.parse(inputDataJson))

    test(mongoDatabase)

    val result: Document = mongoDatabase.getCollection(CollectionName).find.first
    val expectedResult: String = expectedDataJson

    compareJson(result.toJson, expectedResult)
  }

  "ConvertNotificationTimestampToDateType" should {

    "correctly migrate data" when {

      "migrating a movements notification" in {
        runTest(TestDataBeforeChange.notification, TestDataAfterChange.notification)(changeLog.migrationFunction)
      }
    }

    "not change data already migrated" when {

      "migrating a movements notification" in {
        runTest(TestDataAfterChange.notification, TestDataAfterChange.notification)(changeLog.migrationFunction)
      }
    }
  }
}

object ConvertNotificationTimestampToDateTypeISpec {

  object TestDataBeforeChange {

    val notification: String =
      """{
        |   "_id": "3414ac6d-13ba-415c-9ac0-f5ffb3fc2fee",
        |   "timestampReceived" : "2020-12-04T13:02:43.694Z",
        |   "conversationId" : "ed0cd95a-de03-468d-9803-ff6040a8361f",
        |   "payload" : "<n1:inventoryLinkingMovementTotalsResponse xsi:schemaLocation=\"http://gov.uk/customs/inventoryLinking/v1 ../../schema/request/request_schema.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:n1=\"http://gov.uk/customs/inventoryLinking/v1\"><n1:messageCode>ERS</n1:messageCode><n1:goodsArrivalDateTime>2020-12-04T13:02:00Z</n1:goodsArrivalDateTime><n1:goodsLocation>GBAUFXTFXTFXT</n1:goodsLocation><n1:movementReference>GSLl3AE0Aq9r1uzfSvF9mEApC</n1:movementReference><n1:entry><n1:ucrBlock><n1:ucr>9GB123456123456-4657</n1:ucr><n1:ucrType>D</n1:ucrType></n1:ucrBlock><n1:entryStatus><n1:roe>6</n1:roe><n1:soe>3</n1:soe></n1:entryStatus></n1:entry></n1:inventoryLinkingMovementTotalsResponse>",
        |   "data" : {}
        |}
      """.stripMargin
  }

  object TestDataAfterChange {

    val notification: String =
      """{
        |   "_id": "3414ac6d-13ba-415c-9ac0-f5ffb3fc2fee",
        |   "timestampReceived" : {"$date":"2020-12-04T13:02:43.694Z"},
        |   "conversationId" : "ed0cd95a-de03-468d-9803-ff6040a8361f",
        |   "payload" : "<n1:inventoryLinkingMovementTotalsResponse xsi:schemaLocation=\"http://gov.uk/customs/inventoryLinking/v1 ../../schema/request/request_schema.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:n1=\"http://gov.uk/customs/inventoryLinking/v1\"><n1:messageCode>ERS</n1:messageCode><n1:goodsArrivalDateTime>2020-12-04T13:02:00Z</n1:goodsArrivalDateTime><n1:goodsLocation>GBAUFXTFXTFXT</n1:goodsLocation><n1:movementReference>GSLl3AE0Aq9r1uzfSvF9mEApC</n1:movementReference><n1:entry><n1:ucrBlock><n1:ucr>9GB123456123456-4657</n1:ucr><n1:ucrType>D</n1:ucrType></n1:ucrBlock><n1:entryStatus><n1:roe>6</n1:roe><n1:soe>3</n1:soe></n1:entryStatus></n1:entry></n1:inventoryLinkingMovementTotalsResponse>",
        |   "data" : {}
        |}
      """.stripMargin
  }
}
